package com.example.nempille.ui.screens.medication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nempille.domain.model.Medication
import kotlinx.coroutines.launch

@Composable
fun EditMedicationScreen(
    navController: NavController,
    medicationId: Int,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    // Coroutine scope for loading medication
    val scope = rememberCoroutineScope()

    // Local state for form fields
    var medication by remember { mutableStateOf<Medication?>(null) }
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var freqText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Load medication when screen starts
    LaunchedEffect(medicationId) {
        val med = viewModel.getMedication(medicationId)
        medication = med
        if (med != null) {
            name = med.name
            dosage = med.dosage
            freqText = med.frequencyPerDay.toString()
            notes = med.notes ?: ""
        }
    }

    if (medication == null) {
        // Show loading state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Edit Medication", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("Dosage") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = freqText,
            onValueChange = { freqText = it },
            label = { Text("Times per day") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val updated = medication!!.copy(
                    name = name.trim(),
                    dosage = dosage.trim(),
                    frequencyPerDay = freqText.toIntOrNull() ?: 1,
                    notes = notes.ifBlank { null }
                )
                viewModel.updateMedication(updated)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cancel")
        }
    }
}