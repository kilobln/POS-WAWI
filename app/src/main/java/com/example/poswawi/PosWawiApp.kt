package com.example.poswawi

import android.app.Application
import com.example.poswawi.data.CafeRepository
import com.example.poswawi.data.CafeRepositoryImpl
import com.example.poswawi.data.local.CafeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.example.poswawi.data.model.ProductCategory
import com.example.poswawi.data.model.TaxRate

class PosWawiApp : Application() {
    lateinit var repository: CafeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = CafeDatabase.getInstance(this)
        repository = CafeRepositoryImpl(database.dao)
        seedDemoData()
    }

    private fun seedDemoData() {
        CoroutineScope(Dispatchers.IO).launch {
            val products = repository.products.first()
            if (products.isEmpty()) {
                repository.addProduct("Espresso", 2.4, TaxRate.REDUCED, ProductCategory.COFFEE, reorderLevel = 20)
                repository.addProduct("Cappuccino", 3.2, TaxRate.REDUCED, ProductCategory.COFFEE, reorderLevel = 15)
                repository.addProduct("Kuchen", 3.8, TaxRate.REDUCED, ProductCategory.FOOD, reorderLevel = 10)
                repository.addProduct("Limonade", 2.5, TaxRate.FULL, ProductCategory.DRINK, reorderLevel = 25)
            }
            val employees = repository.employees.first()
            if (employees.isEmpty()) {
                repository.addEmployee("Admin", "1234", com.example.poswawi.data.model.EmployeeRole.ADMIN)
                repository.addEmployee("Kassierer", "0000", com.example.poswawi.data.model.EmployeeRole.CASHIER)
            }
        }
    }
