# CLAUDE.md

Guidance for Claude Code working in this repository. Two people and two Claude sessions work here in
parallel, so the ownership and contract sections below are not advisory — ignoring them causes merge
conflicts and wasted work.

## Project

**Name:** TODO — still unnamed. The repo is called `JetBrains-Hackathon`; pick something real.
**Event:** JetBrains Kotlin Multiplatform hackathon.
**Pitch:** An event planner for Abu Dhabi and Dubai. It gathers what's on, matches it to your
interests, and — next — checks your budget and calendar so you can say Yes or No to each suggestion.

A Kotlin Multiplatform app running on **Android and iOS** from one shared Kotlin codebase, UI in
Compose Multiplatform.

### Priorities

1. **Both platforms must work.** A feature that only runs on Android is not done.
2. **Always keep a demoable build.** Never leave `main` broken. Small working increments beat big
   unfinished ones.
3. **Maximise shared code.** Logic and UI in `commonMain`; platform code only where an OS API demands it.
4. **Scope small.** Cut features before cutting polish on the core flow.

## Product scope

### Built

Interests (pick chips, type free-text hobbies) and **What's on**: events grouped by day, filtered by
horizon (This week / This month / Everything) and city, with interest-matching events first and a
switch to hide the rest. Events come from a curated list merged with live Ticketmaster data.

### Next

1. **Planner engine** (#5) — budget, free slots, and the Maybe/Yes/No rules. Pure logic in `planner/`.
2. **Screens** (#6) — budget input, Yes/No on suggestion cards, and a plan screen.
3. **Calendar** (#4) — read the phone's calendar for busy time, read-only. Manual free slots are the
   fallback when permission is denied.
4. **Persistence** — interests and decisions survive a restart. Currently in memory only.

### Out of scope

Location and travel (distance, directions), AI ranking, heat-aware scheduling, maps, routing,
calendar-feed (`.ics`) import, accounts, a backend, writing to the calendar, social features
(referrals, attendee counts, avatars), and event creation.

**If a new feature is proposed, say which of the four items above it displaces before building it.**

## Facts that drive the design

Measured against the real Ticketmaster API on 2026-09-20. Do not undo the workarounds these produced
without re-measuring.

- **76 UAE events**: 33 Abu Dhabi, 43 Dubai. Music 29, Arts & Theatre 25, Sports 14, Misc 8.
- **No prices anywhere.** 0 of 133 events carried `priceRanges` — not in list results, not on the
  detail endpoint, not even for 100 London events. **Nothing may assume Ticketmaster gives a price.**
- **Nothing soon.** Abu Dhabi events ran 2026-10-11 to 2027-01-31: 1 within 30 days, 0 within 7.
  This is why a fixed 7-day window was replaced by a horizon filter.
- **Images are present**: all 76 events carry 16:9, 3:2 and 4:3 images up to 640×360. Curated events
  have none, so any image-led design needs a category-coloured fallback.

### Event sources

| Source | File | Role |
|---|---|---|
| Curated | `data/CuratedEvents.kt` | Community events typed in by hand. The only source of real prices, end times, and near-term events. |
| Ticketmaster | `data/TicketmasterApi.kt` | Live, for large ticketed shows booked months ahead. |
| Sample | `data/SampleEvents.kt` | Made-up. Shown only when the other two produce nothing. |

The repository merges curated and live, dedupes by id, and **a Ticketmaster failure must never hide
the curated events** — it only adds a notice.

**Curated events are typed in by a person reading a page. Never scrape.** Luma's and Partiful's
terms restrict access to their own interfaces. Their fixed dates go stale, so refresh them.

### Ticketmaster key

Use the regular Discovery API (`https://app.ticketmaster.com/discovery/v2/events.json`); the
International Discovery API no longer issues keys. Each person registers a free key (5,000/day) and
puts `ticketmaster.apiKey=...` in `local.properties` (gitignored) or sets `TICKETMASTER_API_KEY`.
The build writes it into a generated file, so it never enters the repo. Without a key the app still
runs on curated events.

## Team and file ownership

`Areebah367` and `mu5tafa-m`. **Both have Xcode**, so both can compile iOS locally
(`compileKotlinIosSimulatorArm64`) — and should, before pushing. Running the app or
`iosSimulatorArm64Test` additionally needs a simulator runtime, which is a separate download, so say
which of the two you actually did.

| Owner | Files | Work |
|---|---|---|
| `Areebah367` | `ui/`, `domain/`, `data/`, `App.kt`, `iosMain/`, `iosApp/` | Screens (#6), iOS calendar reader (#4) |
| `mu5tafa-m` | `planner/`, `calendar/`, `androidMain/calendar/` | Planner engine (#5), Android calendar reader |

- **Do not edit files you do not own.** If you need a change there, ask; do not reach across.
- Shared files (`gradle/libs.versions.toml`, `shared/build.gradle.kts`, `AndroidManifest.xml`) take
  the smallest possible change, and only after asking.
- Branch per task (`feature/<name>`), one PR each. **Never push to `main`.** Rebase before pushing.
- CI (`.github/workflows/build.yml`) builds Android, compiles iOS, and runs tests on every PR.
  **Never merge a red check**, and never claim iOS works without a passing iOS compile.
- iOS `actual` code and everything under `iosApp/` stays with `Areebah367`, so the Swift host and
  each `expect`/`actual` pair have a single owner.

## Contracts

Changing either of these needs both people to agree, because both build on them.

### `Event` — `domain/Event.kt`

Carries `city`, an optional `end` (curated events have one, Ticketmaster events never do),
`soldOut`, and `source` (`TICKETMASTER` / `CURATED` / `SAMPLE`). Prices are
`priceMin` / `priceMax` / `currency` and are **usually null**.

- Show "Price unknown" — never substitute zero in the UI.
- Use `end` when present; assume a duration only when it is absent.
- Times are local UAE time; the zone is `Asia/Dubai` (UTC+4, no daylight saving) for both cities.

### `PlannerState` — `planner/PlannerState.kt`

`Decision` is `MAYBE` / `YES` / `NO`, held in a map keyed by `Event.id` where absent means `MAYBE`.

An event is a **maybe** only when it is undecided, fits entirely inside one free slot, costs no more
than the remaining budget, is **not sold out**, and does not overlap an accepted event. Order
soonest first, then cheapest.

An unknown price counts as 0 against the budget, so **every unknown-price event passes the budget
filter**. That is deliberate and the screens must surface it. Letting the user type a price when
they accept an event (`userPrices`, `accept(eventId, price)`) is the intended fix.

## Tech stack

In the project (versions in `gradle/libs.versions.toml`):

- Kotlin 2.4.x (K2), Compose Multiplatform 1.12.x with Material 3, AGP 9.x, Gradle Kotlin DSL
- `androidx.lifecycle` ViewModel and runtime for Compose
- kotlinx.coroutines, kotlinx.serialization, kotlinx-datetime
- Ktor Client 3.x (`OkHttp` on Android, `Darwin` on iOS) with JSON content negotiation
- Android `minSdk` 24, `compileSdk`/`targetSdk` 37, JVM target 11

Not added — **ask first**: DI (Koin), persistence (Room KMP / SQLDelight / DataStore /
multiplatform-settings), images (Coil 3), navigation (the app currently swaps two screens behind a
bottom bar). Add dependencies only through the version catalog, and confirm both `iosArm64` and
`iosSimulatorArm64` are supported first.

## Project structure

```
shared/src/
├── commonMain/kotlin/areebah/nyuad4jetbrains/project/
│   ├── domain/     # Event, Interests, Schedule, Format
│   ├── data/       # TicketmasterApi, CuratedEvents, SampleEvents, EventRepository
│   ├── ui/         # AppViewModel, InterestsScreen, WhatsOnScreen
│   ├── planner/    # FreeSlot, PlannerState
│   ├── calendar/   # CalendarReader
│   └── App.kt
├── commonTest/     # shared tests
├── desktopMain/    # DesktopPreview.kt — preview entry point only
├── androidMain/    # Android actuals, AndroidCalendarReader
├── androidHostTest/# JVM unit tests
├── iosMain/        # iOS actuals, MainViewController
└── iosTest/
androidApp/         # MainActivity, manifest, icons
iosApp/             # Xcode project hosting the shared Compose UI
```

Base package and Android application ID: `areebah.nyuad4jetbrains.project`. iOS framework: `Shared`.

## Commands

```bash
./gradlew :androidApp:assembleDebug              # Android APK            (needs the Android SDK)
./gradlew :shared:testAndroidHostTest            # shared tests on the JVM (needs the Android SDK)
./gradlew :shared:compileKotlinIosSimulatorArm64 # iOS compile      (needs Xcode, NOT the Android SDK)
./gradlew :shared:iosSimulatorArm64Test          # iOS tests   (needs Xcode + a simulator runtime)
./gradlew :shared:run                            # desktop preview window (no simulator needed)
./gradlew clean
```

**iOS without the Android SDK works.** `compileKotlinIosSimulatorArm64` was verified to succeed with
no Android SDK installed; the Android tasks fail with "SDK location not found". The first
Kotlin/Native run downloads an LLVM toolchain (~24 min), then builds take seconds.

**Xcode ships no simulator runtime.** `Simulator.app` and `iPhoneSimulator.platform` are bundled, but
runtimes are a separate multi-GB download (`xcodebuild -downloadPlatform iOS`, or Xcode → Settings →
Components). Compiling works without one; running and `iosSimulatorArm64Test` do not.

To run on iOS: open `iosApp/iosApp.xcodeproj` in Xcode and press Run. Xcode builds the `Shared`
framework through Gradle.

**The desktop target is a preview tool, not a product target.** `./gradlew :shared:run` opens the
shared Compose UI in a phone-sized window so it can be looked at without a simulator or emulator.
Android and iOS are what ship; something working only on desktop is not done. It shares
`commonMain`, so CI compiles it to stop it rotting.

## Conventions

### Kotlin

Follow the [official conventions](https://kotlinlang.org/docs/coding-conventions.html). Prefer `val`,
immutable data classes, and sealed types for state. Coroutines and Flow — no callbacks, no
`GlobalScope`. No `!!`. Comment the *why*, not the what.

### Compose

Screens are stateless composables taking a `UiState` and event lambdas; state lives in a `ViewModel`
and is exposed as `StateFlow`. Hoist state, pass lambdas rather than ViewModels. Give reusable
composables a `Modifier` parameter, first among the optional ones. Use Material 3 theme tokens — do
not hardcode colours or text sizes. `@Preview` works in `commonMain` here.

### Multiplatform

Default to `commonMain`. Use `expect`/`actual` for small platform differences; for larger ones define
an interface in `commonMain` and inject the implementation. **Never import `android.*`,
`androidx.activity.*`, `java.*`, `platform.UIKit.*` or `platform.Foundation.*` into `commonMain`**,
and avoid JVM-only APIs (`java.util.*`, `String.format`, `System.currentTimeMillis`,
`SimpleDateFormat`) — use kotlinx-datetime, `kotlin.time`, or stdlib. Every `expect` needs both
actuals; never leave one as `TODO()`. Resources go in `composeResources`, reached via `Res`.

## Working rules

1. **Read before writing.** Match the patterns already in the file you are changing.
2. **Smallest change that works**, in `commonMain` first.
3. **Verify, and say what you did not verify.** Run the iOS compile and, where possible, the tests.
   If you cannot run something on this machine, say so and point at the CI result instead of assuming.
4. **Report failures with the actual output.**

### Do

- Ask before adding a dependency, module, or architecture layer.
- Small focused commits (`feat:`, `fix:`, `chore:`, `docs:`).
- Handle loading, empty, and error states on every screen that fetches.
- Keep the demo path smooth: it is what gets judged.

### Don't

- Don't commit secrets, `local.properties`, keystores, or `xcuserdata`.
- Don't edit generated files or anything under `build/`.
- Don't hand-edit `iosApp/*.xcodeproj/project.pbxproj` unless there is no alternative.
- Don't bump Kotlin, Compose Multiplatform, or AGP mid-hackathon.
- Don't refactor working code right before the demo.

## Troubleshooting

- **iOS build fails after Gradle changes:** `./gradlew clean`, then rebuild; in Xcode, Product →
  Clean Build Folder.
- **"Unresolved reference" in `commonMain`:** the symbol is platform-specific. Put it behind
  `expect`/`actual` or an interface.
- **Works on Android, crashes on iOS:** look for JVM-only APIs, a missing `actual`, or the Ktor
  `Darwin` engine.
- **"SDK location not found":** an Android task with no Android SDK. Use the iOS compile task, or
  install the SDK.
- **"Xcode does not support simulator tests":** no simulator runtime installed. See Commands above.
- **Gradle issues:** always use `./gradlew`. It fetches JDK 21 itself
  (`gradle/gradle-daemon-jvm.properties`), so the system JDK does not matter.

## Demo checklist

- [ ] Launches cleanly on an Android emulator or device
- [ ] Launches cleanly on an iOS simulator or device
- [ ] Core flow works end to end on both platforms
- [ ] Curated events are still in the future — refresh them if not
- [ ] No crash on rotation, backgrounding, or no network
- [ ] README has the pitch, screenshots, and run instructions
- [ ] No secrets in the repository
