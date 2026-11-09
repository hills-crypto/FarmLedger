package com.example.farmledger.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmledger.data.CustomerBalance

// Renders the customers debt list screen with total owed balance and payment recording options.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onBack: () -> Unit,
    viewModel: CustomersViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCustomerForPayment by remember { mutableStateOf<CustomerBalance?>(null) }
    var paymentAmountInput by remember { mutableStateOf("") }

    if (selectedCustomerForPayment != null) {
        val customer = selectedCustomerForPayment!!
        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
                selectedCustomerForPayment = null
                paymentAmountInput = ""
            },
            title = { Text("Record Payment – ${customer.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Balance: ${formatCurrency(customer.balance)}")
                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = {
                            paymentAmountInput = it
                            viewModel.clearError()
                        },
                        label = { Text("Payment Amount (Kwacha)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = viewModel.paymentAmountError != null,
                        supportingText = viewModel.paymentAmountError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.recordPayment(
                            customerId = customer.id,
                            amountStr = paymentAmountInput,
                            balance = customer.balance,
                            onDone = {
                                selectedCustomerForPayment = null
                                paymentAmountInput = ""
                            }
                        )
                    },
                    enabled = !viewModel.isSavingPayment
                ) {
                    Text("Save Payment")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.clearError()
                        selectedCustomerForPayment = null
                        paymentAmountInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers & Debts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Money Owed", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = formatCurrency(uiState.totalOwed),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB3261E)
                        )
                    }
                }
            }

            if (uiState.customers.isEmpty()) {
                item {
                    Text(
                        text = "No one owes you money.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.customers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onRecordPayment = {
                            paymentAmountInput = ""
                            viewModel.clearError()
                            selectedCustomerForPayment = customer
                        },
                        onCall = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                // Dialer activity not available
                            }
                        }
                    )
                }
            }
        }
    }
}

// Displays an individual customer's debt balance card with payment and call actions.
@Composable
private fun CustomerCard(
    customer: CustomerBalance,
    onRecordPayment: () -> Unit,
    onCall: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (customer.phone.isNotBlank()) {
                        Text("Phone: ${customer.phone}", style = MaterialTheme.typography.bodySmall)
                    }
                    customer.oldestUnpaidDate?.let { oldestDate ->
                        Text("Since: ${formatDate(oldestDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Text(
                    text = formatCurrency(customer.balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB3261E)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onRecordPayment) {
                    Text("Record Payment")
                }

                if (customer.phone.isNotBlank()) {
                    IconButton(onClick = onCall) {
                        Icon(Icons.Default.Call, contentDescription = "Call Customer", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
