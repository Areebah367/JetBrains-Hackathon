package areebah.nyuad4jetbrains.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Icon
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
import areebah.nyuad4jetbrains.project.ui.AppIcons
import areebah.nyuad4jetbrains.project.ui.AppTheme
import areebah.nyuad4jetbrains.project.ui.AppViewModel
import areebah.nyuad4jetbrains.project.ui.InterestsScreen
import areebah.nyuad4jetbrains.project.ui.PlanScreen
import areebah.nyuad4jetbrains.project.ui.WhatsOnScreen
import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.tab_interests
import kotlinproject.shared.generated.resources.tab_whats_on
import kotlinproject.shared.generated.resources.tab_plan
import org.jetbrains.compose.resources.stringResource

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
                        icon = { Icon(AppIcons.Interests, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_interests)) },
                    )
                    NavigationBarItem(
                        selected = tab == TAB_WHATS_ON,
                        onClick = { tab = TAB_WHATS_ON },
                        icon = { Icon(AppIcons.WhatsOn, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_whats_on)) },
                    )
                    NavigationBarItem(
                        selected = tab == TAB_PLAN,
                        onClick = { tab = TAB_PLAN },
                        icon = { Icon(AppIcons.Plan, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_plan)) },
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
