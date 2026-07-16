package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(private val db: AppDatabase) {

    val allMessages: Flow<List<ChatMessage>> = db.chatDao().getAllMessages()
    val allOrders: Flow<List<RefillOrder>> = db.refillOrderDao().getAllOrders()
    val allClients: Flow<List<Client>> = db.clientDao().getAllClients()
    val latestLeaks: Flow<List<LeakRecord>> = db.leakRecordDao().getLatestRecords()
    val allBulletins: Flow<List<SafetyBulletin>> = db.safetyBulletinDao().getAllBulletins()
    val latestConsumption: Flow<List<ConsumptionRecord>> = db.consumptionRecordDao().getLatestRecords()

    suspend fun populateDefaultsIfNeeded() {
        // Populate safety bulletins if empty
        val currentBulletins = db.safetyBulletinDao().getAllBulletins().first()
        if (currentBulletins.isEmpty()) {
            db.safetyBulletinDao().insertBulletins(
                listOf(
                    SafetyBulletin(
                        id = 101,
                        title = "Winter Gas Flow Regulation Notice",
                        body = "LPG vapor pressure decreases in colder temperatures. Keep your regulator protected from moisture and ice buildup to ensure consistent gas delivery."
                    ),
                    SafetyBulletin(
                        id = 102,
                        title = "Regulator Vent Orientation",
                        body = "To prevent water and dust ingress, ensure the regulator vent is oriented vertically downward and is situated at least 30cm above the ground."
                    ),
                    SafetyBulletin(
                        id = 103,
                        title = "Recognizing Gas Odorants (Mercaptan)",
                        body = "LPG is naturally odorless but scented with Ethyl Mercaptan (smells like sulfur or rotten eggs). If you smell this, check your MQ-2 sensor reading on the Home tab immediately and ventilate the area."
                    )
                )
            )
        }

        // Populate orders if empty
        val currentOrders = db.refillOrderDao().getAllOrders().first()
        if (currentOrders.isEmpty()) {
            db.refillOrderDao().insertOrders(
                listOf(
                    RefillOrder("ORDER #VG-9921", "Oct 12, 2023", 42.5, 158.40, "SUCCESS", clientId = "2"),
                    RefillOrder("ORDER #VG-8842", "Sep 04, 2023", 38.0, 142.12, "SUCCESS", clientId = "4"),
                    RefillOrder("ORDER #VG-7712", "Aug 02, 2023", 45.2, 164.88, "SUCCESS", clientId = "1"),
                    RefillOrder("ORDER #VG-6605", "Jul 05, 2023", 0.0, 0.00, "CANCELED", clientId = "3")
                )
            )
        }

        // Populate clients if empty
        val currentClients = db.clientDao().getAllClients().first()
        if (currentClients.isEmpty()) {
            db.clientDao().insertClients(
                listOf(
                    Client("1", "Apex Alloys Ltd.", "Industrial Park Zone B", 0.12, "INDUSTRIAL", "Oct 12, 2023", "Jan 20, 2024", true, "+1-555-0192"),
                    Client("2", "Sarah Jenkins", "442 Oak Avenue", 0.78, "RESIDENTIAL", "Oct 12, 2023", "Jan 20, 2024", false, "+1-555-0143"),
                    Client("3", "David Miller", "12 Maple St.", 0.24, "RESIDENTIAL", "Sep 15, 2023", "Feb 10, 2024", true, "+1-555-0188"),
                    Client("4", "GigaGlass Works", "Port Terminal A-9", 0.92, "INDUSTRIAL", "Oct 01, 2023", "Mar 15, 2024", false, "+1-555-0177")
                )
            )
        }

        // Populate leak records if empty
        val currentLeaks = db.leakRecordDao().getLatestRecords().first()
        if (currentLeaks.isEmpty()) {
            db.leakRecordDao().insertRecord(LeakRecord(reading = 0.0))
            db.leakRecordDao().insertRecord(LeakRecord(reading = 0.0))
            db.leakRecordDao().insertRecord(LeakRecord(reading = 0.0))
            db.leakRecordDao().insertRecord(LeakRecord(reading = 0.0))
            db.leakRecordDao().insertRecord(LeakRecord(reading = 0.0))
        }

        // Populate chat messages if empty
        val currentMessages = db.chatDao().getAllMessages().first()
        if (currentMessages.isEmpty()) {
            db.chatDao().insertMessage(
                ChatMessage(
                    sender = "agent",
                    text = "Sure, here is how the sensor is mounted on the primary valve. It seems the indicator light is pulsing orange.",
                    imageUrl = "sensor_setup_valve_01.jpg",
                    timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
                )
            )
            db.chatDao().insertMessage(
                ChatMessage(
                    sender = "user",
                    text = "Analyzing image for sensor alignment...",
                    timestamp = System.currentTimeMillis() - 1800000 // 30 mins ago
                )
            )
            db.chatDao().insertMessage(
                ChatMessage(
                    sender = "agent",
                    text = "Got it. The orange pulse indicates a low battery or signal interference. Please try rotating the sensor 45 degrees.",
                    timestamp = System.currentTimeMillis() - 900000 // 15 mins ago
                )
            )
        }
    }

    // Chat operations
    suspend fun sendMessage(sender: String, text: String, imageUrl: String? = null): Long {
        return db.chatDao().insertMessage(
            ChatMessage(sender = sender, text = text, imageUrl = imageUrl)
        )
    }

    suspend fun clearChat() {
        db.chatDao().clearChat()
    }

    // Refill operations
    suspend fun placeRefillOrder(orderId: String, volume: Double, cost: Double, clientId: String = ""): RefillOrder {
        val order = RefillOrder(
            orderId = orderId,
            date = "Today",
            volume = volume,
            cost = cost,
            status = "PENDING",
            clientId = clientId
        )
        db.refillOrderDao().insertOrder(order)
        return order
    }

    // Client operations
    suspend fun updateClientFuel(id: String, level: Double) {
        db.clientDao().updateClientFuelLevel(id, level)
    }

    suspend fun updateClientRefill(id: String, requested: Boolean) {
        db.clientDao().updateClientRefillRequest(id, requested)
    }

    suspend fun insertClient(client: Client) {
        db.clientDao().insertClient(client)
    }

    // Leak operations
    suspend fun recordLeakReading(reading: Double) {
        db.leakRecordDao().insertRecord(LeakRecord(reading = reading))
    }

    // Consumption operations (from the MPXV7002DP flow/pressure telemetry)
    suspend fun recordConsumption(pressureKpa: Double, flowLpm: Double, consumptionKgPerHour: Double) {
        db.consumptionRecordDao().insertRecord(
            ConsumptionRecord(
                pressureKpa = pressureKpa,
                flowLpm = flowLpm,
                consumptionKgPerHour = consumptionKgPerHour
            )
        )
    }

    // Safety Bulletin operations
    suspend fun insertBulletins(bulletins: List<SafetyBulletin>) {
        db.safetyBulletinDao().insertBulletins(bulletins)
    }

    suspend fun clearBulletins() {
        db.safetyBulletinDao().clearBulletins()
    }
}
