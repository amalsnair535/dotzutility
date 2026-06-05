# Dotz Utility

> Calculator, Calendar, Clock & Notes — All in One.

A lightweight, minimalist Android utility app with a strict black-and-white design philosophy.

## Modules

| Module       | Features |
|--------------|----------|
| Calculator   | Basic arithmetic, %, decimal, calculation history, copy result |
| Calendar     | Monthly view, add/delete events, local Room storage |
| Clock        | Digital clock, stopwatch with laps, countdown timer |
| Notes        | Create/edit/delete, pin, search, export as TXT |
| Settings     | Light / Dark / System theme, export notes |

## Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Room** for Notes and Calendar persistence
- **DataStore** for Settings
- **MVVM** architecture
- Min SDK 24 — targets Android 14

## Building

1. Open `DotzUtility/` in Android Studio Hedgehog or newer.
2. Set `sdk.dir` in `local.properties` if needed (Android Studio does this automatically).
3. Run → **app** on a device or emulator.

## Design Principles

- No gradients, no colour accents
- System default typeface — lean APK
- Offline-first, zero network permissions
- Minify + R8 enabled in release build
