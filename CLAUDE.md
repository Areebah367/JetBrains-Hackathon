# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

**Name:** TODO: project name
**Event:** JetBrains Kotlin Multiplatform hackathon
**One-line pitch:** TODO: what the app does and who it is for

A Kotlin Multiplatform (KMP) app that runs on **Android and iOS** from a single shared Kotlin codebase, with the UI built in Compose Multiplatform.

### Hackathon priorities

1. **Both platforms must work.** A feature that only runs on Android is not done. Check iOS before calling anything finished.
2. **Always keep a demoable build.** Never leave `main` broken. Prefer small, working increments over big unfinished ones.
3. **Maximize shared code.** Put logic and UI in `commonMain`. Use platform code only where a platform API is genuinely required.
4. **Scope small.** Cut features before cutting polish on the core flow. Do not add libraries or abstractions the demo does not need.

## Tech stack

- **Language:** Kotlin (latest stable, K2 compiler)
- **UI:** Compose Multiplatform (shared UI for Android and iOS)
- **Build:** Gradle with Kotlin DSL and a version catalog (`gradle/libs.versions.toml`)
- **Async:** kotlinx.coroutines and Flow
- **Networking:** Ktor Client (`OkHttp` engine on Android, `Darwin` engine on iOS)
- **Serialization:** kotlinx.serialization
- **DI:** Koin (or manual constructor injection if the project stays small)
- **Persistence (if needed):** Room KMP or SQLDelight; DataStore or multiplatform-settings for key/value
- **Images (if needed):** Coil 3
- **Navigation:** Navigation Compose (multiplatform) or Voyager/Decompose, whichever is already in the project. Do not mix them.

Add dependencies only through the version catalog. Confirm a library supports both Android and iOS targets before adding it.

## Project structure

Based on the standard JetBrains KMP wizard layout. Adjust if this repo differs.

```
.
├── composeApp/                  # Shared Kotlin module + Android app entry point
│   └── src/
│       ├── commonMain/          # Shared code: UI, ViewModels, data, domain (most code goes here)
│       ├── commonTest/          # Shared tests
│       ├── androidMain/         # Android-only actuals, MainActivity, Android APIs
│       └── iosMain/             # iOS-only actuals, MainViewController, iOS APIs
├── iosApp/                      # Xcode project (thin SwiftUI host that embeds the Compose UI)
├── gradle/libs.versions.toml    # Dependency versions
└── build.gradle.kts / settings.gradle.kts
```

Inside `commonMain`, prefer feature-based packages:

```
com.example.app/
├── ui/            # Composables, theme, navigation
├── feature/<x>/   # Screen + ViewModel + state per feature
├── data/          # Repositories, API clients, DTOs, local storage
├── domain/        # Models and use cases (only if they add clarity)
└── di/            # Koin modules
```

## Common commands

Run from the repo root.

```bash
# Build the Android debug APK
./gradlew :composeApp:assembleDebug

# Install and run on a connected device or emulator
./gradlew :composeApp:installDebug

# Run shared and Android unit tests
./gradlew :composeApp:allTests

# Run iOS simulator tests
./gradlew :composeApp:iosSimulatorArm64Test

# Compile the iOS framework (quick check that iOS code compiles, no Xcode needed)
./gradlew :composeApp:compileKotlinIosSimulatorArm64

# Clean
./gradlew clean
```

**Running on iOS:** open `iosApp/iosApp.xcodeproj` in Xcode and press Run, or use the run configuration in Android Studio / IntelliJ IDEA with the KMP plugin. Requires macOS with Xcode installed.

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
- Add `@Preview` only in `androidMain`/`desktopMain` if the tooling in this project supports it. Do not rely on previews in `commonMain` unless configured.

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
4. **Run tests** with `./gradlew :composeApp:allTests` when logic changes.
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
- **Works on Android, crashes on iOS:** check for JVM-only APIs, missing `actual` implementations, and Ktor engine setup for `Darwin`.
- **Gradle sync issues:** confirm JDK 17+ is selected and the Gradle wrapper is being used.
- **Kotlin/Native builds are slow:** use `compileKotlinIosSimulatorArm64` for quick checks and avoid full clean builds unless needed.

## Setup

Prerequisites: JDK 17+, Android Studio or IntelliJ IDEA with the Kotlin Multiplatform plugin, Android SDK, and (for iOS) macOS with Xcode.

```bash
git clone <repo-url>
cd <repo>
./gradlew :composeApp:assembleDebug
```

Then open the project in Android Studio for Android, and `iosApp/iosApp.xcodeproj` in Xcode (or use the iOS run configuration) for iOS.

## Demo checklist

- [ ] App launches cleanly on an Android emulator or device
- [ ] App launches cleanly on an iOS simulator or device
- [ ] Core flow works end to end on both platforms
- [ ] No crashes on rotation, backgrounding, or a missing network connection
- [ ] README has the pitch, screenshots, and run instructions
- [ ] No secrets in the repository
