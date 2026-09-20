package areebah.nyuad4jetbrains.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import areebah.nyuad4jetbrains.project.ui.AppTheme
import areebah.nyuad4jetbrains.project.ui.AppViewModel
import areebah.nyuad4jetbrains.project.ui.InterestsScreen
import areebah.nyuad4jetbrains.project.ui.PlanScreen
import areebah.nyuad4jetbrains.project.ui.WhatsOnScreen

private const val TAB_INTERESTS = 0
private const val TAB_WHATS_ON = 1
private const val TAB_PLAN = 2

@Composable
fun App() {
    val viewModel: AppViewModel = viewModel { AppViewModel() }
    val state by viewModel.state.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(TAB_INTERESTS) }

    AppTheme(mode = state.themeMode) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == TAB_INTERESTS,
                        onClick = { tab = TAB_INTERESTS },
                        icon = {},
                        label = { Text("My interests") },
                    )
                    NavigationBarItem(
                        selected = tab == TAB_WHATS_ON,
                        onClick = { tab = TAB_WHATS_ON },
                        icon = {},
                        label = { Text("What's on") },
                    )
                    NavigationBarItem(
                        selected = tab == TAB_PLAN,
                        onClick = { tab = TAB_PLAN },
                        icon = {},
                        label = { Text("My plan") },
                    )
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    TAB_INTERESTS -> InterestsScreen(
                        profile = state.profile,
                        themeMode = state.themeMode,
                        onToggleInterest = viewModel::toggleInterest,
                        onOtherHobbiesChange = viewModel::setOtherHobbies,
                        onThemeModeChange = viewModel::setThemeMode,
                        onShowWeek = { tab = TAB_WHATS_ON },
                    )

                    TAB_WHATS_ON -> WhatsOnScreen(
                        state = state,
                        onOnlyMatchingChange = viewModel::setOnlyMatching,
                        onHorizonChange = viewModel::setHorizon,
                        onToggleCity = viewModel::toggleCity,
                        onRefresh = viewModel::refresh,
                    )

                    else -> PlanScreen(
                        state = state,
                        onBudgetChange = viewModel::setBudget,
                        onAccept = viewModel::accept,
                        onReject = viewModel::reject,
                        onReset = viewModel::resetPlan,
                    )
                }
            }
        }
    }
}
