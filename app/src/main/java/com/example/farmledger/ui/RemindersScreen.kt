package com.example.farmledger.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmledger.data.Reminder

// Renders the scheduled farm tasks screen with status alerts and custom reminder creation.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    onOpenProject: (Long) -> Unit,
    viewModel: RemindersViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var customTitleInput by remember { mutableStateOf("") }
    var customDateInput by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = customDateInput)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { customDateInput = it }
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

    if (showAddDialog) {
        if (uiState.projects.isEmpty()) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("No Projects Found") },
                text = { Text("Create a project first.") },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("OK")
                    }
                }
            )
        } else {
            val defaultProject = selectedProjectId ?: uiState.projects.first().id
            val selectedProjectObj = uiState.projects.firstOrNull { it.id == defaultProject }

            AlertDialog(
                onDismissRequest = {
                    viewModel.clearError()
                    showAddDialog = false
                },
                title = { Text("Add Custom Reminder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedProjectObj?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Project") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                uiState.projects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text(proj.name) },
                                        onClick = {
                                            selectedProjectId = proj.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customTitleInput,
                            onValueChange = {
                                customTitleInput = it
                                viewModel.clearError()
                            },
                            label = { Text("Reminder Title") },
                            placeholder = { Text("e.g. Feed stock adjustment") },
                            isError = viewModel.customReminderError != null,
                            supportingText = viewModel.customReminderError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = formatDate(customDateInput),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Due Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.addCustomReminder(
                                projectId = defaultProject,
                                title = customTitleInput,
                                dueDateMs = customDateInput,
                                onDone = {
                                    showAddDialog = false
                                    customTitleInput = ""
                                }
                            )
                        },
                        enabled = !viewModel.isSaving
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearError()
                            showAddDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.testNotification()
                        Toast.makeText(context, "Test notification in 15 seconds", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Test")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedProjectId = uiState.projects.firstOrNull()?.id
                    customTitleInput = ""
                    customDateInput = System.currentTimeMillis()
                    viewModel.clearError()
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Reminder") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.reminders.isEmpty()) {
                item {
                    Text(
                        text = "No upcoming reminders.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.reminders, key = { it.id }) { reminder ->
                    ReminderItemCard(
                        reminder = reminder,
                        projectName = uiState.projectNames[reminder.projectId] ?: "Project",
                        onCheckedChange = { viewModel.markDone(reminder) },
                        onClick = { onOpenProject(reminder.projectId) }
                    )
                }
            }
        }
    }
}

// Renders an individual scheduled reminder card with overdue indicators and completion checkbox.
@Composable
private fun ReminderItemCard(
    reminder: Reminder,
    projectName: String,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit
) {
    val isOverdue = dayIndex(reminder.dueDate) < dayIndex(System.currentTimeMillis())
    val dateColor = if (isOverdue) Color(0xFFB3261E) else MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isDone,
                onCheckedChange = { onCheckedChange() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Project: $projectName", style = MaterialTheme.typography.bodySmall)

                val dateText = if (isOverdue) {
                    "${formatDate(reminder.dueDate)} (Overdue)"
                } else {
                    formatDate(reminder.dueDate)
                }
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = dateColor,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
