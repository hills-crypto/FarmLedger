package com.example.farmledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.farmledger.ui.theme.FarmLedgerTheme

// 1. Every screen the app can show.
//    "data object" = a screen with no extra information.
//    "data class"  = a screen that needs to know WHICH project (projectId).
sealed interface Screen {
    data object Dashboard : Screen
    data object AddProject : Screen
    data class ProjectDetails(val projectId: Long) : Screen
    data class AddTransaction(val projectId: Long) : Screen
    data object Customers : Screen
    data object Reminders : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FarmLedgerTheme {
                FarmLedgerApp()
            }
        }
    }
}

// 2. The app shell: holds the back stack and decides which screen to draw.
@Composable
fun FarmLedgerApp() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
    val current = backStack.last()

    fun goTo(screen: Screen) { backStack.add(screen) }
    fun goBack() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    // Makes the phone's own Back button/gesture use our back stack
    BackHandler(enabled = backStack.size > 1) { goBack() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (current) {
                Screen.Dashboard -> DashboardScreen(
                    onNewProject = { goTo(Screen.AddProject) },
                    onOpenProject = { id -> goTo(Screen.ProjectDetails(id)) },
                    onMoneyOwed = { goTo(Screen.Customers) },
                    onReminders = { goTo(Screen.Reminders) }
                )
                Screen.AddProject -> AddProjectScreen(
                    onSaved = { goBack() },
                    onCancel = { goBack() }
                )
                is Screen.ProjectDetails -> ProjectDetailsScreen(
                    projectId = current.projectId,
                    onAddTransaction = { goTo(Screen.AddTransaction(current.projectId)) },
                    onBack = { goBack() }
                )
                is Screen.AddTransaction -> AddTransactionScreen(
                    projectId = current.projectId,
                    onSaved = { goBack() },
                    onCancel = { goBack() }
                )
                Screen.Customers -> CustomersScreen(onBack = { goBack() })
                Screen.Reminders -> RemindersScreen(onBack = { goBack() })
            }
        }
    }
}

// 3. Temporary placeholder screens. We replace each one with the real screen later.
@Composable
fun PlaceholderScreen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        content()
    }
}

@Composable
fun DashboardScreen(
    onNewProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onMoneyOwed: () -> Unit,
    onReminders: () -> Unit
) = PlaceholderScreen("Dashboard") {
    Button(onClick = onNewProject) { Text("+ New Project") }
    Button(onClick = { onOpenProject(1L) }) { Text("Open test project (id 1)") }
    Button(onClick = onMoneyOwed) { Text("Money Owed") }
    Button(onClick = onReminders) { Text("Reminders") }
}

@Composable
fun AddProjectScreen(onSaved: () -> Unit, onCancel: () -> Unit) =
    PlaceholderScreen("Add Project") {
        Button(onClick = onSaved) { Text("Save") }
        OutlinedButton(onClick = onCancel) { Text("Cancel") }
    }

@Composable
fun ProjectDetailsScreen(projectId: Long, onAddTransaction: () -> Unit, onBack: () -> Unit) =
    PlaceholderScreen("Project Details (id $projectId)") {
        Button(onClick = onAddTransaction) { Text("Add Transaction") }
        OutlinedButton(onClick = onBack) { Text("Back") }
    }

@Composable
fun AddTransactionScreen(projectId: Long, onSaved: () -> Unit, onCancel: () -> Unit) =
    PlaceholderScreen("Add Transaction (project $projectId)") {
        Button(onClick = onSaved) { Text("Save") }
        OutlinedButton(onClick = onCancel) { Text("Cancel") }
    }

@Composable
fun CustomersScreen(onBack: () -> Unit) = PlaceholderScreen("Customers & Debts") {
    OutlinedButton(onClick = onBack) { Text("Back") }
}

@Composable
fun RemindersScreen(onBack: () -> Unit) = PlaceholderScreen("Reminders") {
    OutlinedButton(onClick = onBack) { Text("Back") }
}