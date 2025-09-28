package com.example.poswawi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ProductEntity::class,
        InventoryEntity::class,
        EmployeeEntity::class,
        EmployeeShiftEntity::class,
        CustomerEntity::class,
        PurchaseEntity::class,
        AuditLogEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ParkedOrderEntity::class,
        ParkedOrderItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CafeDatabase : RoomDatabase() {
    abstract val dao: CafeDao

    companion object {
        @Volatile
        private var INSTANCE: CafeDatabase? = null

        fun getInstance(context: Context): CafeDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): CafeDatabase =
            Room.databaseBuilder(context, CafeDatabase::class.java, "cafe-pos.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
