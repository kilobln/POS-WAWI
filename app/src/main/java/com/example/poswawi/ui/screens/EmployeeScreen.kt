package com.example.poswawi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.poswawi.data.model.Employee
import com.example.poswawi.data.model.EmployeeRole
import com.example.poswawi.ui.LocalCafeRepository
import kotlinx.coroutines.launch

@Composable
fun EmployeeScreen() {
    val repository = LocalCafeRepository.current
    val employees by repository.employees.collectAsState(initial = emptyList())
    val openShifts by repository.openShifts.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var newEmployeeName by remember { mutableStateOf("") }
    var newEmployeePin by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(EmployeeRole.CASHIER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mitarbeiter", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(employees, key = { it.id }) { employee ->
                EmployeeCard(
                    employee = employee,
                    isClockedIn = openShifts.any { it.employeeId == employee.id },
                    onClockIn = { scope.launch { repository.clockIn(employee.id) } },
                    onClockOut = {
                        val shift = openShifts.firstOrNull { it.employeeId == employee.id }
                        if (shift != null) {
                            scope.launch { repository.clockOut(shift.id) }
                        }
                    }
                )
            }
        }
        Text("Neuer Mitarbeiter", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = newEmployeeName,
            onValueChange = { newEmployeeName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newEmployeePin,
            onValueChange = { newEmployeePin = it },
            label = { Text("PIN") },
            modifier = Modifier.fillMaxWidth()
        )
        RoleSelector(selectedRole = selectedRole, onRoleSelected = { selectedRole = it })
        Button(onClick = {
            if (newEmployeeName.isNotBlank() && newEmployeePin.length >= 4) {
                scope.launch {
                    repository.addEmployee(newEmployeeName, newEmployeePin, selectedRole)
                    newEmployeeName = ""
                    newEmployeePin = ""
                }
            }
        }, enabled = newEmployeeName.isNotBlank() && newEmployeePin.length >= 4) {
            Text("Mitarbeiter anlegen")
        }
    }
}

@Composable
private fun EmployeeCard(
    employee: Employee,
    isClockedIn: Boolean,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(employee.name, fontWeight = FontWeight.SemiBold)
            Text("Rolle: ${employee.role}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isClockedIn) {
                    Button(onClick = onClockOut) { Text("Check-out") }
                } else {
                    Button(onClick = onClockIn) { Text("Check-in") }
                }
            }
        }
    }
}

@Composable
private fun RoleSelector(selectedRole: EmployeeRole, onRoleSelected: (EmployeeRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Rolle:")
        EmployeeRole.entries.forEach { role ->
            Button(onClick = { onRoleSelected(role) }, enabled = role != selectedRole) {
                Text(role.name)
            }
        }
    }
}
