package com.example.poswawi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.poswawi.ui.LocalCafeRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val repository = LocalCafeRepository.current
    val employees by repository.employees.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var offlineEnabled by rememberSaveable { mutableStateOf(true) }
    var loyaltyEnabled by rememberSaveable { mutableStateOf(true) }
    var language by rememberSaveable { mutableStateOf("Deutsch") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Einstellungen", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingRow(
                    title = "Offline Synchronisation",
                    description = "Daten werden zwischengespeichert und bei Verbindung gesendet",
                    checked = offlineEnabled,
                    onCheckedChange = {
                        offlineEnabled = it
                        if (employees.isNotEmpty()) {
                            scope.launch { repository.createAuditLog(employees.first().id, "SYNC", "Offline Sync ${if (it) "aktiv" else "deaktiv"}") }
                        }
                    }
                )
                SettingRow(
                    title = "Stammkunden",
                    description = "Rabatte für registrierte Kunden aktivieren",
                    checked = loyaltyEnabled,
                    onCheckedChange = {
                        loyaltyEnabled = it
                        if (employees.isNotEmpty()) {
                            scope.launch { repository.createAuditLog(employees.first().id, "LOYALTY", "Loyalty ${if (it) "an" else "aus"}") }
                        }
                    }
                )
                Text("Sprache")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Deutsch", "English").forEach { option ->
                        Button(onClick = { language = option }, enabled = language != option) {
                            Text(option)
                        }
                    }
                }
                Text("Aktive Sprache: $language")
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
        )
    }
}
