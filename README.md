# JetBrains-Hackathon

A **Kotlin Multiplatform** event planner for Abu Dhabi and Dubai, built for the JetBrains Kotlin Multiplatform hackathon.

Tell it what you're into, and it shows what's on, what fits your free time, and what your budget can take.

## What it does

1. **My interests** — pick categories and type free-text hobbies.
2. **What's on** — events grouped by day, filtered by city and how far ahead you're looking, with matching events first.
3. **My plan** — set a budget, accept or dismiss suggestions, and see them laid out in a **calendar view** against your free time. Since most events carry no price, accepting one lets you type what it costs, so the budget stays honest.

## Kotlin Multiplatform

One shared codebase. **All UI and logic lives in `commonMain`** — the screens, the planner rules, the event matching, the data layer. Nothing is duplicated per platform.

| Target | Status |
|---|---|
| **Android** | APK built in CI on every commit (`:androidApp:assembleDebug`) |
| **iOS** | Framework compiled in CI and locally (`iosArm64`, `iosSimulatorArm64`) |
| **Desktop** | Preview tool only, for looking at the UI without a simulator |
| **Server** | Ktor backend sharing the same `Event` model as the apps |

Only two pieces are platform-specific: the Android calendar reader (`CalendarContract`) and the iOS entry point.

## Features

- **Dark and light themes** with an in-app switch, following the system by default.
- **English and Arabic**, with right-to-left layout. Interest names translate for display while their identity stays stable, so switching language never breaks matching.
- **Kotlin backend** — a Ktor server serving the curated events, reusing the app's own `@Serializable Event` class. One Kotlin model defines both ends of the wire.
- **Two event sources** merged into one list, with the app falling back to bundled data whenever the network or server is unavailable.

## Why the data works the way it does

We measured the Ticketmaster API with a real key on 2026-09-20 and found **0 of 133 events carried any price**, and the earliest Abu Dhabi event was three weeks out. So the app pairs it with a curated list of community events, entered by hand, which is where real AED prices and near-term events come from. Nothing is scraped — a person reads a listing and types it in.

## Running it

```bash
./gradlew :androidApp:assembleDebug              # Android APK
./gradlew :shared:compileKotlinIosSimulatorArm64 # iOS framework
./gradlew :shared:run                            # desktop preview
APP_LANG=ar ./gradlew :shared:run                # desktop preview in Arabic
./gradlew :server:run                            # curated events API on :8080
./gradlew :shared:testAndroidHostTest            # shared tests
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and press Run.

### Ticketmaster key (optional)

Add `ticketmaster.apiKey=YOUR_KEY` to `local.properties` (gitignored), or set `TICKETMASTER_API_KEY`. Without one the app still runs on the curated events.

## Project layout

```
shared/     Shared Kotlin: UI, planner, calendar contract, data. Most of the code.
androidApp/ Android host
iosApp/     iOS host (Xcode)
server/     Ktor backend
```

Built by [@Areebah367](https://github.com/Areebah367) and [@mu5tafa-m](https://github.com/mu5tafa-m). See `CLAUDE.md` for development guidelines.
