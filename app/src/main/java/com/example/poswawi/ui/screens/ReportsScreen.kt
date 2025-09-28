package com.example.poswawi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.poswawi.data.model.ReportSummary
import com.example.poswawi.ui.LocalCafeRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime

enum class ReportRange { DAILY, MONTHLY }

@Composable
fun ReportsScreen() {
    val repository = LocalCafeRepository.current
    var report by remember { mutableStateOf<ReportSummary?>(null) }
    var range by remember { mutableStateOf(ReportRange.DAILY) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = rememberSnackbarHostState()

    LaunchedEffect(range) {
        val (from, to) = when (range) {
            ReportRange.DAILY -> startAndEndOfDay()
            ReportRange.MONTHLY -> startAndEndOfMonth()
        }
        report = repository.computeReport(from, to)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Berichte", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        SnackbarHost(hostState = snackbarHostState)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { range = ReportRange.DAILY }, enabled = range != ReportRange.DAILY) { Text("Tagesbericht") }
            Button(onClick = { range = ReportRange.MONTHLY }, enabled = range != ReportRange.MONTHLY) { Text("Monatsbericht") }
        }
        report?.let { summary ->
            SummaryCards(summary = summary)
            Button(onClick = {
                scope.launch {
                    val export = buildString {
                        appendLine("Kategorie,Wert")
                        appendLine("Umsatz,${summary.totalRevenue}")
                        appendLine("Mehrwertsteuer,${summary.totalVat}")
                        appendLine("Rabatte,${summary.totalDiscounts}")
                    }
                    snackbarHostState.showSnackbar("Export erstellt:\n$export")
                }
            }) {
                Text("Export als CSV")
            }
        }
    }
}

@Composable
private fun SummaryCards(summary: ReportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Gesamtumsatz", fontWeight = FontWeight.Bold)
                Text("%.2f €".format(summary.totalRevenue))
                Text("MwSt gesamt: %.2f €".format(summary.totalVat))
                Text("Rabatte: %.2f €".format(summary.totalDiscounts))
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Top Seller", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(summary.topProducts) { stat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stat.product.name)
                            Text("${stat.quantitySold} Stück / %.2f €".format(stat.revenue))
                        }
                    }
                }
            }
        }
        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Mitarbeiterleistung", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(summary.employeePerformance) { stat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stat.employee.name)
                            Text("${stat.salesCount} Bons / %.2f €".format(stat.revenue))
                        }
                    }
                }
            }
        }
    }
}

private fun startAndEndOfDay(): Pair<Instant, Instant> {
    val now = ZonedDateTime.now()
    val start = now.toLocalDate().atStartOfDay(now.zone)
    val end = start.plusDays(1)
    return start.toInstant() to end.toInstant()
}

private fun startAndEndOfMonth(): Pair<Instant, Instant> {
    val now = ZonedDateTime.now()
    val start = now.withDayOfMonth(1).toLocalDate().atStartOfDay(now.zone)
    val end = start.plusMonths(1)
    return start.toInstant() to end.toInstant()
}
