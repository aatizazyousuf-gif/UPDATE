/*
 * GasGuard AI - ESP32 firmware
 * ----------------------------
 * Reads the MQ-2 gas sensor and the MPXV7002DP differential pressure sensor, and publishes
 * live readings to Cloud Firestore under a document unique to THIS device:
 *
 *   device_telemetry/{deviceId}
 *     ppm: 0.023                    <- MQ-2 gas reading
 *     pressureKpa: 0.84             <- MPXV7002DP differential pressure
 *     flowLpm: 1.2                  <- flow rate derived from that pressure
 *     consumptionKgPerHour: 0.09    <- mass consumption rate derived from the flow
 *     timestamp: 1737000000000      <- millis-since-epoch, set via NTP
 *     deviceStatus: "online"
 *
 * `deviceId` is derived from this chip's own unique MAC address, so every ESP32 gets its own
 * document automatically - no manual ID assignment needed. The first time it boots, it prints
 * that ID to Serial; copy it into the app's "Pair your gas meter" screen once, and from then
 * on that homeowner's account is the only one that can read this device's data (see
 * DevicePairingService.kt / firestore.rules.example on the app side for how that's enforced).
 *
 * Library required (Arduino IDE > Library Manager):
 *   "Firebase ESP Client" by mobizt  (search: Firebase ESP Client)
 *   -> this same library talks to both Realtime Database AND Firestore; we use its
 *      Firebase.Firestore.patchDocument() here since the app already has Firestore set up.
 *
 * Board: any ESP32 dev board.
 *
 * Wiring - MQ-2 (gas sensor):
 *   VCC -> 5V, GND -> GND, AOUT -> GPIO34 (ADC1, input-only, safe with WiFi active)
 *
 * Wiring - MPXV7002DP (differential pressure sensor):
 *   The MPXV7002DP is a 5V analog sensor with output centered at 2.5V (0.5V-4.5V full range)
 *   - that's above the ESP32's 3.3V ADC max, so you need a simple voltage divider between
 *   its Vout and GPIO35 (e.g. two 10k resistors: Vout -> R1 -> GPIO35 -> R2 -> GND). With two
 *   equal resistors that's a /2 divider, matched by DIVIDER_RATIO = 2.0 below - adjust if you
 *   use different resistor values.
 *   Vout -> divider -> GPIO35, Vs (5V) -> sensor pin 1, GND -> sensor pin 2.
 *
 * IMPORTANT - CALIBRATE FOR YOUR OWN SETUP:
 * Both the MQ-2-to-ppm curve and the pressure-to-flow-to-consumption conversion below use
 * generic formulas/placeholder constants. Every individual sensor and installation (tubing,
 * orifice size, whether you're tapping liquid or vapor phase) is different. Run the device,
 * compare against a known reference (a stopwatch + kitchen scale on the cylinder works),
 * and adjust R0_CLEAN_AIR / FLOW_CALIBRATION_K / LPG_DENSITY_KG_PER_L until it tracks.
 */

#include <WiFi.h>
#include <time.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>

// ---------- WiFi ----------
#define WIFI_SSID "YOUR_WIFI_SSID"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"

// ---------- Firebase project ----------
// Firebase Console > Project settings > General > Web API Key
#define API_KEY "YOUR_FIREBASE_WEB_API_KEY"
// Firebase Console > Project settings > General > Project ID
#define FIRESTORE_PROJECT_ID "YOUR_FIREBASE_PROJECT_ID"

// ---------- MQ-2 wiring / calibration ----------
#define MQ2_PIN 34           // ADC1 pin
#define RL_VALUE 5.0          // Load resistor on the MQ-2 breakout, in kOhm (5k on most boards)
#define R0_CLEAN_AIR 10.0     // Placeholder sensor resistance in clean air, kOhm - replace with
                               // your own calibrated value (run once in clean air, check Serial)

// ---------- MPXV7002DP wiring / calibration ----------
#define MPX_PIN 35            // ADC1 pin (see wiring note above re: voltage divider)
#define DIVIDER_RATIO 2.0     // Undo the voltage divider's scaling (2.0 for two equal resistors)
#define MPX_VS 5.0            // Sensor supply voltage
// Empirical constant relating sqrt(pressure) to volumetric flow (L/min). Start with 1.0 and
// tune against a real reference measurement - there's no universal correct value here, it
// depends entirely on your specific orifice/tubing.
#define FLOW_CALIBRATION_K 1.0
// Liquid LPG density, used to convert volumetric flow to a mass consumption rate.
#define LPG_DENSITY_KG_PER_L 0.51

#define ADC_MAX 4095.0
#define ADC_VREF 3.3

// How often to publish a reading
const unsigned long PUBLISH_INTERVAL_MS = 3000;

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long lastPublish = 0;
bool signupOK = false;
String deviceId;

String getDeviceId() {
  uint64_t chipId = ESP.getEfuseMac(); // unique per chip, stable across reboots/reflashes
  char buf[24];
  snprintf(buf, sizeof(buf), "gasguard-%012llx", chipId);
  return String(buf);
}

void connectWiFi() {
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(300);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("WiFi connected, IP: ");
  Serial.println(WiFi.localIP());
}

void syncTime() {
  // Needed so the "timestamp" field we write is meaningful - the app uses it to detect a
  // stale/dead sensor (Firestore, unlike Realtime Database, has no built-in onDisconnect hook).
  configTime(0, 0, "pool.ntp.org", "time.nist.gov");
  Serial.print("Syncing time");
  time_t now = time(nullptr);
  while (now < 8 * 3600 * 2) { // wait until we get a plausible epoch time
    delay(300);
    Serial.print(".");
    now = time(nullptr);
  }
  Serial.println();
}

void connectFirebase() {
  config.api_key = API_KEY;

  // Anonymous auth - same mechanism the Android app itself uses (see FirebaseAuthService.kt),
  // so no separate device credentials are needed. Requires the "Anonymous" sign-in provider
  // to be enabled in Firebase Console > Build > Authentication > Sign-in method.
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Firebase anonymous sign-up OK");
    signupOK = true;
  } else {
    Serial.printf("Firebase sign-up failed: %s\n", config.signer.signupError.message.c_str());
  }

  config.token_status_callback = tokenStatusCallback;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

// Converts a raw MQ-2 ADC reading into an approximate ppm value using the standard MQ-2
// clean-air-ratio curve. Simple by design - swap in your own curve fit from the datasheet
// if you need tighter accuracy for a specific gas.
double readPpm() {
  int adc = analogRead(MQ2_PIN);
  double voltage = (adc / ADC_MAX) * ADC_VREF;
  if (voltage <= 0.0) voltage = 0.01; // avoid divide-by-zero

  double rs = ((ADC_VREF * RL_VALUE) / voltage) - RL_VALUE; // sensor resistance, kOhm
  double ratio = rs / R0_CLEAN_AIR; // Rs/R0

  double ppm = pow(10, ((log10(ratio) - 0.38) / -0.42));
  if (ppm < 0 || isnan(ppm) || isinf(ppm)) ppm = 0;
  return ppm;
}

// Reads the MPXV7002DP and returns the differential pressure in kPa.
double readPressureKpa() {
  int adc = analogRead(MPX_PIN);
  double adcVoltage = (adc / ADC_MAX) * ADC_VREF;
  double sensorVoltage = adcVoltage * DIVIDER_RATIO; // undo the divider

  // Datasheet transfer function: Vout = Vs * (0.2 * P + 0.5)  =>  P = (Vout/Vs - 0.5) / 0.2
  double pressureKpa = ((sensorVoltage / MPX_VS) - 0.5) / 0.2;
  return pressureKpa;
}

// Rough flow-from-pressure model (flow scales with sqrt of differential pressure across a
// fixed restriction). FLOW_CALIBRATION_K absorbs everything about your specific plumbing -
// tune it against a real reference, don't trust the absolute number out of the box.
double pressureToFlowLpm(double pressureKpa) {
  double magnitude = fabs(pressureKpa);
  if (magnitude < 0.001) return 0.0;
  return FLOW_CALIBRATION_K * sqrt(magnitude);
}

double flowToConsumptionKgPerHour(double flowLpm) {
  // L/min -> L/hour -> kg/hour
  return flowLpm * 60.0 * LPG_DENSITY_KG_PER_L;
}

void setup() {
  Serial.begin(115200);
  analogReadResolution(12); // 0-4095

  deviceId = getDeviceId();
  Serial.println();
  Serial.println("========================================");
  Serial.print("  This meter's device ID: ");
  Serial.println(deviceId);
  Serial.println("  Enter this in the app's 'Pair your gas meter' screen.");
  Serial.println("========================================");

  connectWiFi();
  syncTime();
  connectFirebase();
  Serial.println("Warming up MQ-2 sensor (30-60s for stable readings)...");
}

void loop() {
  if (Firebase.ready() && signupOK && millis() - lastPublish > PUBLISH_INTERVAL_MS) {
    lastPublish = millis();

    double ppm = readPpm();
    double pressureKpa = readPressureKpa();
    double flowLpm = pressureToFlowLpm(pressureKpa);
    double consumptionKgPerHour = flowToConsumptionKgPerHour(flowLpm);
    long timestampMs = (long) time(nullptr) * 1000L;

    FirebaseJson content;
    content.set("fields/ppm/doubleValue", ppm);
    content.set("fields/pressureKpa/doubleValue", pressureKpa);
    content.set("fields/flowLpm/doubleValue", flowLpm);
    content.set("fields/consumptionKgPerHour/doubleValue", consumptionKgPerHour);
    content.set("fields/timestamp/integerValue", String(timestampMs));
    content.set("fields/deviceStatus/stringValue", "online");

    // patchDocument overwrites (or creates) device_telemetry/{deviceId} with exactly these
    // fields. Signature is (fbdo, projectId, databaseId, documentPath, content, updateMask) in
    // recent library versions - check the library's Firestore examples if this doesn't match
    // what you have installed.
    String documentPath = "device_telemetry/" + deviceId;
    if (Firebase.Firestore.patchDocument(
            &fbdo, FIRESTORE_PROJECT_ID, "", documentPath.c_str(),
            content.raw(),
            "ppm,pressureKpa,flowLpm,consumptionKgPerHour,timestamp,deviceStatus")) {
      Serial.printf("Published ppm=%.3f pressureKpa=%.3f flowLpm=%.3f kg/hr=%.3f\n",
                    ppm, pressureKpa, flowLpm, consumptionKgPerHour);
    } else {
      Serial.print("Publish failed: ");
      Serial.println(fbdo.errorReason());
    }
  }
}
