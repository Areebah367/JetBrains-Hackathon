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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class UiState(
    val profile: InterestProfile = InterestProfile(),
    val filters: ScheduleFilters = ScheduleFilters(),
    val loading: Boolean = true,
    val today: LocalDate? = null,
    val result: EventsResult? = null,
)

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
            _state.update { it.copy(loading = false, today = today, result = result) }
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

    /** Tapping a selected city clears the filter, so the chips behave as "all" when none is on. */
    fun toggleCity(city: String) = updateFilters { filters ->
        filters.copy(cities = if (city in filters.cities) filters.cities - city else filters.cities + city)
    }

    private fun updateFilters(block: (ScheduleFilters) -> ScheduleFilters) {
        _state.update { it.copy(filters = block(it.filters)) }
    }
}
