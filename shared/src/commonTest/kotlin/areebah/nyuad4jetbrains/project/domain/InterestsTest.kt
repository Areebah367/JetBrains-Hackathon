package areebah.nyuad4jetbrains.project.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterestsTest {
    private val music = InterestProfile(selectedLabels = setOf("Music"))

    @Test
    fun pickedInterestMatchesTheEventCategory() {
        val event = testEvent("1", "Sunset Sessions", category = "Music")
        assertEquals(listOf("Music"), matchedInterests(event, music))
    }

    @Test
    fun longKeywordsMatchLongerForms() {
        val event = testEvent("1", "Summer Concerts Series")
        assertEquals(listOf("Music"), matchedInterests(event, music))
    }

    @Test
    fun shortKeywordsNeedAWholeWord() {
        val djNight = testEvent("1", "DJ Night")
        val adjust = testEvent("2", "Adjustable Chairs Expo")
        assertEquals(listOf("Music"), matchedInterests(djNight, music))
        assertTrue(matchedInterests(adjust, music).isEmpty())
    }

    @Test
    fun aNotPickedInterestDoesNotMatch() {
        val event = testEvent("1", "Sunset Sessions", category = "Music")
        assertTrue(matchedInterests(event, InterestProfile(selectedLabels = setOf("Sports"))).isEmpty())
    }

    @Test
    fun freeTextHobbiesMatchTheEventName() {
        val event = testEvent("1", "Abu Dhabi Chess Open")
        val profile = InterestProfile(otherHobbies = "Chess, padel")
        assertEquals(listOf("chess"), matchedInterests(event, profile))
    }

    @Test
    fun noInterestsMeansNoMatches() {
        val event = testEvent("1", "Live Jazz", category = "Music")
        assertTrue(matchedInterests(event, InterestProfile()).isEmpty())
        assertTrue(InterestProfile().isEmpty)
    }

    @Test
    fun hobbiesAreSplitOnCommasAndTrimmed() {
        assertEquals(listOf("chess", "board games"), InterestProfile(otherHobbies = " Chess , board games ,").hobbyWords)
    }
}
