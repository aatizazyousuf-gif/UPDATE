package com.example.data

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Links a homeowner's account (their Firebase Auth uid - see FirebaseAuthService) to the
 * specific ESP32 meter that belongs to them, so two different homeowners' installs never see
 * each other's gas readings even though the readings themselves live in a shared
 * `device_telemetry/{deviceId}` collection (see SensorRepository.kt for why that collection
 * can't be nested under `users/{uid}/...` directly).
 *
 * The pairing itself IS stored under this user's own uid-scoped path
 * (`users/{uid}/profile/main`), same as their clients/orders/chat history, and that's also
 * what the Firestore security rules check to decide whether this uid is allowed to read a
 * given device's telemetry.
 */
object DevicePairingService {

    private const val TAG = "DevicePairingService"

    suspend fun getPairedDeviceId(uid: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Tasks.await(
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("profile").document("main")
                    .get(),
                8, TimeUnit.SECONDS
            )
            doc.getString("pairedDeviceId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read paired device id", e)
            null
        }
    }

    suspend fun setPairedDeviceId(uid: String, deviceId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Tasks.await(
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("profile").document("main")
                    .set(mapOf("pairedDeviceId" to deviceId.trim())),
                8, TimeUnit.SECONDS
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save paired device id", e)
            false
        }
    }

    suspend fun clearPairedDeviceId(uid: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Tasks.await(
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("profile").document("main")
                    .set(mapOf("pairedDeviceId" to null)),
                8, TimeUnit.SECONDS
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear paired device id", e)
            false
        }
    }
}
