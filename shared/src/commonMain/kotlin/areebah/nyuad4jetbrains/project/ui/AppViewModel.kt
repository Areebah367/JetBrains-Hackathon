package areebah.nyuad4jetbrains.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import areebah.nyuad4jetbrains.project.data.EventRepository
import areebah.nyuad4jetbrains.project.data.EventsResult
import areebah.nyuad4jetbrains.project.data.defaultEventRepository
import areebah.nyuad4jetbrains.project.domain.AbuDhabiTime
import areebah.nyuad4jetbrains.project.domain.Horizon
import areebah.nyuad4jetbrains.project.domain.InterestProfile
import areebah.nyuad4jetbrains.project.domain.ScheduleFilters
import areebah.nyuad4jetbrains.project.planner.Decision
import areebah.nyuad4jetbrains.project.planner.FreeSlot
import areebah.nyuad4jetbrains.project.planner.PlannerState
import areebah.nyuad4jetbrains.project.planner.accept
import areebah.nyuad4jetbrains.project.planner.computeFreeSlots
import areebah.nyuad4jetbrains.project.planner.reject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** How many days ahead the planner looks for free time. */
private const val PLANNING_DAYS = 14
private const val DEFAULT_BUDGET = 500.0

data class UiState(
    val profile: InterestProfile = InterestProfile(),
    val filters: ScheduleFilters = ScheduleFilters(),
    val loading: Boolean = true,
    val today: LocalDate? = null,
    val result: EventsResult? = null,
    val budget: Double = DEFAULT_BUDGET,
    val decisions: Map<String, Decision> = emptyMap(),
    val userPrices: Map<String, Double> = emptyMap(),
    /**
     * Busy time is not read from the phone's calendar yet, so every day counts as free inside the
     * planner's day window. The Plan screen says so rather than implying a calendar is connected.
     */
    val freeSlots: List<FreeSlot> = emptyList(),
    val calendarConnected: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
) {
    /** The planner rebuilt from the current state. Cheap: everything it derives is computed on read. */
    val planner: PlannerState
        get() = PlannerState(
            budget = budget,
            events = result?.events.orEmpty(),
            freeSlots = freeSlots,
            decisions = decisions,
            userPrices = userPrices,
        )
}

class AppViewModel(
    private val repository: EventRepository = defaultEventRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val today = AbuDhabiTime.today()
            val result = repository.load(today)
            _state.update {
                it.copy(
                    loading = false,
                    today = today,
                    result = result,
                    freeSlots = computeFreeSlots(busy = emptyList(), from = today, days = PLANNING_DAYS),
                )
            }
        }
    }

    fun toggleInterest(label: String) {
        _state.update { state ->
            val selected = state.profile.selectedLabels
            val updated = if (label in selected) selected - label else selected + label
            state.copy(profile = state.profile.copy(selectedLabels = updated))
        }
    }

    fun setOtherHobbies(text: String) {
        _state.update { it.copy(profile = it.profile.copy(otherHobbies = text)) }
    }

    fun setOnlyMatching(value: Boolean) = updateFilters { it.copy(onlyMatching = value) }

    fun setHorizon(horizon: Horizon) = updateFilters { it.copy(horizon = horizon) }

    /** Tapping a selected city clears it, so the chips behave as "all" when none is on. */
    fun toggleCity(city: String) = updateFilters { filters ->
        filters.copy(cities = if (city in filters.cities) filters.cities - city else filters.cities + city)
    }

    fun setThemeMode(mode: ThemeMode) {
        _state.update { it.copy(themeMode = mode) }
    }

    fun setBudget(value: Double) {
        _state.update { it.copy(budget = value.coerceAtLeast(0.0)) }
    }

    /** Accepts an event, optionally recording the price the user typed for it. */
    fun accept(eventId: String, price: Double? = null) {
        _state.update { state ->
            val updated = state.planner.accept(eventId, price)
            state.copy(decisions = updated.decisions, userPrices = updated.userPrices)
        }
    }

    fun reject(eventId: String) {
        _state.update { state ->
            state.copy(decisions = state.planner.reject(eventId).decisions)
        }
    }

    /** Puts every decision back to maybe, so the plan can be demoed more than once. */
    fun resetPlan() {
        _state.update { it.copy(decisions = emptyMap(), userPrices = emptyMap()) }
    }

    private fun updateFilters(block: (ScheduleFilters) -> ScheduleFilters) {
        _state.update { it.copy(filters = block(it.filters)) }
    }
}
