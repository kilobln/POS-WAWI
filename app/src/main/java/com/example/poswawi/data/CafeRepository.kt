package com.example.poswawi.data

import com.example.poswawi.data.model.AuditLog
import com.example.poswawi.data.model.Customer
import com.example.poswawi.data.model.Employee
import com.example.poswawi.data.model.EmployeeShift
import com.example.poswawi.data.model.InventorySnapshot
import com.example.poswawi.data.model.PaymentMethod
import com.example.poswawi.data.model.Product
import com.example.poswawi.data.model.ProductCategory
import com.example.poswawi.data.model.Purchase
import com.example.poswawi.data.model.ReportSummary
import com.example.poswawi.data.model.Sale
import com.example.poswawi.data.model.SaleItem
import com.example.poswawi.data.model.TaxRate
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface CafeRepository {
    val products: Flow<List<Product>>
    val inventory: Flow<List<InventorySnapshot>>
    val employees: Flow<List<Employee>>
    val customers: Flow<List<Customer>>
    val openShifts: Flow<List<EmployeeShift>>
    val sales: Flow<List<Sale>>
    val purchases: Flow<List<Purchase>>
    val auditLogs: Flow<List<AuditLog>>
    val parkedSales: Flow<List<Sale>>

    suspend fun addProduct(
        name: String,
        price: Double,
        taxRate: TaxRate,
        category: ProductCategory,
        imageUri: String? = null,
        reorderLevel: Int = 5
    )

    suspend fun adjustInventory(productId: Long, delta: Int)

    suspend fun recordSale(
        items: List<SaleItem>,
        employeeId: Long,
        paymentMethod: PaymentMethod,
        discountPercent: Double = 0.0,
        customerId: Long? = null,
        markAsParked: Boolean = false,
        note: String? = null
    )

    suspend fun parkOrder(name: String, items: List<SaleItem>, employeeId: Long)

    suspend fun resumeParkedOrder(orderId: Long)

    suspend fun deleteParkedOrder(orderId: Long)

    suspend fun addEmployee(name: String, pin: String, role: com.example.poswawi.data.model.EmployeeRole)

    suspend fun clockIn(employeeId: Long)

    suspend fun clockOut(shiftId: Long)

    suspend fun addCustomer(name: String, discountPercent: Double)

    suspend fun addPurchase(productId: Long, quantity: Int, supplier: String, costPerUnit: Double)

    suspend fun createAuditLog(employeeId: Long, action: String, reason: String)

    suspend fun computeReport(from: Instant, to: Instant): ReportSummary
}
