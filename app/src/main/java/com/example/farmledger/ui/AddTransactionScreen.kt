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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.farmledger.data.EntryKind
import com.example.farmledger.data.PaymentType

private val ExpenseCategories = listOf("Feed", "Drugs", "Labour", "Transport", "Other")

// Renders the input screen for recording a project expense or sales transaction.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    projectId: Long,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel(
        key = "add-tx-$projectId",
        factory = AppViewModelProvider.addTransactionFactory(projectId)
    )
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateDate(it) }
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

    val errors = viewModel.validationErrors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.kind == EntryKind.EXPENSE) "Record Expense" else "Record Sale") }
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
            Text("Transaction Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EntryKind.entries.forEach { kindOption ->
                    FilterChip(
                        selected = viewModel.kind == kindOption,
                        onClick = { viewModel.updateKind(kindOption) },
                        label = { Text(if (kindOption == EntryKind.EXPENSE) "Expense" else "Sale") }
                    )
                }
            }

            if (viewModel.kind == EntryKind.EXPENSE) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        isError = errors.categoryError != null,
                        supportingText = errors.categoryError?.let { { Text(it) } },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        ExpenseCategories.forEach { categoryItem ->
                            DropdownMenuItem(
                                text = { Text(categoryItem) },
                                onClick = {
                                    viewModel.updateCategory(categoryItem)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = viewModel.quantity,
                    onValueChange = viewModel::updateQuantity,
                    label = { Text("Quantity Sold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errors.quantityError != null,
                    supportingText = errors.quantityError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Payment Type", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentType.entries.forEach { paymentTypeOption ->
                        FilterChip(
                            selected = viewModel.paymentType == paymentTypeOption,
                            onClick = { viewModel.updatePaymentType(paymentTypeOption) },
                            label = { Text(if (paymentTypeOption == PaymentType.CASH) "Cash" else "Credit") }
                        )
                    }
                }

                if (viewModel.paymentType == PaymentType.CREDIT) {
                    OutlinedTextField(
                        value = viewModel.customerName,
                        onValueChange = viewModel::updateCustomerName,
                        label = { Text("Customer Name") },
                        isError = errors.customerNameError != null,
                        supportingText = errors.customerNameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.customerPhone,
                        onValueChange = viewModel::updateCustomerPhone,
                        label = { Text("Customer Phone (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            OutlinedTextField(
                value = viewModel.amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("Total Amount (Kwacha)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errors.amountError != null,
                supportingText = errors.amountError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formatDate(viewModel.date),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Transaction Date") },
                isError = errors.dateError != null,
                supportingText = errors.dateError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            OutlinedTextField(
                value = viewModel.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
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
                    Text("Save Transaction")
                }
            }
        }
    }
}
