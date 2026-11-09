package com.example.farmledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.farmledger.ui.AddProjectScreen
import com.example.farmledger.ui.AddTransactionScreen
import com.example.farmledger.ui.CustomersScreen
import com.example.farmledger.ui.DashboardScreen
import com.example.farmledger.ui.ProjectDetailsScreen
import com.example.farmledger.ui.theme.FarmLedgerTheme

// Represents navigation screen destinations for hand-written backstack routing.
sealed interface Screen {
    data object Dashboard : Screen
    data object AddProject : Screen
    data class ProjectDetails(val projectId: Long) : Screen
    data class AddTransaction(val projectId: Long) : Screen
    data object Customers : Screen
    data object Reminders : Screen
}

// Main activity entry point enabling edge-to-edge layout and initializing theme content.
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

// App navigation shell managing the hand-written screen backstack.
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
                    onBack = { goBack() },
                    onDeleted = { goBack() }
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

// Helper composable container for rendering remaining temporary placeholder screens.
@Composable
fun PlaceholderScreen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        content()
    }
}

// Temporary placeholder screen for Reminders.
@Composable
fun RemindersScreen(onBack: () -> Unit) = PlaceholderScreen("Reminders") {
    OutlinedButton(onClick = onBack) { Text("Back") }
}
