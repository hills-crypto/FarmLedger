package com.example.farmledger.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmledger.data.ProjectType

// Renders the form screen for creating a new farming project with validated fields.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddProjectViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.startDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateStartDate(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Project") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = viewModel::updateName,
                label = { Text("Project Name") },
                placeholder = { Text("e.g. 400 Broilers – Batch 3") },
                isError = viewModel.nameError != null,
                supportingText = viewModel.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Project Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectType.entries.forEach { type ->
                    FilterChip(
                        selected = viewModel.type == type,
                        onClick = { viewModel.updateType(type) },
                        label = { Text(type.toDisplayName()) }
                    )
                }
            }

            if (viewModel.type == ProjectType.GARDEN) {
                Text("Crop", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Rape", "Tomato", "Cabbage", "Other").forEach { crop ->
                        FilterChip(
                            selected = viewModel.variety == crop,
                            onClick = { viewModel.updateVariety(crop) },
                            label = { Text(crop) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.quantity,
                onValueChange = viewModel::updateQuantity,
                label = { Text("Initial Quantity / Animals") },
                placeholder = { Text("e.g. 400") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = viewModel.quantityError != null,
                supportingText = viewModel.quantityError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formatDate(viewModel.startDate),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Start Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            OutlinedTextField(
                value = viewModel.budget,
                onValueChange = viewModel::updateBudget,
                label = { Text("Budget (Kwacha)") },
                placeholder = { Text("e.g. 7500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = viewModel.budgetError != null,
                supportingText = viewModel.budgetError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.reset()
                        onCancel()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { viewModel.save(onSaved) },
                    enabled = !viewModel.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Project")
                }
            }
        }
    }
}
