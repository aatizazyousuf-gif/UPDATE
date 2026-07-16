package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "agent"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val isPendingAnalysis: Boolean = false
)

@Entity(tableName = "refill_orders")
data class RefillOrder(
    @PrimaryKey val orderId: String,
    val date: String,
    val volume: Double, // in Gallons, 0.0 means cancelled/not recorded
    val cost: Double, // in USD
    val status: String, // "SUCCESS", "CANCELED", "PENDING"
    val clientId: String = "" // links to Client.id on the supplier side; blank = a homeowner's own order
)

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val fuelLevel: Double, // 0.0 to 1.0 (e.g., 0.12 for 12%)
    val type: String, // "RESIDENTIAL" or "INDUSTRIAL"
    val lastDelivery: String,
    val estRunout: String,
    val isRefillRequested: Boolean,
    val phone: String,
    val status: String = "ACTIVE"
)

@Entity(tableName = "leak_records")
data class LeakRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val reading: Double // ppm
)

@Entity(tableName = "consumption_records")
data class ConsumptionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pressureKpa: Double,     // raw MPXV7002DP differential pressure reading
    val flowLpm: Double,         // derived volumetric flow rate, liters/min
    val consumptionKgPerHour: Double // derived mass consumption rate
)

@Entity(tableName = "safety_bulletins")
data class SafetyBulletin(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

