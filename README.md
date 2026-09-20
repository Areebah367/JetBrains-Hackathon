# JetBrains-Hackathon

A Kotlin Multiplatform app for **Android and iOS**, built for the JetBrains Kotlin Multiplatform hackathon.

## The idea

An event planner for **Abu Dhabi and Dubai**. It gathers what's on, matches it to your **interests**, and (next) checks your **budget** and **calendar** so you can say **Yes or No** to each suggestion.

## What works now

1. Tell the app your interests and hobbies.
2. It pulls events from two places: a curated list of community events with real AED prices, and the Ticketmaster API for larger ticketed shows.
3. **What's on** groups them by day. Filter by city and by how far ahead to look, and matching events rise to the top.

## Why two sources

We checked the Ticketmaster API against a real key on 2026-09-20 and found 76 UAE events — but **none of them carried a price**, and the earliest Abu Dhabi event was three weeks out. Community events fill the near-term gap and are where real prices come from. They are typed in by hand from event pages, not scraped.

## Roadmap

- [x] Kotlin Multiplatform project (Android + iOS, shared Compose UI)
- [x] Interests, and a What's on list with city and date filters
- [x] Ticketmaster events for Abu Dhabi and Dubai
- [x] Curated community events with prices
- [ ] Budget, and Yes/No on suggestions
- [ ] Phone calendar availability
- [ ] Save choices on the device

## Ticketmaster key

Register a free key at the [Ticketmaster developer portal](https://developer.ticketmaster.com/), then add this line to `local.properties` in the project root (the file is not committed):

```
ticketmaster.apiKey=YOUR_KEY
```

You can set the `TICKETMASTER_API_KEY` environment variable instead. Never commit the key. Without one, the app still runs on the curated events.

## Project layout

- `shared/` – shared Kotlin code and UI
- `androidApp/` – Android app
- `iosApp/` – iOS app (Xcode)

## Run it

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run.
- Tests: `./gradlew :shared:testAndroidHostTest`

See `CLAUDE.md` for development guidelines.
