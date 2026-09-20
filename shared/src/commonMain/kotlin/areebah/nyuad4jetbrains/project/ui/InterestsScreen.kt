package areebah.nyuad4jetbrains.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import areebah.nyuad4jetbrains.project.domain.InterestProfile
import areebah.nyuad4jetbrains.project.domain.Interests

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    profile: InterestProfile,
    themeMode: ThemeMode,
    onToggleInterest: (String) -> Unit,
    onOtherHobbiesChange: (String) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onShowWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("What are you into?", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Pick a few. Matching events in Abu Dhabi and Dubai go to the top of your list.",
            style = MaterialTheme.typography.bodyMedium,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Interests.all.forEach { interest ->
                FilterChip(
                    selected = interest.label in profile.selectedLabels,
                    onClick = { onToggleInterest(interest.label) },
                    label = { Text(interest.label) },
                )
            }
        }

        OutlinedTextField(
            value = profile.otherHobbies,
            onValueChange = onOtherHobbiesChange,
            label = { Text("Other hobbies") },
            placeholder = { Text("e.g. chess, padel, jazz") },
            supportingText = { Text("Separate with commas") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = onShowWeek, modifier = Modifier.fillMaxWidth()) {
            Text("Show what's on")
        }

        Text("Appearance", style = MaterialTheme.typography.titleSmall)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                ) {
                    Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}
