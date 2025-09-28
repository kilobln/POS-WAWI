package com.example.poswawi.data

import com.example.poswawi.data.local.AuditLogEntity
import com.example.poswawi.data.local.CafeDao
import com.example.poswawi.data.local.CustomerEntity
import com.example.poswawi.data.local.EmployeeEntity
import com.example.poswawi.data.local.EmployeeShiftEntity
import com.example.poswawi.data.local.InventoryEntity
import com.example.poswawi.data.local.ParkedOrderEntity
import com.example.poswawi.data.local.ParkedOrderItemEntity
import com.example.poswawi.data.local.ParkedOrderWithItems
import com.example.poswawi.data.local.ProductEntity
import com.example.poswawi.data.local.PurchaseEntity
import com.example.poswawi.data.local.SaleEntity
import com.example.poswawi.data.local.SaleItemEntity
import com.example.poswawi.data.local.SaleWithItems
import com.example.poswawi.data.model.AuditLog
import com.example.poswawi.data.model.Customer
import com.example.poswawi.data.model.Discount
import com.example.poswawi.data.model.DiscountType
import com.example.poswawi.data.model.Employee
import com.example.poswawi.data.model.EmployeeRole
import com.example.poswawi.data.model.EmployeeShift
import com.example.poswawi.data.model.EmployeeStatistic
import com.example.poswawi.data.model.InventoryItem
import com.example.poswawi.data.model.InventorySnapshot
import com.example.poswawi.data.model.PaymentMethod
import com.example.poswawi.data.model.Product
import com.example.poswawi.data.model.ProductCategory
import com.example.poswawi.data.model.ProductStatistic
import com.example.poswawi.data.model.Purchase
import com.example.poswawi.data.model.ReportSummary
import com.example.poswawi.data.model.Sale
import com.example.poswawi.data.model.SaleItem
import com.example.poswawi.data.model.TaxRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

class CafeRepositoryImpl(private val dao: CafeDao) : CafeRepository {
    override val products: Flow<List<Product>> = dao.observeProducts().map { entities ->
        entities.map { it.toDomain() }
    }

    override val inventory: Flow<List<InventorySnapshot>> =
        combine(dao.observeProducts(), dao.observeInventory()) { products, inventory ->
            val productMap = products.associateBy { it.id }
            inventory.mapNotNull { item ->
                val product = productMap[item.productId]
                if (product != null) {
                    InventorySnapshot(product.toDomain(), item.toDomain())
                } else {
                    null
                }
            }
        }

    override val employees: Flow<List<Employee>> = dao.observeEmployees().map { list ->
        list.map { it.toDomain() }
    }

    override val customers: Flow<List<Customer>> = dao.observeCustomers().map { list ->
        list.map { it.toDomain() }
    }

    override val openShifts: Flow<List<EmployeeShift>> = dao.observeOpenShifts().map { list ->
        list.map { it.toDomain() }
    }

    override val sales: Flow<List<Sale>> = dao.observeSales().map { list ->
        list.map { it.toDomain() }
    }

    override val purchases: Flow<List<Purchase>> = dao.observePurchases().map { list ->
        list.map { it.toDomain() }
    }

    override val auditLogs: Flow<List<AuditLog>> = dao.observeAuditLogs().map { list ->
        list.map { it.toDomain() }
    }

    override val parkedSales: Flow<List<Sale>> =
        dao.observeParkedOrdersWithItems().map { orders ->
            orders.map { it.toDomain() }
        }

    override suspend fun addProduct(
        name: String,
        price: Double,
        taxRate: TaxRate,
        category: ProductCategory,
        imageUri: String?,
        reorderLevel: Int
    ) {
        withContext(Dispatchers.IO) {
            val insertedId = dao.upsertProduct(
                ProductEntity(
                    name = name,
                    price = price,
                    taxRate = taxRate,
                    category = category,
                    imageUri = imageUri,
                    isActive = true
                )
            )
            val productId = if (insertedId != 0L) {
                insertedId
            } else {
                dao.observeProducts().first().maxByOrNull { it.id }?.id ?: 0L
            }

            dao.upsertInventory(
                InventoryEntity(
                    productId = productId,
                    quantity = 0,
                    reorderLevel = reorderLevel
                )
            )
        }
    }

    override suspend fun adjustInventory(productId: Long, delta: Int) {
        withContext(Dispatchers.IO) {
            dao.adjustStock(productId, delta)
        }
    }

    override suspend fun recordSale(
        items: List<SaleItem>,
        employeeId: Long,
        paymentMethod: PaymentMethod,
        discountPercent: Double,
        customerId: Long?,
        markAsParked: Boolean,
        note: String?
    ) {
        withContext(Dispatchers.IO) {
            val discount = if (discountPercent > 0.0) {
                Discount(DiscountType.PERCENT, discountPercent)
            } else null
            val saleId = dao.insertSale(
                SaleEntity(
                    employeeId = employeeId,
                    timestamp = Instant.now(),
                    paymentMethod = paymentMethod,
                    discountType = discount?.type,
                    discountValue = discount?.value,
                    customerId = customerId,
                    isParked = markAsParked,
                    isSynced = !markAsParked
                )
            )
            dao.insertSaleItems(items.map { item ->
                SaleItemEntity(
                    saleId = saleId,
                    productId = item.productId,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    taxRate = item.taxRate
                )
            })
            if (!markAsParked) {
                items.forEach { dao.adjustStock(it.productId, -it.quantity) }
            }
            if (!note.isNullOrBlank()) {
                dao.insertAuditLog(
                    AuditLogEntity(
                        employeeId = employeeId,
                        timestamp = Instant.now(),
                        action = "SALE_NOTE",
                        reason = note
                    )
                )
            }
        }
    }

    override suspend fun parkOrder(name: String, items: List<SaleItem>, employeeId: Long) {
        withContext(Dispatchers.IO) {
            val orderId = dao.insertParkedOrder(
                ParkedOrderEntity(
                    createdAt = Instant.now(),
                    name = name,
                    employeeId = employeeId
                )
            )
            dao.insertParkedOrderItems(items.map {
                ParkedOrderItemEntity(
                    parkedOrderId = orderId,
                    productId = it.productId,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    taxRate = it.taxRate
                )
            })
            createAuditLog(employeeId, "ORDER_PARKED", "Order $name parked")
        }
    }

    override suspend fun resumeParkedOrder(orderId: Long) {
        withContext(Dispatchers.IO) {
            val order = dao.getParkedOrder(orderId) ?: return@withContext
            val items = dao.getParkedOrderItems(orderId).map { it.toSaleItem() }
            recordSale(
                items = items,
                employeeId = order.employeeId,
                paymentMethod = PaymentMethod.CASH,
                discountPercent = 0.0,
                customerId = null,
                markAsParked = false,
                note = "Parked order ${order.name} resumed"
            )
            dao.deleteParkedOrderItems(orderId)
            dao.deleteParkedOrder(orderId)
        }
    }

    override suspend fun deleteParkedOrder(orderId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteParkedOrderItems(orderId)
            dao.deleteParkedOrder(orderId)
        }
    }

    override suspend fun addEmployee(name: String, pin: String, role: EmployeeRole) {
        withContext(Dispatchers.IO) {
            dao.upsertEmployee(
                EmployeeEntity(
                    name = name,
                    pin = pin,
                    role = role,
                    hourlyRate = when (role) {
                        EmployeeRole.ADMIN -> 18.5
                        EmployeeRole.CASHIER -> 15.0
                        EmployeeRole.TEMP -> 12.5
                    }
                )
            )
        }
    }

    override suspend fun clockIn(employeeId: Long) {
        withContext(Dispatchers.IO) {
            dao.insertShift(
                EmployeeShiftEntity(
                    employeeId = employeeId,
                    clockIn = Instant.now()
                )
            )
        }
    }

    override suspend fun clockOut(shiftId: Long) {
        withContext(Dispatchers.IO) {
            val existing = dao.getShiftById(shiftId) ?: return@withContext
            dao.updateShift(existing.copy(clockOut = Instant.now()))
        }
    }

    override suspend fun addCustomer(name: String, discountPercent: Double) {
        withContext(Dispatchers.IO) {
            dao.upsertCustomer(
                CustomerEntity(
                    name = name,
                    loyaltyPoints = 0,
                    discountPercent = discountPercent
                )
            )
        }
    }

    override suspend fun addPurchase(productId: Long, quantity: Int, supplier: String, costPerUnit: Double) {
        withContext(Dispatchers.IO) {
            dao.insertPurchase(
                PurchaseEntity(
                    productId = productId,
                    quantity = quantity,
                    supplier = supplier,
                    costPerUnit = costPerUnit,
                    timestamp = Instant.now()
                )
            )
            dao.adjustStock(productId, quantity)
        }
    }

    override suspend fun createAuditLog(employeeId: Long, action: String, reason: String) {
        withContext(Dispatchers.IO) {
            dao.insertAuditLog(
                AuditLogEntity(
                    employeeId = employeeId,
                    timestamp = Instant.now(),
                    action = action,
                    reason = reason
                )
            )
        }
    }

    override suspend fun computeReport(from: Instant, to: Instant): ReportSummary = withContext(Dispatchers.IO) {
        val sales = dao.observeSales().map { list -> list.map { it.toDomain() } }.first()
            .filter { it.timestamp.isAfter(from) || it.timestamp == from }
            .filter { it.timestamp.isBefore(to) || it.timestamp == to }
        val employeesMap = employees.first().associateBy { it.id }
        val productsMap = products.first().associateBy { it.id }

        var totalRevenue = 0.0
        var totalVat = 0.0
        var totalDiscount = 0.0
        val productStats = mutableMapOf<Long, ProductStatistic>()
        val employeeStats = mutableMapOf<Long, EmployeeStatistic>()

        sales.forEach { sale ->
            val discountMultiplier = when (sale.discount?.type) {
                DiscountType.PERCENT -> 1 - sale.discount.value / 100.0
                DiscountType.AMOUNT -> 1.0
                null -> 1.0
            }
            sale.items.forEach { item ->
                val product = productsMap[item.productId] ?: return@forEach
                val lineNet = item.unitPrice * item.quantity * discountMultiplier
                val lineVat = lineNet * item.taxRate.percentage / 100.0
                totalRevenue += lineNet + lineVat
                totalVat += lineVat
                if (sale.discount?.type == DiscountType.PERCENT) {
                    totalDiscount += item.unitPrice * item.quantity * sale.discount.value / 100.0
                }
                val stat = productStats[item.productId]
                productStats[item.productId] = if (stat == null) {
                    ProductStatistic(product, item.quantity, lineNet + lineVat)
                } else {
                    stat.copy(
                        quantitySold = stat.quantitySold + item.quantity,
                        revenue = stat.revenue + lineNet + lineVat
                    )
                }
            }
            val employee = employeesMap[sale.employeeId]
            if (employee != null) {
                val stat = employeeStats[sale.employeeId]
                employeeStats[sale.employeeId] = if (stat == null) {
                    EmployeeStatistic(employee, 1, sale.totalAmount())
                } else {
                    stat.copy(
                        salesCount = stat.salesCount + 1,
                        revenue = stat.revenue + sale.totalAmount()
                    )
                }
            }
        }

        ReportSummary(
            totalRevenue = totalRevenue,
            totalVat = totalVat,
            totalDiscounts = totalDiscount,
            topProducts = productStats.values.sortedByDescending { it.revenue }.take(5),
            employeePerformance = employeeStats.values.sortedByDescending { it.revenue }
        )
    }

    private fun ProductEntity.toDomain() = Product(
        id = id,
        name = name,
        price = price,
        taxRate = taxRate,
        category = category,
        imageUri = imageUri,
        isActive = isActive
    )

    private fun InventoryEntity.toDomain() = InventoryItem(
        productId = productId,
        quantity = quantity,
        reorderLevel = reorderLevel
    )

    private fun EmployeeEntity.toDomain() = Employee(
        id = id,
        name = name,
        pin = pin,
        role = role,
        hourlyRate = hourlyRate
    )

    private fun EmployeeShiftEntity.toDomain() = EmployeeShift(
        id = id,
        employeeId = employeeId,
        clockIn = clockIn,
        clockOut = clockOut
    )

    private fun CustomerEntity.toDomain() = Customer(
        id = id,
        name = name,
        loyaltyPoints = loyaltyPoints,
        discountPercent = discountPercent
    )

    private fun PurchaseEntity.toDomain() = Purchase(
        id = id,
        productId = productId,
        quantity = quantity,
        supplier = supplier,
        costPerUnit = costPerUnit,
        timestamp = timestamp
    )

    private fun AuditLogEntity.toDomain() = AuditLog(
        id = id,
        employeeId = employeeId,
        timestamp = timestamp,
        action = action,
        reason = reason
    )

    private fun SaleWithItems.toDomain(): Sale = sale.toDomain(items)

    private fun SaleEntity.toDomain(items: List<SaleItemEntity>): Sale {
        val discount = if (discountType != null && discountValue != null) {
            Discount(discountType, discountValue)
        } else null
        return Sale(
            id = id,
            items = items.map { it.toDomain() },
            employeeId = employeeId,
            timestamp = timestamp,
            paymentMethod = paymentMethod,
            discount = discount,
            customerId = customerId,
            isParked = isParked,
            isSynced = isSynced
        )
    }

    private fun SaleItemEntity.toDomain() = SaleItem(
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        taxRate = taxRate
    )

    private fun ParkedOrderWithItems.toDomain(): Sale = Sale(
        id = order.id,
        items = items.map { it.toSaleItem() },
        employeeId = order.employeeId,
        timestamp = order.createdAt,
        paymentMethod = PaymentMethod.CASH,
        discount = null,
        customerId = null,
        isParked = true,
        isSynced = false
    )

    private fun ParkedOrderItemEntity.toSaleItem() = SaleItem(
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        taxRate = taxRate
    )

    private fun Sale.totalAmount(): Double = items.sumOf {
        val net = it.unitPrice * it.quantity
        val vat = net * it.taxRate.percentage / 100.0
        net + vat
    }
}
