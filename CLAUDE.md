# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

**Name:** TODO: project name
**Event:** JetBrains Kotlin Multiplatform hackathon
**One-line pitch:** An event planner for Abu Dhabi. It finds Ticketmaster events, shows you the coming week matched to your interests, and (next) checks your budget and calendar so you can say Yes or No to each suggestion.

A Kotlin Multiplatform (KMP) app that runs on **Android and iOS** from a single shared Kotlin codebase, with the UI built in Compose Multiplatform.

### Hackathon priorities

1. **Both platforms must work.** A feature that only runs on Android is not done. Check iOS before calling anything finished.
2. **Always keep a demoable build.** Never leave `main` broken. Prefer small, working increments over big unfinished ones.
3. **Maximize shared code.** Put logic and UI in `commonMain`. Use platform code only where a platform API is genuinely required.
4. **Scope small.** Cut features before cutting polish on the core flow. Do not add libraries or abstractions the demo does not need.

## Product scope

**Working MVP only.** Build exactly what is listed, and nothing else, until it works on both Android and iOS.

### Current focus (build this now)

No calendar and no budget yet. Only this:

1. **Fetch events** from the Ticketmaster Discovery API for Abu Dhabi.
2. **Interests.** The user picks interests and types other hobbies in the app.
3. **This week.** A list of the next 7 days, grouped by day. Events matching the user's interests come first, and a switch hides the rest.

Status: built on branch `feature/events-and-interests`. The Ticketmaster call has not yet been tried with a real key, so it is not confirmed that Abu Dhabi events come back. Until then the app shows clearly labelled sample events.

### Ticketmaster key

- Use the regular **Discovery API** (`https://app.ticketmaster.com/discovery/v2/events.json`). The International Discovery API no longer issues new keys.
- Each person registers their own free key at the Ticketmaster developer portal, then adds `ticketmaster.apiKey=YOUR_KEY` to `local.properties` (gitignored) or sets the `TICKETMASTER_API_KEY` environment variable. The build turns it into a generated file, so it never enters the repo.
- With no key, or on any failure, the app shows sample events and says why. Do not remove this fallback.

### Full flow (after the current focus)

1. **Budget.** The user sets a budget in AED.
2. **Calendar.** The app reads the phone's calendar (read-only) and works out the free slots over the next few days. Manually entered free slots are the fallback if calendar permission is denied.
3. **Events.** The app fetches upcoming Abu Dhabi events from the Ticketmaster Discovery API.
4. **Match.** An event that fits inside a free slot and costs no more than the remaining budget becomes a **"maybe" suggestion**, shown semi-transparent.
5. **Decide.** The user taps **Yes** or **No** on each maybe.
   - **Yes:** the card turns solid and joins the plan. Its price comes off the remaining budget. Other maybes that now overlap it, or no longer fit the budget, disappear.
   - **No:** the event is dismissed and never suggested again.
6. **Plan.** A screen lists the accepted events and the remaining budget.

Assumption to confirm with the team: "maybe/opaque" means a tentative, semi-transparent suggestion that becomes solid on Yes.

### Event states

Every event is in exactly one state: `Maybe`, `Yes`, or `No`. Model this as a sealed type in `commonMain`.

### Rules

- Maybes are ordered simply: soonest first, then cheapest. No complex scoring yet.
- The calendar is read-only. Do not write events back to it.
- If Ticketmaster gives no price, show the event with a "price unknown" badge, do not subtract from the budget, and warn the user.
- Use `kotlinx-datetime` with the `Asia/Dubai` time zone (Abu Dhabi, UTC+4, no daylight saving).
- The Ticketmaster API key goes in `local.properties`, which is gitignored. Never commit it or paste it into chat.
- Do not scrape Luma or Partiful. Their terms restrict access to official interfaces.

### Behaviours to unit-test in `commonTest`

- An event outside every free slot is never a maybe.
- An event above the remaining budget is never a maybe.
- Tapping Yes reduces the remaining budget by the event's price.
- After a Yes, overlapping maybes and maybes that no longer fit the budget are removed.
- A No event never comes back.
- Two events cannot both be Yes if they overlap.

### Build order (do not skip ahead)

1. Domain model and matching logic in `commonMain`, with the tests above.
2. Screens using sample events and manually entered free slots: budget, the maybe list with Yes/No, and the plan.
3. Ticketmaster fetch (Ktor and kotlinx.serialization).
4. Phone calendar reader (`CalendarReader` interface with an `expect`/`actual` for Android and iOS).
5. Persist decisions on the device, then polish and a demo script.

### Out of scope for now

Location, going and travel (distance, directions), AI ranking, heat-aware scheduling, maps, routing, calendar-feed (`.ics`) import, accounts, a backend, and writing to the calendar. Revisit only after the flow above works on both platforms.

## Team and ownership

Two people work on this repo: `Areebah367` and `mu5tafa-m`. **Only Areebah367 has Xcode.** Kotlin/Native cannot compile the iOS targets without Xcode, so `mu5tafa-m` cannot build or run anything iOS locally.

| Owner | Work |
|---|---|
| `Areebah367` | Matching logic and tests, screens, iOS `actual` code (for example the `EventKit` calendar reader), and all iOS verification |
| `mu5tafa-m` | Ticketmaster client, the `CalendarReader` interface and its Android `actual` |

Rules that follow from this:

- Keep `commonMain` free of Android and JVM APIs, so it compiles for iOS even when the author cannot check it.
- Never claim iOS works unless the iOS compile task has passed, locally or in CI. If you cannot run it on this machine, say so and point to the CI result.
- iOS `actual` implementations and iOS verification go through `Areebah367`.
- Work on a branch (`feature/<name>`) and open a pull request. Never push directly to `main`. Run `git pull --rebase origin main` before you start.
- The GitHub Actions workflow in `.github/workflows/build.yml` builds Android and compiles iOS on every pull request. Do not merge a pull request with a red check.
- Agree on the shape of the `Event` model before writing code that depends on it. Both people build on it.

## Tech stack

Already in the project (from the KMP wizard; versions live in `gradle/libs.versions.toml`):

- **Language:** Kotlin 2.4.x (K2 compiler)
- **UI:** Compose Multiplatform 1.12.x with Material 3 (shared UI for Android and iOS)
- **Build:** Gradle with Kotlin DSL, a version catalog, and Android Gradle Plugin 9.x
- **Lifecycle:** `androidx.lifecycle` ViewModel and runtime for Compose (multiplatform artifacts)
- **Android:** `minSdk` 24, `compileSdk` and `targetSdk` 37, JVM target 11
- **Async:** kotlinx.coroutines and Flow
- **Networking:** Ktor Client 3.x (`OkHttp` engine on Android, `Darwin` engine on iOS), with JSON content negotiation
- **Serialization:** kotlinx.serialization
- **Dates:** kotlinx-datetime, always in the `Asia/Dubai` zone for Abu Dhabi

Not added yet. Add only when a feature needs it, and ask first:

- **DI:** Koin (or manual constructor injection if the project stays small)
- **Persistence:** Room KMP or SQLDelight; DataStore or multiplatform-settings for key/value (interests are in memory only for now)
- **Images:** Coil 3
- **Navigation:** Navigation Compose (multiplatform) or Voyager/Decompose. Pick one and do not mix them. Today the app switches between two screens with a bottom bar.

Add dependencies only through the version catalog. Confirm a library supports both Android and iOS targets (`iosArm64` and `iosSimulatorArm64`) before adding it.

## Project structure

This is the layout generated by the JetBrains KMP wizard with "Share UI" enabled. The Android app is a separate module from the shared code.

```
.
├── shared/                      # Kotlin Multiplatform module: all shared UI and logic
│   └── src/
│       ├── commonMain/          # Shared code: UI, ViewModels, data, domain (most code goes here)
│       │   ├── kotlin/          #   App.kt is the root composable
│       │   └── composeResources/ # Shared drawables, strings, fonts (accessed via `Res`)
│       ├── commonTest/          # Shared tests
│       ├── androidMain/         # Android-only actuals
│       ├── androidHostTest/     # Android unit tests (run on the JVM)
│       ├── iosMain/             # iOS-only actuals, MainViewController (entry point for iOS)
│       └── iosTest/             # iOS tests
├── androidApp/                  # Android application module (MainActivity, manifest, launcher icons)
├── iosApp/                      # Xcode project (thin SwiftUI host that embeds the shared Compose UI)
├── gradle/libs.versions.toml    # Dependency versions
└── build.gradle.kts / settings.gradle.kts
```

The base package is `areebah.nyuad4jetbrains.project`. Android application ID: `areebah.nyuad4jetbrains.project`. The iOS framework is named `Shared`.

Inside `shared/src/commonMain/kotlin/areebah/nyuad4jetbrains/project/`, prefer feature-based packages as the app grows:

```
├── ui/            # Composables, theme, navigation
├── feature/<x>/   # Screen + ViewModel + state per feature
├── data/          # Repositories, API clients, DTOs, local storage
├── domain/        # Models and use cases (only if they add clarity)
└── di/            # DI modules (if a DI library is added)
```

The existing `Platform.kt` with `Platform.android.kt` and `Platform.ios.kt` is the wizard's `expect`/`actual` example. Follow that pattern for platform differences.

## Common commands

Run from the repo root.

```bash
# Build the Android debug APK
./gradlew :androidApp:assembleDebug

# Install and run on a connected device or emulator
./gradlew :androidApp:installDebug

# Run shared tests on Android (JVM)
./gradlew :shared:testAndroidHostTest

# Run shared tests on the iOS simulator
./gradlew :shared:iosSimulatorArm64Test

# Compile the shared iOS code (quick check that iOS code compiles; needs Xcode installed)
./gradlew :shared:compileKotlinIosSimulatorArm64

# Clean
./gradlew clean
```

**Running on iOS:** open `iosApp/iosApp.xcodeproj` in Xcode and press Run, or use the run configuration in Android Studio / IntelliJ IDEA with the KMP plugin. Requires macOS with Xcode installed. Xcode builds the `Shared` framework from the `shared` module through Gradle.

After changing shared code, run **both** the Android build and the iOS compile task above.

## Coding conventions

### Kotlin

- Follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Prefer `val`, immutable data classes, and sealed types for state and results.
- Use coroutines and Flow. No callbacks, no `GlobalScope`. Scope work to a `ViewModel` or a composable's lifecycle.
- No `!!`. Handle nullability explicitly.
- Keep functions small and names descriptive. Skip comments that restate the code; comment the *why*.

### Compose

- Screens are stateless composables that take a `UiState` and event lambdas. State lives in a `ViewModel`.
- Expose state as `StateFlow<UiState>` and collect with `collectAsState()` (or `collectAsStateWithLifecycle()` where available).
- Hoist state. Pass lambdas down, not ViewModels.
- Provide a `Modifier` parameter on reusable composables and put it first among the optional parameters.
- Use Material 3 theme tokens (colors, typography, shapes). Do not hardcode colors or text sizes.
- `@Preview` composables work in `commonMain` in this project (`ui-tooling-preview` is a dependency of `shared`), so previews can live next to the screens they show.

### Multiplatform rules

- **Default to `commonMain`.** Only drop to `androidMain` or `iosMain` when a platform API is required.
- Use `expect`/`actual` for small platform differences (for example, platform name, file paths, haptics). For larger differences, define an interface in `commonMain` and inject the platform implementation.
- Never import `android.*`, `androidx.activity.*`, `java.*`, or `platform.UIKit.*`/`platform.Foundation.*` in `commonMain`. It will break the other platform.
- Do not use JVM-only APIs in shared code (`java.util.*`, `String.format`, `System.currentTimeMillis`, `SimpleDateFormat`, etc.). Use `kotlinx-datetime`, `kotlin.time`, or Kotlin stdlib equivalents.
- Resources (strings, images, fonts) go in `composeResources` and are accessed through the generated `Res` class.
- Any platform code must have both an `actual` for Android **and** iOS. Do not leave one as `TODO()`.

## Workflow for Claude

1. **Read before writing.** Look at existing screens and modules and match their patterns.
2. **Make the smallest change that works,** in `commonMain` first.
3. **Verify on both platforms.** Run the Android build and the iOS compile task. If you cannot run one, say so instead of assuming it works.
4. **Run tests** with `./gradlew :shared:testAndroidHostTest :shared:iosSimulatorArm64Test` when logic changes.
5. **Report what you did and did not verify.** State failures with the actual error output.

### Do

- Ask before adding a new dependency, module, or architecture layer.
- Keep commits small and focused, with clear messages (`feat:`, `fix:`, `chore:`, `docs:`).
- Handle loading, empty, and error states in every screen that fetches data.
- Keep the demo flow in mind: the most important path should be smooth, fast, and good-looking.

### Don't

- Don't commit secrets, API keys, `local.properties`, keystores, or `xcuserdata`. Read keys from `local.properties` or environment variables and document required keys in the README.
- Don't edit generated files or anything under `build/`.
- Don't hand-edit `iosApp/*.xcodeproj/project.pbxproj` unless necessary. Prefer changes in Gradle or Kotlin.
- Don't upgrade Kotlin, Compose Multiplatform, or AGP versions mid-hackathon unless something is blocked. Version bumps eat time.
- Don't refactor working code right before the demo.

## Troubleshooting

- **iOS build fails after Gradle changes:** run `./gradlew clean`, then rebuild. In Xcode, use Product > Clean Build Folder.
- **"Unresolved reference" in `commonMain` for a platform class:** it is platform-specific. Move it behind `expect`/`actual` or an interface.
- **Works on Android, crashes on iOS:** check for JVM-only APIs, missing `actual` implementations, and (once Ktor is added) the `Darwin` engine setup.
- **Gradle sync issues:** use the Gradle wrapper (`./gradlew`). The build needs JDK 21, which Gradle downloads automatically (see `gradle/gradle-daemon-jvm.properties`), so the system JDK version does not matter.
- **Kotlin/Native builds are slow:** use `compileKotlinIosSimulatorArm64` for quick checks and avoid full clean builds unless needed.

## Setup

Prerequisites: Android Studio or IntelliJ IDEA with the Kotlin Multiplatform plugin, Android SDK, and (for iOS) macOS with Xcode.

```bash
git clone <repo-url>
cd <repo>
./gradlew :androidApp:assembleDebug
```

Then open the project in Android Studio for Android, and `iosApp/iosApp.xcodeproj` in Xcode (or use the iOS run configuration) for iOS.

## Demo checklist

- [ ] App launches cleanly on an Android emulator or device
- [ ] App launches cleanly on an iOS simulator or device
- [ ] Core flow works end to end on both platforms
- [ ] No crashes on rotation, backgrounding, or a missing network connection
- [ ] README has the pitch, screenshots, and run instructions
- [ ] No secrets in the repository
