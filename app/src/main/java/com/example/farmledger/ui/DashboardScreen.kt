package com.example.farmledger.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmledger.data.Project
import com.example.farmledger.data.ProjectType
import com.example.farmledger.data.Reminder

// Renders the main dashboard screen displaying project summaries, financial metrics, and upcoming reminders.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNewProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onMoneyOwed: () -> Unit,
    onReminders: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FarmLedger", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewProject,
                icon = { Text("+", style = MaterialTheme.typography.titleLarge) },
                text = { Text("New Project") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Active Projects",
                        value = uiState.projectCount.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    val isProfitPositive = uiState.totalProfit >= 0.0
                    val profitColor = if (isProfitPositive) Color(0xFF2E6B3A) else Color(0xFFB3261E)
                    MetricCard(
                        title = "Total Profit/Loss",
                        value = formatCurrency(uiState.totalProfit),
                        valueColor = profitColor,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Money Owed",
                        value = formatCurrency(uiState.totalOwed),
                        onClick = onMoneyOwed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReminders() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Next Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("View All", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }

                        if (uiState.nextReminders.isEmpty()) {
                            Text(
                                "No upcoming reminders scheduled.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            uiState.nextReminders.forEach { reminder ->
                                ReminderRow(reminder = reminder)
                            }
                        }
                    }
                }
            }

            item {
                Text("Projects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (uiState.projects.isEmpty()) {
                item {
                    Text(
                        "No active farming projects yet. Click '+ New Project' to create one.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.projects, key = { it.id }) { project ->
                    ProjectCard(project = project, onClick = { onOpenProject(project.id) })
                }
            }
        }
    }
}

// Renders an individual financial or quantitative summary metric card.
@Composable
private fun MetricCard(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

// Displays a single reminder row item on the dashboard summary card.
@Composable
private fun ReminderRow(reminder: Reminder) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(reminder.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(formatDate(reminder.dueDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

// Displays a list item card for an individual farming project.
@Composable
private fun ProjectCard(project: Project, onClick: () -> Unit) {
    val displayCategory = if (project.type == ProjectType.GARDEN && project.variety.isNotBlank()) {
        "Garden – ${project.variety}"
    } else {
        project.type.toDisplayName()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Type: $displayCategory", style = MaterialTheme.typography.bodyMedium)
                Text("Started: ${formatDate(project.startDate)}", style = MaterialTheme.typography.bodySmall)
            }
            Text("Quantity: ${project.quantity}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
