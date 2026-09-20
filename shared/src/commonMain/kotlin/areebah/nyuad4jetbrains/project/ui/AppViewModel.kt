package areebah.nyuad4jetbrains.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import areebah.nyuad4jetbrains.project.data.EventRepository
import areebah.nyuad4jetbrains.project.data.EventsResult
import areebah.nyuad4jetbrains.project.data.defaultEventRepository
import areebah.nyuad4jetbrains.project.domain.AbuDhabiTime
import areebah.nyuad4jetbrains.project.domain.InterestProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class UiState(
    val profile: InterestProfile = InterestProfile(),
    val onlyMatching: Boolean = false,
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
            val result = repository.loadWeek(today)
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

    fun setOnlyMatching(value: Boolean) {
        _state.update { it.copy(onlyMatching = value) }
    }
}
