package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Live telemetry pushed up by the ESP32:
 *  - `ppm` from the MQ-2 gas sensor
 *  - `pressureKpa` / `flowLpm` / `consumptionKgPerHour` derived from the MPXV7002DP
 *    differential pressure sensor (see firmware/esp32_mq2_firebase for the on-device math)
 *
 * `timestamp` is the device's own clock (millis since epoch, synced via NTP) at the moment it
 * took the reading - used here purely to detect staleness. Unlike Realtime Database, Firestore
 * has no server-side "onDisconnect" hook, so a dead/powered-off ESP32 is detected only by its
 * last write going stale, not by an explicit flag.
 */
data class SensorReading(
    val ppm: Double,
    val pressureKpa: Double,
    val flowLpm: Double,
    val consumptionKgPerHour: Double,
    val timestamp: Long,
    val deviceStatus: String // "online", written by the ESP32 on every publish
)

/**
 * Reads real-time sensor telemetry from Cloud Firestore, written there by a homeowner's own
 * ESP32 (see /firmware/esp32_mq2_firebase/esp32_mq2_firebase.ino in the project root).
 *
 * Expected Firestore layout (see firestore.rules.example for the security rules):
 *
 *   device_telemetry/{deviceId}   <- one document per physical ESP32, keyed by its stable
 *                                    chip ID (printed on boot, see firmware)
 *     ppm: 0.023
 *     pressureKpa: 0.84
 *     flowLpm: 1.2
 *     consumptionKgPerHour: 0.09
 *     timestamp: 1737000000000
 *     deviceStatus: "online"
 *
 * This is keyed by device, not by user uid, because the ESP32 can't authenticate as a
 * specific homeowner (see DevicePairingService.kt for how a homeowner's account gets linked
 * to their own deviceId, and how the security rules use that pairing to keep everyone's
 * readings private to them).
 *
 * If `google-services.json` hasn't been added yet, this fails closed: callers just never
 * receive a reading (isSensorConfigured() lets the UI show a clear "not connected" state
 * instead of a fake one).
 */
object SensorRepository {

    private const val TAG = "SensorRepository"
    private const val COLLECTION = "device_telemetry"

    /** How long a reading is trusted before we consider the ESP32 gone quiet. */
    private const val STALE_AFTER_MS = 20_000L

    fun isSensorConfigured(context: android.content.Context): Boolean =
        com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()

    /**
     * Emits a new [SensorReading] every time the paired ESP32 overwrites its
     * `device_telemetry/{deviceId}` document in Firestore. Uses a real-time snapshot listener
     * (not polling), so it fires within roughly a second of the device's write reaching the
     * server on a normal connection.
     */
    fun observeReadings(deviceId: String): Flow<SensorReading> = callbackFlow {
        val docRef = FirebaseFirestore.getInstance().collection(COLLECTION).document(deviceId)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Firestore listener for $COLLECTION/$deviceId failed", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) {
                // Nothing published yet (device hasn't connected once, or deviceId is wrong).
                return@addSnapshotListener
            }

            val ppm = snapshot.getDouble("ppm") ?: return@addSnapshotListener
            val pressureKpa = snapshot.getDouble("pressureKpa") ?: 0.0
            val flowLpm = snapshot.getDouble("flowLpm") ?: 0.0
            val consumptionKgPerHour = snapshot.getDouble("consumptionKgPerHour") ?: 0.0
            val timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis()
            val status = snapshot.getString("deviceStatus") ?: "unknown"

            trySend(
                SensorReading(
                    ppm = ppm,
                    pressureKpa = pressureKpa,
                    flowLpm = flowLpm,
                    consumptionKgPerHour = consumptionKgPerHour,
                    timestamp = timestamp,
                    deviceStatus = status
                )
            )
        }

        awaitClose { registration.remove() }
    }

    /** True once a reading has arrived recently enough to trust. */
    fun isLive(reading: SensorReading?): Boolean {
        if (reading == null) return false
        val age = System.currentTimeMillis() - reading.timestamp
        // Small negative ages are tolerated (ESP32 clock via NTP can drift a couple seconds
        // ahead of the phone's clock); only genuinely stale or wildly-off readings fail this.
        return age in -5_000..STALE_AFTER_MS
    }
}
