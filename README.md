# JetBrains-Hackathon

A Kotlin Multiplatform app for **Android and iOS**, built for the JetBrains Kotlin Multiplatform hackathon.

## The idea

An event planner for Abu Dhabi. It pulls events from **Ticketmaster**, shows you the coming week matched to your **interests**, and (next) checks your **budget** and **calendar** so you can say **Yes or No** to each suggestion.

## What works now

1. Tell the app your interests and hobbies.
2. It fetches upcoming Abu Dhabi events from the Ticketmaster API.
3. **This week** lists the next 7 days by day, with time, venue, and price. Events that match your interests come first, and a switch hides the rest.

Without a Ticketmaster key, or if the request fails, the app shows clearly labelled sample events so it can always be demoed.

## Roadmap

- [x] Kotlin Multiplatform project (Android + iOS, shared Compose UI)
- [x] Ticketmaster events, interests, and the next-7-days list
- [ ] Confirm Ticketmaster returns Abu Dhabi events (needs a real key)
- [ ] Budget, and Yes/No on "maybe" suggestions
- [ ] Phone calendar availability
- [ ] Save choices on the device

## Ticketmaster key

Register a free key at the [Ticketmaster developer portal](https://developer.ticketmaster.com/), then add this line to `local.properties` in the project root (the file is not committed):

```
ticketmaster.apiKey=YOUR_KEY
```

You can set the `TICKETMASTER_API_KEY` environment variable instead. Never commit the key.

## Project layout

- `shared/` – shared Kotlin code and UI
- `androidApp/` – Android app
- `iosApp/` – iOS app (Xcode)

## Run it

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run.
- Tests: `./gradlew :shared:testAndroidHostTest`

See `CLAUDE.md` for development guidelines.
