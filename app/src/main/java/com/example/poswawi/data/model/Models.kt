package com.example.poswawi.data.model

import kotlinx.serialization.Serializable
import java.time.Instant

enum class TaxRate(val percentage: Double) {
    REDUCED(7.0),
    FULL(19.0)
}

enum class ProductCategory {
    COFFEE,
    FOOD,
    DRINK,
    MERCHANDISE
}

enum class PaymentMethod {
    CASH,
    CARD,
    WALLET
}

enum class EmployeeRole {
    ADMIN,
    CASHIER,
    TEMP
}

@Serializable
data class Product(
    val id: Long,
    val name: String,
    val price: Double,
    val taxRate: TaxRate,
    val category: ProductCategory,
    val imageUri: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class InventoryItem(
    val productId: Long,
    val quantity: Int,
    val reorderLevel: Int
)

@Serializable
data class InventorySnapshot(
    val product: Product,
    val inventory: InventoryItem
)

@Serializable
data class SaleItem(
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: TaxRate
)

@Serializable
data class Discount(
    val type: DiscountType,
    val value: Double
)

enum class DiscountType {
    PERCENT,
    AMOUNT
}

@Serializable
data class Sale(
    val id: Long,
    val items: List<SaleItem>,
    val employeeId: Long,
    val timestamp: Instant,
    val paymentMethod: PaymentMethod,
    val discount: Discount? = null,
    val customerId: Long? = null,
    val isParked: Boolean = false,
    val isSynced: Boolean = true
)

@Serializable
data class Employee(
    val id: Long,
    val name: String,
    val pin: String,
    val role: EmployeeRole,
    val hourlyRate: Double = 0.0
)

@Serializable
data class EmployeeShift(
    val id: Long,
    val employeeId: Long,
    val clockIn: Instant,
    val clockOut: Instant? = null
)

@Serializable
data class Customer(
    val id: Long,
    val name: String,
    val loyaltyPoints: Int = 0,
    val discountPercent: Double = 0.0
)

@Serializable
data class Purchase(
    val id: Long,
    val productId: Long,
    val quantity: Int,
    val supplier: String,
    val costPerUnit: Double,
    val timestamp: Instant
)

@Serializable
data class AuditLog(
    val id: Long,
    val employeeId: Long,
    val timestamp: Instant,
    val action: String,
    val reason: String
)

@Serializable
data class ReportSummary(
    val totalRevenue: Double,
    val totalVat: Double,
    val totalDiscounts: Double,
    val topProducts: List<ProductStatistic>,
    val employeePerformance: List<EmployeeStatistic>
)

@Serializable
data class ProductStatistic(
    val product: Product,
    val quantitySold: Int,
    val revenue: Double
)

@Serializable
data class EmployeeStatistic(
    val employee: Employee,
    val salesCount: Int,
    val revenue: Double
)
