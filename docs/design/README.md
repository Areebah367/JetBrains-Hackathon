# Design reference

`prototype/Main.dc.html` is an interactive HTML prototype of $ocial, used to work out the
interface before committing it to Compose. Open it in a browser.

It runs the **same logic the Kotlin app already implements** — the same 18 curated events, the same
interest matching, the same maybe/accept rules, and the same "no price listed, so ask the user"
flow. It is a visual study, not a second implementation.

## What the shipped app takes from it

- Violet and orange palette, in light and dark
- Three tabs: interests, what's on, my plan
- Faded suggestions that turn solid once accepted
- A week grid with free time and busy time drawn behind events
- Asking for a price when an event has none, with skipping allowed

## What it explores that the app has not adopted yet

- Icon tiles for interests instead of text chips
- A day strip across the top of What's on, with per-day event counts
- Ticket-stub event cards, with the time on a torn-off left edge
- A budget card with a spend meter
- A bottom sheet for the price prompt rather than a dialog
- Bricolage Grotesque for headings

These are layout changes over the existing screens; the planner, matching and data layers would not
need to change to adopt them.
