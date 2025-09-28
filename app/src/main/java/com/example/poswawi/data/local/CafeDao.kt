package com.example.poswawi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CafeDao {
    @Query("SELECT * FROM products WHERE isActive = 1")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduct(product: ProductEntity): Long

    @Query("SELECT * FROM inventory")
    fun observeInventory(): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(item: InventoryEntity): Long

    @Query("UPDATE inventory SET quantity = quantity + :delta WHERE productId = :productId")
    suspend fun adjustStock(productId: Long, delta: Int)

    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun observeSales(): Flow<List<SaleWithItems>>

    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteSaleItemsBySaleId(saleId: Long)

    @Query("SELECT * FROM employees")
    fun observeEmployees(): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmployee(employee: EmployeeEntity): Long

    @Query("SELECT * FROM employee_shifts WHERE clockOut IS NULL")
    fun observeOpenShifts(): Flow<List<EmployeeShiftEntity>>

    @Insert
    suspend fun insertShift(shift: EmployeeShiftEntity): Long

    @Query("SELECT * FROM employee_shifts WHERE id = :id")
    suspend fun getShiftById(id: Long): EmployeeShiftEntity?

    @Update
    suspend fun updateShift(shift: EmployeeShiftEntity)

    @Query("SELECT * FROM customers")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(customer: CustomerEntity): Long

    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun observePurchases(): Flow<List<PurchaseEntity>>

    @Insert
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun observeAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM sales WHERE isParked = 1")
    fun observeParkedSales(): Flow<List<SaleEntity>>

    @Insert
    suspend fun insertParkedOrder(order: ParkedOrderEntity): Long

    @Query("SELECT * FROM parked_orders WHERE id = :id")
    suspend fun getParkedOrder(id: Long): ParkedOrderEntity?

    @Insert
    suspend fun insertParkedOrderItems(items: List<ParkedOrderItemEntity>)

    @Transaction
    @Query("SELECT * FROM parked_orders")
    fun observeParkedOrdersWithItems(): Flow<List<ParkedOrderWithItems>>

    @Query("SELECT * FROM parked_orders")
    fun observeParkedOrders(): Flow<List<ParkedOrderEntity>>

    @Query("SELECT * FROM parked_order_items WHERE parkedOrderId = :id")
    suspend fun getParkedOrderItems(id: Long): List<ParkedOrderItemEntity>

    @Query("DELETE FROM parked_orders WHERE id = :id")
    suspend fun deleteParkedOrder(id: Long)

    @Query("DELETE FROM parked_order_items WHERE parkedOrderId = :id")
    suspend fun deleteParkedOrderItems(id: Long)
}
