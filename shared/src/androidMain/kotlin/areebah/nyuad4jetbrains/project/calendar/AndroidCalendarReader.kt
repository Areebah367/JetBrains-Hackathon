package areebah.nyuad4jetbrains.project.calendar

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import areebah.nyuad4jetbrains.project.domain.AbuDhabiTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Reads busy time from the Android device calendar through [CalendarContract.Instances].
 *
 * Read-only: nothing is ever written back. All-day events are skipped, since they say nothing
 * about which hours are actually taken. Times come back in the `Asia/Dubai` zone.
 *
 * This class is deliberately not wired into the app. `Areebah367` connects it to the UI along
 * with the permission request, and provides the iOS EventKit counterpart (issue #4).
 */
class AndroidCalendarReader(
    private val contentResolver: ContentResolver,
    private val hasPermission: () -> Boolean,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CalendarReader {

    /**
     * Uses [context] for both the resolver and the READ_CALENDAR permission check.
     * Pass the application context, so the reader does not outlive an Activity.
     */
    constructor(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        contentResolver = context.contentResolver,
        hasPermission = {
            context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED
        },
        dispatcher = dispatcher,
    )

    override suspend fun busyBlocks(from: LocalDate, days: Int): CalendarResult {
        if (!hasPermission()) return CalendarResult.PermissionDenied
        if (days <= 0) return CalendarResult.Success(emptyList())

        val zone = AbuDhabiTime.zone
        val windowStart = from.atStartOfDayIn(zone)
        val windowEnd = windowStart + days.days

        return withContext(dispatcher) {
            runCatching { query(windowStart, windowEnd) }
                .fold(
                    onSuccess = { CalendarResult.Success(it) },
                    // A missing provider or a revoked permission surfaces here as an exception.
                    onFailure = { CalendarResult.Unavailable(it.message ?: it::class.simpleName.orEmpty()) },
                )
        }
    }

    private fun query(windowStart: Instant, windowEnd: Instant): List<BusyBlock> {
        // Instances expands recurring events, so each repeat comes back as its own row.
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .let { ContentUris.appendId(it, windowStart.toEpochMilliseconds()) }
            .let { ContentUris.appendId(it, windowEnd.toEpochMilliseconds()) }
            .build()

        val cursor: Cursor = contentResolver.query(uri, PROJECTION, null, null, null)
            ?: return emptyList()

        return cursor.use { rows ->
            buildList {
                while (rows.moveToNext()) {
                    if (rows.getInt(COLUMN_ALL_DAY) == 1) continue
                    if (rows.getInt(COLUMN_STATUS) == CalendarContract.Instances.STATUS_CANCELED) continue

                    val begin = rows.getLong(COLUMN_BEGIN)
                    val end = rows.getLong(COLUMN_END)
                    if (end <= begin) continue

                    add(
                        BusyBlock(
                            start = Instant.fromEpochMilliseconds(begin).toLocalDateTime(AbuDhabiTime.zone),
                            end = Instant.fromEpochMilliseconds(end).toLocalDateTime(AbuDhabiTime.zone),
                        )
                    )
                }
            }
        }
    }

    private companion object {
        val PROJECTION = arrayOf(
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.STATUS,
        )

        const val COLUMN_BEGIN = 0
        const val COLUMN_END = 1
        const val COLUMN_ALL_DAY = 2
        const val COLUMN_STATUS = 3
    }
}
