package com.example.poswawi.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.poswawi.data.model.DiscountType
import com.example.poswawi.data.model.EmployeeRole
import com.example.poswawi.data.model.PaymentMethod
import com.example.poswawi.data.model.ProductCategory
import com.example.poswawi.data.model.TaxRate
import java.time.Instant

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val taxRate: TaxRate,
    val category: ProductCategory,
    val imageUri: String? = null,
    val isActive: Boolean = true
)

@Entity(
    tableName = "inventory",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"], unique = true)]
)
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val quantity: Int,
    val reorderLevel: Int
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pin: String,
    val role: EmployeeRole,
    val hourlyRate: Double
)

@Entity(
    tableName = "employee_shifts",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId")]
)
data class EmployeeShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val clockIn: Instant,
    val clockOut: Instant? = null
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val loyaltyPoints: Int,
    val discountPercent: Double
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val quantity: Int,
    val supplier: String,
    val costPerUnit: Double,
    val timestamp: Instant
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val timestamp: Instant,
    val action: String,
    val reason: String
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val timestamp: Instant,
    val paymentMethod: PaymentMethod,
    val discountType: DiscountType? = null,
    val discountValue: Double? = null,
    val customerId: Long? = null,
    val isParked: Boolean = false,
    val isSynced: Boolean = true
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId"), Index("productId")]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: TaxRate
)

@Entity(tableName = "parked_orders")
data class ParkedOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Instant,
    val name: String,
    val employeeId: Long
)

@Entity(
    tableName = "parked_order_items",
    foreignKeys = [
        ForeignKey(
            entity = ParkedOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parkedOrderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parkedOrderId"), Index("productId")]
)
data class ParkedOrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parkedOrderId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val taxRate: TaxRate
)

data class SaleWithItems(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>
)


data class ParkedOrderWithItems(
    @Embedded val order: ParkedOrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parkedOrderId"
    )
    val items: List<ParkedOrderItemEntity>
)
