package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Handles Firebase Anonymous Authentication.
 *
 * Anonymous Auth is free (no quota/billing implications on Firebase's Spark plan) and gives
 * each app install a stable, unique user ID (uid) without ever asking the person for an email
 * or password. We use that uid to:
 *   1. Satisfy Firestore security rules that require `request.auth != null` (see README /
 *      firestore.rules.txt), so random people can't read or write your data.
 *   2. Scope each install's clients/orders/messages/leak-records under its own
 *      `users/{uid}/...` path in Firestore, so two different phones/testers never see or
 *      overwrite each other's data.
 *
 * This is separate from the in-app "Sign In" screen (which is just a local UI gate) - this
 * auth happens silently in the background purely so cloud sync has a safe identity to write
 * under.
 */
object FirebaseAuthService {

    /**
     * Ensures there is a signed-in Firebase user and returns their uid.
     * Returns null if Firebase isn't configured yet (no google-services.json) or sign-in fails
     * for any reason - callers should treat that as "cloud sync unavailable right now."
     */
    suspend fun ensureSignedInUid(context: Context): String? = withContext(Dispatchers.IO) {
        if (FirebaseApp.getApps(context.applicationContext).isEmpty()) {
            // No google-services.json configured - nothing to sign into.
            return@withContext null
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val existingUser = auth.currentUser
            if (existingUser != null) {
                return@withContext existingUser.uid
            }

            val result = Tasks.await(auth.signInAnonymously(), 10, TimeUnit.SECONDS)
            val uid = result.user?.uid
            Log.d("FirebaseAuthService", "Signed in anonymously: $uid")
            uid
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Anonymous sign-in failed", e)
            null
        }
    }
}
