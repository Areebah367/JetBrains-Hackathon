package areebah.nyuad4jetbrains.project.ui

import kotlin.test.Test
import kotlin.test.assertTrue

class StringsTest {
    @Test
    fun everyInterestHasATranslation() {
        val missing = untranslatedInterests()
        assertTrue(
            missing.isEmpty(),
            "these interests would show their English label in every language: $missing",
        )
    }
}
