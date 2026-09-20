# JetBrains-Hackathon

A Kotlin Multiplatform app for **Android and iOS**, built for the JetBrains Kotlin Multiplatform hackathon.

## The idea

An event planner for Abu Dhabi that checks your **budget** and your **calendar**, finds **Ticketmaster** events that fit, and lets you say **Yes or No** to each suggestion.

## How it works

1. Set a budget in AED.
2. The app reads your phone calendar and finds your free time.
3. It fetches upcoming events from the Ticketmaster API.
4. Events that fit a free slot and your remaining budget show up as "maybe" suggestions.
5. Tap **Yes** to add an event to your plan (the price comes off your budget) or **No** to dismiss it for good.

The matching logic is plain Kotlin in the shared module, so it behaves the same on Android and iOS.

## Roadmap

- [x] Kotlin Multiplatform project (Android + iOS, shared Compose UI)
- [ ] Matching logic and tests
- [ ] Budget, maybe list with Yes/No, and plan screens
- [ ] Ticketmaster events
- [ ] Phone calendar availability
- [ ] Save decisions on the device

## Project layout

- `shared/` – shared Kotlin code and UI
- `androidApp/` – Android app
- `iosApp/` – iOS app (Xcode)

## Run it

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run.

See `CLAUDE.md` for development guidelines.
