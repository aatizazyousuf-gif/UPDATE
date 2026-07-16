<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/aea12662-f43a-4619-a2d1-3c459ca47caf

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. (Optional, still free) Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to a key from https://aistudio.google.com/apikey — see `.env.example`.
   Get the key from **AI Studio**, not the Cloud Console, and never enable billing on that project. Flash models are free on the AI Studio tier, and the app also caps itself at 40 AI calls/day as a safety net (see `GeminiService.kt`). If you skip this step entirely, the tech-support chat still works using built-in offline responses at no cost.
5. Run the app on an emulator or physical device — no other setup required. The project automatically signs debug builds with Android Studio's own auto-generated debug keystore, and release builds fall back to the same free keystore until you create a real one for the Play Store (see `KEYSTORE_PATH`/`STORE_PASSWORD`/`KEY_PASSWORD` in `app/build.gradle.kts`).

## Cloud sync with Firebase (optional, free on the Spark plan)

The "Sync with backend" button backs up your local data (clients, orders, chat, leak readings)
to Firestore and lets suppliers pull shared safety bulletins. It's fully optional — the app
works completely offline without it — and every piece below is free with no billing required.

1. Go to https://console.firebase.google.com, click **Add project**, and create a project
   (you can decline Google Analytics, it isn't needed here).
2. Inside the project, click **Add app > Android**, and register it with the applicationId
   from `app/build.gradle.kts` (currently `com.aistudio.gasmonitor.wtpnbq`).
3. Download the generated `google-services.json` and place it at `app/google-services.json`
   (same folder as `app/build.gradle.kts`). Without this file, cloud sync stays disabled and
   the app just tells you so when you tap Sync — nothing breaks.
4. In the Firebase console, go to **Build > Firestore Database > Create database**, and start
   it in **production mode** (not test mode — test mode allows anyone to read/write for 30
   days, which you don't want).
5. Go to **Build > Authentication > Get started**, and enable the **Anonymous** sign-in
   provider. This lets the app identify each install without ever asking for an email/password
   — it's a separate thing from the app's own "Sign In" screen.
6. Go to **Firestore Database > Rules**, paste in the contents of `firestore.rules.example`
   from this project, and click **Publish**. This is what keeps one install's data private
   from every other install's data.
7. Rebuild and run the app, then tap **Sync with backend**. It will sign in anonymously in the
   background, then write your data under `users/{your-device's-uid}/...` in Firestore.

You can check everything landed correctly in the Firebase console under
**Firestore Database > Data**.

## Live gas + consumption sensors (ESP32 + MQ-2 + MPXV7002DP over Firestore)

The app no longer simulates any sensor data, and **every homeowner's gas meter is kept
separate from everyone else's** - two different installs never see each other's readings.

### How the data is actually stored

- `device_telemetry/{deviceId}` - one Firestore document **per physical ESP32**, keyed by an
  ID derived from that chip's own MAC address (so it's unique automatically, no manual setup).
  This holds only the live/current reading (ppm, pressure, flow, consumption rate, timestamp).
- `users/{uid}/profile/main` - each homeowner's own account has a `pairedDeviceId` field
  linking them to their specific meter. This is set once, from the app, when they pair.
- `users/{uid}/leak_records` and `users/{uid}/consumption_records` - each homeowner's own
  *history* of readings (synced up from the local Room database via "Sync with backend"),
  same pattern as their clients/orders/chat history.

**Why the live feed isn't nested under `users/{uid}/...` directly:** the ESP32 can't
authenticate as a specific homeowner - it only has its own anonymous Firebase identity, not
the homeowner's. Making it impersonate a specific uid would need a server-side step (a Cloud
Function minting a custom token), which requires a paid Firebase plan. Instead, the security
rules use the `pairedDeviceId` link to make sure a homeowner can only ever *read* their own
device's document - see the `device_telemetry/{deviceId}` rule in `firestore.rules.example`
for exactly how that's enforced (it's a `get()` lookup against their own profile doc).

One tradeoff worth knowing: *write* access to `device_telemetry/{deviceId}` is open to any
signed-in (anonymous) device, since there's no way to lock it to one specific ESP32 without
that same server-side step. Device IDs derived from a chip's MAC address aren't practically
guessable, but this isn't cryptographic-grade security - fine for a personal/home project,
worth revisiting (with a Cloud Function) if you ever have many unrelated households on it.

### Setup

**1. You already have a Firestore database - just add these two things to it:**
- **Anonymous sign-in**: **Build > Authentication > Sign-in method > Anonymous** (enable it,
  if you haven't for cloud sync already). The ESP32 signs in the same anonymous way the app
  does - no separate device credentials needed.
- **Rules**: go to **Firestore Database > Rules**, paste in the contents of
  `firestore.rules.example` from this project, and click **Publish**.

**2. Flash the ESP32**
Open `firmware/esp32_mq2_firebase/esp32_mq2_firebase.ino` in the Arduino IDE.
- Install the **Firebase ESP Client** library (by mobizt) via Library Manager - it talks to
  both Realtime Database and Firestore; this sketch uses its Firestore functions.
- Fill in your WiFi SSID/password, your Firebase Web API key, and your Firebase Project ID
  (both under **Project settings > General** in the console).
- Wire the **MQ-2**: VCC to 5V, GND to GND, AOUT to GPIO34.
- Wire the **MPXV7002DP**: it outputs 0.5V-4.5V (centered at 2.5V) from a 5V supply, which is
  above the ESP32's 3.3V ADC limit, so add a simple voltage divider (two 10k resistors) between
  its Vout and GPIO35. See the wiring comment at the top of the sketch for the exact hookup.
- Flash it, then open the Serial Monitor (115200 baud). On first boot it prints this device's
  ID in a boxed banner - copy it down, you'll need it in the next step.
- The MQ-2 needs ~30-60 seconds to warm up before readings settle.

**3. Pair it to your account, in the app**
On the Home tab, tap **"Safety Check / ESP32 Setup"** - this opens a 4-step setup wizard.
Step 1 is where you pair: paste in the device ID from step 2 above and tap **"Pair this
device"**. From that point on, this app install (and only this one) will read that specific
device's live feed. The remaining steps (calibration reminders, mounting checklist, and a
live system test showing your real readings) walk you through the rest of the physical setup.
If you ever need to switch to a different meter, there's a "Pair a different device" link on
that same step.

**4. Calibrate (important - the defaults are placeholders, not real physics)**
- `R0_CLEAN_AIR` for the MQ-2 - run once in clean air and hardcode the value the sketch prints.
- `FLOW_CALIBRATION_K` and `LPG_DENSITY_KG_PER_L` for the consumption estimate - the
  pressure-to-flow-to-mass conversion depends entirely on your specific tubing/orifice and
  whether you're tapping liquid or vapor phase. The simplest way to calibrate: run the burner
  for a known time, weigh the cylinder before/after, and adjust `FLOW_CALIBRATION_K` until the
  app's estimated consumption matches the real weight loss.
- `TANK_CAPACITY_KG` in `GasGuardViewModel.kt` - set this to your actual cylinder size (default
  is 14.2kg).

**5. Run the app**
Once paired and the ESP32 is publishing, the Home tab's status pill will read "MQ-2 sensor
live", the consumption chart on the Analytics tab will start filling in with real samples, and
"days remaining" will update based on the real consumption rate rather than a fixed number. If
it says "offline", check the ESP32's Serial Monitor for connection errors and confirm both the
app and the ESP32 are pointed at the same Firebase project. Note that this is one meter per
app install - if a homeowner reinstalls the app or moves to a new phone, they'll need to
re-pair (they get a new anonymous uid) unless you later add real account login on top of the
current anonymous auth.

## Building the APK on GitHub

This project includes a ready-to-go GitHub Actions workflow at
`.github/workflows/build.yml` that builds a debug APK for you — no local
Android Studio setup required, and it's free (GitHub Actions gives every
public repo, and every private repo on the free plan, a monthly quota of
build minutes at no cost).

1. Create a new repository on GitHub and push this project to it:
   ```
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
2. Go to your repository's **Actions** tab. The **Build APK** workflow runs
   automatically on every push to `main` (you can also click **Run workflow**
   to trigger it manually).
3. Once it finishes (usually a few minutes), open the completed run and
   download the **gas-monitor-debug-apk** artifact from the **Artifacts**
   section at the bottom of the page. Unzip it to get `app-debug.apk`.
4. Copy that APK to your Android phone and open it to install (you'll need to
   allow "install from unknown sources" the first time) — or drag it onto a
   running emulator.

**Optional secrets** (only needed if you want the real Gemini API or Firebase
cloud sync working in the built APK — the app works fine without either):
go to your repo's **Settings > Secrets and variables > Actions** and add:
- `GEMINI_API_KEY` — your key from https://aistudio.google.com/apikey
- `GOOGLE_SERVICES_JSON` — the full text contents of your `app/google-services.json`
  file (see the Firebase section above)

Without these secrets, the workflow still succeeds — the app just uses its
built-in offline chat responses and shows cloud sync as "not set up yet,"
exactly like it does when built locally without those files.

**Note:** this workflow builds a **debug** APK, which is fine for installing
on your own device or sharing with testers, but isn't signed for the Play
Store. If you want a **release** APK/AAB signed for Play Store submission,
you'd add a real keystore as a base64-encoded secret and change the workflow's
build step to `./gradlew assembleRelease` (or `bundleRelease` for an AAB) —
let me know if you'd like that set up too.
