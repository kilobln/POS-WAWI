package com.example.poswawi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.poswawi.data.model.ProductCategory
import com.example.poswawi.data.model.TaxRate
import com.example.poswawi.ui.LocalCafeRepository
import kotlinx.coroutines.launch

@Composable
fun InventoryScreen() {
    val repository = LocalCafeRepository.current
    val inventory by repository.inventory.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var newProductName by remember { mutableStateOf("") }
    var newProductPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.COFFEE) }
    var selectedTax by remember { mutableStateOf(TaxRate.REDUCED) }
    var reorderLevel by remember { mutableStateOf("10") }
    var purchaseQuantity by remember { mutableStateOf("0") }
    var purchaseSupplier by remember { mutableStateOf("") }
    var purchaseProductId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Warenwirtschaft", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(inventory, key = { it.product.id }) { snapshot ->
                val lowStock = snapshot.inventory.quantity <= snapshot.inventory.reorderLevel
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (lowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(snapshot.product.name, fontWeight = FontWeight.SemiBold)
                            Text("Bestand: ${snapshot.inventory.quantity}")
                            Text("Meldebestand: ${snapshot.inventory.reorderLevel}")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { scope.launch { repository.adjustInventory(snapshot.product.id, -1) } }) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            IconButton(onClick = { scope.launch { repository.adjustInventory(snapshot.product.id, 1) } }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
        Text("Neues Produkt", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newProductName,
                onValueChange = { newProductName = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = newProductPrice,
                onValueChange = { newProductPrice = it },
                label = { Text("Preis €") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CategorySelector(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
            TaxSelector(selectedTax = selectedTax, onTaxSelected = { selectedTax = it })
            OutlinedTextField(
                value = reorderLevel,
                onValueChange = { reorderLevel = it },
                label = { Text("Meldebestand") },
                modifier = Modifier.weight(1f)
            )
        }
        Button(onClick = {
            val price = newProductPrice.toDoubleOrNull()
            val reorder = reorderLevel.toIntOrNull()
            if (!newProductName.isNullOrBlank() && price != null && reorder != null) {
                scope.launch {
                    repository.addProduct(
                        name = newProductName,
                        price = price,
                        taxRate = selectedTax,
                        category = selectedCategory,
                        reorderLevel = reorder
                    )
                    newProductName = ""
                    newProductPrice = ""
                    reorderLevel = "10"
                }
            }
        }, enabled = newProductName.isNotBlank() && newProductPrice.isNotBlank()) {
            Text("Produkt anlegen")
        }
        Text("Wareneingang", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductDropdown(
                items = inventory.map { it.product },
                selectedProductId = purchaseProductId,
                onProductSelected = { purchaseProductId = it },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = purchaseQuantity,
                onValueChange = { purchaseQuantity = it },
                label = { Text("Menge") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = purchaseSupplier,
                onValueChange = { purchaseSupplier = it },
                label = { Text("Lieferant") },
                modifier = Modifier.weight(1f)
            )
        }
        Button(onClick = {
            val quantity = purchaseQuantity.toIntOrNull()
            val productId = purchaseProductId
            if (quantity != null && productId != null && purchaseSupplier.isNotBlank()) {
                scope.launch {
                    repository.addPurchase(productId, quantity, purchaseSupplier, costPerUnit = 0.0)
                    purchaseQuantity = "0"
                    purchaseSupplier = ""
                }
            }
        }, enabled = purchaseProductId != null) {
            Text("Wareneingang buchen")
        }
    }
}

@Composable
private fun CategorySelector(selectedCategory: ProductCategory, onCategorySelected: (ProductCategory) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Kategorie:")
        ProductCategory.entries.forEach { category ->
            Button(onClick = { onCategorySelected(category) }, enabled = category != selectedCategory) {
                Text(category.name)
            }
        }
    }
}

@Composable
private fun TaxSelector(selectedTax: TaxRate, onTaxSelected: (TaxRate) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("MwSt")
        TaxRate.entries.forEach { rate ->
            Button(onClick = { onTaxSelected(rate) }, enabled = rate != selectedTax) {
                Text("${rate.percentage.toInt()}%")
            }
        }
    }
}

@Composable
private fun ProductDropdown(
    items: List<com.example.poswawi.data.model.Product>,
    selectedProductId: Long?,
    onProductSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedProduct = items.firstOrNull { it.id == selectedProductId }
    Column(modifier = modifier) {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedProduct?.name ?: "Produkt wählen")
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { product ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(product.name) },
                    onClick = {
                        onProductSelected(product.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
