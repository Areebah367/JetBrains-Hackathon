package areebah.nyuad4jetbrains.project.domain

/** A pickable interest and the words that indicate an event belongs to it. */
data class Interest(val label: String, val keywords: List<String>)

object Interests {
    val all: List<Interest> = listOf(
        Interest("Music", listOf("music", "concert", "dj", "festival", "orchestra")),
        Interest(
            "Sports",
            listOf("sport", "football", "soccer", "basketball", "tennis", "racing", "cricket", "boxing", "mma", "golf", "rugby"),
        ),
        Interest("Comedy", listOf("comedy", "stand-up", "standup", "comedian")),
        Interest("Theatre & arts", listOf("theatre", "theater", "arts", "art", "ballet", "opera", "musical", "dance", "exhibition")),
        Interest("Family", listOf("family", "kids", "children")),
        Interest("Film", listOf("film", "cinema", "movie", "screening")),
    )
}

/** What the user told us they like: picked interests plus free-text hobbies. */
data class InterestProfile(
    val selectedLabels: Set<String> = emptySet(),
    val otherHobbies: String = "",
) {
    /** The free-text hobbies as separate lowercase words or phrases. */
    val hobbyWords: List<String>
        get() = otherHobbies
            .split(',', ';', '\n')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

    val isEmpty: Boolean get() = selectedLabels.isEmpty() && hobbyWords.isEmpty()
}

private val wordSeparators = Regex("[^a-z0-9]+")

/**
 * Which of the user's interests and hobbies this event matches, in display form.
 * Matching looks at the event's name, category, and genre. Short keywords must match a whole word;
 * longer ones also match longer forms ("concert" matches "concerts").
 */
fun matchedInterests(event: Event, profile: InterestProfile): List<String> {
    val text = listOfNotNull(event.name, event.category, event.genre).joinToString(" ").lowercase()
    val words = text.split(wordSeparators).filter { it.isNotEmpty() }.toSet()

    val fromPicks = Interests.all
        .filter { it.label in profile.selectedLabels && it.keywords.any { keyword -> matchesKeyword(text, words, keyword) } }
        .map { it.label }
    val fromHobbies = profile.hobbyWords.filter { matchesKeyword(text, words, it) }
    return fromPicks + fromHobbies
}

private fun matchesKeyword(text: String, words: Set<String>, keyword: String): Boolean = when {
    ' ' in keyword || '-' in keyword -> text.contains(keyword)
    keyword.length < 4 -> keyword in words
    else -> words.any { it.startsWith(keyword) }
}
