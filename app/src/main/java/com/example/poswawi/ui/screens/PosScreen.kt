package com.example.poswawi.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.poswawi.data.model.Employee
import com.example.poswawi.data.model.InventorySnapshot
import com.example.poswawi.data.model.PaymentMethod
import com.example.poswawi.data.model.Product
import com.example.poswawi.data.model.Sale
import com.example.poswawi.data.model.SaleItem
import com.example.poswawi.data.model.TaxRate
import com.example.poswawi.ui.LocalCafeRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PosScreen() {
    val repository = LocalCafeRepository.current
    val products by repository.products.collectAsState(initial = emptyList())
    val employees by repository.employees.collectAsState(initial = emptyList())
    val inventory by repository.inventory.collectAsState(initial = emptyList())
    val parkedOrders by repository.parkedSales.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val orderItems = remember { mutableStateMapOf<Long, Int>() }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var discount by remember { mutableStateOf(0f) }
    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }
    var customerName by remember { mutableStateOf("") }
    val snackbarHostState = rememberSnackbarHostState()

    LaunchedEffect(employees) {
        if (selectedEmployeeId == null && employees.isNotEmpty()) {
            selectedEmployeeId = employees.first().id
        }
    }

    val productMap = products.associateBy { it.id }
    val orderLines = orderItems.mapNotNull { (productId, qty) ->
        productMap[productId]?.let { product -> OrderLine(product, qty) }
    }
    val totals = remember(orderLines, discount) {
        OrderTotals.calculate(orderLines, discount.toDouble())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "POS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        EmployeeSelector(
            employees = employees,
            selectedEmployeeId = selectedEmployeeId,
            onEmployeeSelected = { selectedEmployeeId = it }
        )
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProductGrid(
                products = products,
                inventory = inventory,
                onAddProduct = { product ->
                    val current = orderItems[product.id] ?: 0
                    orderItems[product.id] = current + 1
                }
            )
            OrderSummaryCard(
                orderLines = orderLines,
                totals = totals,
                paymentMethod = paymentMethod,
                discount = discount,
                customerName = customerName,
                onDiscountChange = { discount = it },
                onPaymentSelected = { paymentMethod = it },
                onClear = { orderItems.clear(); discount = 0f; customerName = "" },
                onCheckout = {
                    val employeeId = selectedEmployeeId ?: return@OrderSummaryCard
                    val saleItems = orderLines.map {
                        SaleItem(
                            productId = it.product.id,
                            quantity = it.quantity,
                            unitPrice = it.product.price,
                            taxRate = it.product.taxRate
                        )
                    }
                    scope.launch {
                        repository.recordSale(
                            items = saleItems,
                            employeeId = employeeId,
                            paymentMethod = paymentMethod,
                            discountPercent = discount.toDouble(),
                            customerId = null,
                            markAsParked = false,
                            note = if (customerName.isNotBlank()) "Sale for $customerName" else null
                        )
                        repository.createAuditLog(
                            employeeId = employeeId,
                            action = "SALE_CREATED",
                            reason = "${saleItems.size} items sold"
                        )
                        snackbarHostState.showSnackbar("Bon gespeichert")
                        orderItems.clear()
                        discount = 0f
                        customerName = ""
                    }
                },
                onPark = {
                    val employeeId = selectedEmployeeId ?: return@OrderSummaryCard
                    val saleItems = orderLines.map {
                        SaleItem(
                            productId = it.product.id,
                            quantity = it.quantity,
                            unitPrice = it.product.price,
                            taxRate = it.product.taxRate
                        )
                    }
                    scope.launch {
                        repository.parkOrder(
                            name = customerName.ifBlank { "Parkbon" },
                            items = saleItems,
                            employeeId = employeeId
                        )
                        snackbarHostState.showSnackbar("Bon geparkt")
                        orderItems.clear()
                        discount = 0f
                        customerName = ""
                    }
                },
                onRemoveItem = { product ->
                    val current = orderItems[product.id] ?: 0
                    if (current <= 1) {
                        orderItems.remove(product.id)
                    } else {
                        orderItems[product.id] = current - 1
                    }
                },
                onCustomerChanged = { customerName = it },
                snackbarHostState = snackbarHostState
            )
        }
        ParkedOrdersList(
            orders = parkedOrders,
            onResume = { sale ->
                scope.launch {
                    repository.resumeParkedOrder(sale.id)
                    snackbarHostState.showSnackbar("Bon ${sale.id} wieder aufgenommen")
                }
            },
            onDelete = { sale ->
                scope.launch {
                    repository.deleteParkedOrder(sale.id)
                    snackbarHostState.showSnackbar("Bon ${sale.id} gelöscht")
                }
            },
            snackbarHostState = snackbarHostState
        )
    }
}

data class OrderLine(val product: Product, val quantity: Int)

data class OrderTotals(
    val subtotal: Double,
    val vatReduced: Double,
    val vatFull: Double,
    val discountAmount: Double,
    val total: Double
) {
    companion object {
        fun calculate(lines: List<OrderLine>, discountPercent: Double): OrderTotals {
            var subtotal = 0.0
            var vatReduced = 0.0
            var vatFull = 0.0
            lines.forEach { line ->
                val lineTotal = line.product.price * line.quantity
                subtotal += lineTotal
                val vat = lineTotal * line.product.taxRate.percentage / 100.0
                if (line.product.taxRate == TaxRate.REDUCED) {
                    vatReduced += vat
                } else {
                    vatFull += vat
                }
            }
            val discountAmount = subtotal * discountPercent / 100.0
            val total = subtotal - discountAmount + vatReduced + vatFull
            return OrderTotals(subtotal, vatReduced, vatFull, discountAmount, total)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductGrid(
    products: List<Product>,
    inventory: List<InventorySnapshot>,
    onAddProduct: (Product) -> Unit
) {
    val gridState = rememberLazyGridState()
    val stockByProduct = inventory.associateBy { it.product.id }
    LazyVerticalGrid(
        modifier = Modifier.weight(1f),
        columns = GridCells.Fixed(3),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products, key = { it.id }) { product ->
            val snapshot = stockByProduct[product.id]
            val lowStock = snapshot?.let { it.inventory.quantity <= it.inventory.reorderLevel } ?: false
            Card(
                onClick = { onAddProduct(product) },
                colors = CardDefaults.cardColors(
                    containerColor = if (lowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("%.2f €".format(product.price), fontWeight = FontWeight.Bold)
                    if (snapshot != null) {
                        Text("Bestand: ${snapshot.inventory.quantity}")
                    }
                    Text(
                        text = when (product.taxRate) {
                            TaxRate.REDUCED -> "7% MwSt"
                            TaxRate.FULL -> "19% MwSt"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun EmployeeSelector(
    employees: List<Employee>,
    selectedEmployeeId: Long?,
    onEmployeeSelected: (Long) -> Unit
) {
    if (employees.isEmpty()) {
        Text("Keine Mitarbeiter hinterlegt", color = MaterialTheme.colorScheme.error)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Mitarbeiter:", style = MaterialTheme.typography.titleSmall)
        employees.forEach { employee ->
            FilterChip(
                selected = employee.id == selectedEmployeeId,
                onClick = { onEmployeeSelected(employee.id) },
                label = { Text(employee.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun OrderSummaryCard(
    orderLines: List<OrderLine>,
    totals: OrderTotals,
    paymentMethod: PaymentMethod,
    discount: Float,
    customerName: String,
    onDiscountChange: (Float) -> Unit,
    onPaymentSelected: (PaymentMethod) -> Unit,
    onClear: () -> Unit,
    onCheckout: () -> Unit,
    onPark: () -> Unit,
    onRemoveItem: (Product) -> Unit,
    onCustomerChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Bon", style = MaterialTheme.typography.titleLarge)
            SnackbarHost(hostState = snackbarHostState)
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orderLines, key = { it.product.id }) { line ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(line.product.name, fontWeight = FontWeight.SemiBold)
                            Text("${line.quantity} x %.2f €".format(line.product.price))
                        }
                        IconButton(onClick = { onRemoveItem(line.product) }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
            OutlinedTextField(
                value = customerName,
                onValueChange = onCustomerChanged,
                label = { Text("Kunde optional") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Rabatt: ${discount.toInt()}%")
            Slider(value = discount, onValueChange = onDiscountChange, valueRange = 0f..50f)
            PaymentSelector(paymentMethod = paymentMethod, onSelected = onPaymentSelected)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Zwischensumme: %.2f €".format(totals.subtotal))
                Text("MwSt 7%: %.2f €".format(totals.vatReduced))
                Text("MwSt 19%: %.2f €".format(totals.vatFull))
                if (discount > 0f) {
                    Text("Rabatt: -%.2f €".format(totals.discountAmount))
                }
                Text("Gesamt: %.2f €".format(totals.total), fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClear, enabled = orderLines.isNotEmpty()) {
                    Text("Zurücksetzen")
                }
                Button(onClick = onPark, enabled = orderLines.isNotEmpty()) {
                    Text("Bon parken")
                }
                Button(onClick = onCheckout, enabled = orderLines.isNotEmpty()) {
                    Text("Zahlung abschließen")
                }
            }
        }
    }
}

@Composable
private fun PaymentSelector(
    paymentMethod: PaymentMethod,
    onSelected: (PaymentMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Zahlungsart", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentMethod.entries.forEach { method ->
                AssistChip(
                    onClick = { onSelected(method) },
                    label = { Text(method.toDisplay()) },
                    leadingIcon = {
                        if (method == paymentMethod) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (method == paymentMethod) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        labelColor = if (method == paymentMethod) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun PaymentMethod.toDisplay(): String = when (this) {
    PaymentMethod.CASH -> "Bar"
    PaymentMethod.CARD -> "Karte"
    PaymentMethod.WALLET -> "Gutschein"
}

@Composable
private fun ParkedOrdersList(
    orders: List<Sale>,
    onResume: (Sale) -> Unit,
    onDelete: (Sale) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Geparkte Bons", style = MaterialTheme.typography.titleMedium)
        if (orders.isEmpty()) {
            Text("Keine geparkten Bons")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders, key = { it.id }) { sale ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Bon #${sale.id}", fontWeight = FontWeight.Bold)
                                Text("Positionen: ${sale.items.size}")
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { onResume(sale) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                }
                                IconButton(onClick = { onDelete(sale) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
