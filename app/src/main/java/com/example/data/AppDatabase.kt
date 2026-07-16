package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Dao
interface RefillOrderDao {
    @Query("SELECT * FROM refill_orders ORDER BY orderId DESC")
    fun getAllOrders(): Flow<List<RefillOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: RefillOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<RefillOrder>)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<Client>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client)

    @Query("UPDATE clients SET fuelLevel = :fuelLevel WHERE id = :id")
    suspend fun updateClientFuelLevel(id: String, fuelLevel: Double)

    @Query("UPDATE clients SET isRefillRequested = :requested WHERE id = :id")
    suspend fun updateClientRefillRequest(id: String, requested: Boolean)
}

@Dao
interface LeakRecordDao {
    @Query("SELECT * FROM leak_records ORDER BY timestamp DESC LIMIT 30")
    fun getLatestRecords(): Flow<List<LeakRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LeakRecord)
}

@Dao
interface ConsumptionRecordDao {
    @Query("SELECT * FROM consumption_records ORDER BY timestamp DESC LIMIT 30")
    fun getLatestRecords(): Flow<List<ConsumptionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ConsumptionRecord)
}

@Dao
interface SafetyBulletinDao {
    @Query("SELECT * FROM safety_bulletins ORDER BY id ASC")
    fun getAllBulletins(): Flow<List<SafetyBulletin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulletins(bulletins: List<SafetyBulletin>)

    @Query("DELETE FROM safety_bulletins")
    suspend fun clearBulletins()
}

@Database(
    entities = [ChatMessage::class, RefillOrder::class, Client::class, LeakRecord::class, SafetyBulletin::class, ConsumptionRecord::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun refillOrderDao(): RefillOrderDao
    abstract fun clientDao(): ClientDao
    abstract fun leakRecordDao(): LeakRecordDao
    abstract fun safetyBulletinDao(): SafetyBulletinDao
    abstract fun consumptionRecordDao(): ConsumptionRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gasguard_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
