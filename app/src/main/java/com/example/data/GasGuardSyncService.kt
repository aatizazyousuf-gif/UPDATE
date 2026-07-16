package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * IMPORTANT (security): this project has no Firebase project configured out of the box
 * (there's no google-services.json), so none of this code runs until you add one - see README.
 *
 * Once configured, every call here requires a signed-in Firebase user (see
 * FirebaseAuthService - anonymous auth, still free). Personal data (clients, orders, chat,
 * leak readings) is written under `users/{uid}/...` instead of shared top-level collections,
 * so two different installs/testers never see or overwrite each other's data. Only
 * `safety_bulletins` stays a shared, top-level collection, since those are meant to be the
 * same broadcast notices for everyone.
 *
 * This still relies on you setting Firestore security rules that check `request.auth.uid`
 * matches the `{uid}` in the path - see firestore.rules.example in the project root. Without
 * those rules Firestore's defaults deny all access, which is safe but means sync will just
 * fail; it will never silently expose data.
 */
object GasGuardSyncService {

    /**
     * Backs up local Room data directly to Firebase Firestore, scoped under this user's uid.
     * Returns true if successful.
     */
    suspend fun backupData(
        uid: String,
        clients: List<Client>,
        orders: List<RefillOrder>,
        messages: List<ChatMessage>,
        leaks: List<LeakRecord>,
        consumption: List<ConsumptionRecord> = emptyList()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(uid)

            Log.d("GasGuardSyncService", "Starting Firebase Firestore database synchronization for uid=$uid...")

            // 1. Sync Clients collection
            for (c in clients) {
                val clientMap = hashMapOf(
                    "id" to c.id,
                    "name" to c.name,
                    "address" to c.address,
                    "fuelLevel" to c.fuelLevel,
                    "type" to c.type,
                    "lastDelivery" to c.lastDelivery,
                    "estRunout" to c.estRunout,
                    "isRefillRequested" to c.isRefillRequested,
                    "phone" to c.phone,
                    "status" to c.status
                )
                Tasks.await(
                    userDoc.collection("clients").document(c.id).set(clientMap),
                    8,
                    TimeUnit.SECONDS
                )
            }

            // 2. Sync Refill Orders collection
            for (o in orders) {
                val orderMap = hashMapOf(
                    "orderId" to o.orderId,
                    "date" to o.date,
                    "volume" to o.volume,
                    "cost" to o.cost,
                    "status" to o.status
                )
                Tasks.await(
                    userDoc.collection("refill_orders").document(o.orderId).set(orderMap),
                    8,
                    TimeUnit.SECONDS
                )
            }

            // 3. Sync Chat Messages collection
            for (m in messages) {
                val messageMap = hashMapOf(
                    "id" to m.id,
                    "sender" to m.sender,
                    "text" to m.text,
                    "timestamp" to m.timestamp,
                    "imageUrl" to (m.imageUrl ?: ""),
                    "isPendingAnalysis" to m.isPendingAnalysis
                )
                Tasks.await(
                    userDoc.collection("chat_messages").document(m.id.toString()).set(messageMap),
                    8,
                    TimeUnit.SECONDS
                )
            }

            // 4. Sync Gas Leak Telemetry Logs collection
            for (l in leaks) {
                val leakMap = hashMapOf(
                    "id" to l.id,
                    "timestamp" to l.timestamp,
                    "reading" to l.reading
                )
                Tasks.await(
                    userDoc.collection("leak_records").document(l.id.toString()).set(leakMap),
                    8,
                    TimeUnit.SECONDS
                )
            }

            // 5. Sync LPG consumption history (from the MPXV7002DP telemetry), also scoped to this user
            for (c in consumption) {
                val consumptionMap = hashMapOf(
                    "id" to c.id,
                    "timestamp" to c.timestamp,
                    "pressureKpa" to c.pressureKpa,
                    "flowLpm" to c.flowLpm,
                    "consumptionKgPerHour" to c.consumptionKgPerHour
                )
                Tasks.await(
                    userDoc.collection("consumption_records").document(c.id.toString()).set(consumptionMap),
                    8,
                    TimeUnit.SECONDS
                )
            }

            // 6. Store a snapshot/history event document for sync meta tracking, also scoped to this user
            val syncMeta = hashMapOf(
                "sync_timestamp" to System.currentTimeMillis(),
                "device_model" to android.os.Build.MODEL,
                "os_version" to android.os.Build.VERSION.RELEASE,
                "clients_count" to clients.size,
                "orders_count" to orders.size,
                "messages_count" to messages.size,
                "leaks_count" to leaks.size,
                "consumption_count" to consumption.size,
                "database_engine" to "Firebase Cloud Firestore"
            )
            Tasks.await(
                userDoc.collection("sync_history").document().set(syncMeta),
                8,
                TimeUnit.SECONDS
            )

            Log.d("GasGuardSyncService", "Firestore database sync successfully completed!")
            true
        } catch (e: Exception) {
            Log.e("GasGuardSyncService", "Error backing up data to Firebase Cloud Firestore database", e)
            false
        }
    }

    /**
     * Fetches safety updates directly from Firebase Firestore's shared 'safety_bulletins'
     * collection (same for every user - not scoped by uid). If empty, populates remote
     * database with default safety notices automatically.
     */
    suspend fun fetchSafetyBulletins(): List<SafetyBulletin> = withContext(Dispatchers.IO) {
        val bulletins = mutableListOf<SafetyBulletin>()
        try {
            val db = FirebaseFirestore.getInstance()
            val getTask = db.collection("safety_bulletins").get()
            val result = Tasks.await(getTask, 8, TimeUnit.SECONDS)

            if (result != null && !result.isEmpty) {
                for (doc in result.documents) {
                    val id = doc.getLong("id")?.toInt() ?: doc.id.hashCode()
                    val title = doc.getString("title") ?: ""
                    val body = doc.getString("body") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    bulletins.add(SafetyBulletin(id, title, body, timestamp))
                }
                Log.d("GasGuardSyncService", "Successfully retrieved safety bulletins from Firestore.")
            } else {
                Log.d("GasGuardSyncService", "Firestore bulletins collection empty. Seeding remote database...")
                val seededBulletins = listOf(
                    SafetyBulletin(
                        id = 301,
                        title = "Winter LPG Vaporization notice",
                        body = "LPG vaporization rates drop in freezing temperatures. Keep your cylinder shield properly insulated to maintain accurate pressure reads."
                    ),
                    SafetyBulletin(
                        id = 302,
                        title = "Vapor Lock & Overfill Prevention",
                        body = "LPG tanks should only be filled up to 80% capacity to allow safe liquid expansion. Avoid keeping cylinders in fully enclosed trunks or hot vehicles."
                    ),
                    SafetyBulletin(
                        id = 303,
                        title = "Standard 5-Year Regulator Recertification",
                        body = "Check your main high pressure stage 1 regulator. Standard codes require mandatory replacement or inspection every 5 years to prevent diaphragm dry rot."
                    )
                )

                // Seed the remote Firestore collection so it is populated for next queries
                for (b in seededBulletins) {
                    val bulletinMap = hashMapOf(
                        "id" to b.id,
                        "title" to b.title,
                        "body" to b.body,
                        "timestamp" to b.timestamp
                    )
                    db.collection("safety_bulletins").document(b.id.toString()).set(bulletinMap)
                }
                bulletins.addAll(seededBulletins)
            }
        } catch (e: Exception) {
            Log.e("GasGuardSyncService", "Error reading safety bulletins from Firestore, falling back to secure offline alerts", e)
            // Safety-first local fallback
            bulletins.addAll(
                listOf(
                    SafetyBulletin(
                        id = 401,
                        title = "Offline Safety Alert: LPG Storage",
                        body = "LPG is heavier than air and accumulates in low spots. Ensure your storage area has floor-level vents to prevent gas pooling."
                    ),
                    SafetyBulletin(
                        id = 402,
                        title = "Offline Safety Alert: Soap Leak Check",
                        body = "To trace physical gas leaks safely without flame, apply 10% dish-soap water solution to valve connections. Large growing bubbles denote active leaks."
                    )
                )
            )
        }
        bulletins
    }
}
