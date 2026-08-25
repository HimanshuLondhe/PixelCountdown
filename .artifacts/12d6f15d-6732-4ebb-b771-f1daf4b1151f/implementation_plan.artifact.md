# Implementation Plan - Navigation Drawer, Settings, and About Screens

This plan details the addition of a hamburger menu (Navigation Drawer) to the app, along with a Settings screen for theme and font preferences, and an About screen with app details and an Easter egg.

## User Review Required

> [!IMPORTANT]
> The "Font selection" will allow choosing between standard system font families: Default, Serif, and Monospace.
> Notification settings will link to the system app settings for channel management, which is the idiomatic way for modern Android apps.

## Proposed Changes

### [Data Layer]

#### [NEW] [SettingsRepository](file:///Users/himanshu/.gemini/antigravity/scratch/pixel-countdown/app/src/main/java/com/pixelcountdown/data/SettingsRepository.kt)
Create a repository to persist user preferences using `SharedPreferences`:
- `theme`: "system", "light", "dark"
- `fontFamily`: "default", "serif", "monospace"

### [UI Components]

#### [NEW] [SettingsScreen](file:///Users/himanshu/.gemini/antigravity/scratch/pixel-countdown/app/src/main/java/com/pixelcountdown/ui/settings/SettingsScreen.kt)
Implementation of the Settings UI:
- Radio buttons for Theme selection.
- Dropdown or list for Font selection.
- Button to open system notification settings.

#### [NEW] [AboutScreen](file:///Users/himanshu/.gemini/antigravity/scratch/pixel-countdown/app/src/main/java/com/pixelcountdown/ui/about/AboutScreen.kt)
Implementation of the About UI:
- Version display (fetched from `PackageInfo`).
- GitHub profile link button.
- Hidden Easter Egg button (5 taps to open Rickroll).

### [Theme Update]

#### [MODIFY] [Theme.kt](file:///Users/himanshu/.gemini/antigravity/scratch/pixel-countdown/app/src/main/java/com/pixelcountdown/ui/theme/Theme.kt)
Update `PixelCountdownTheme` to accept the user's preferred theme and font family.

#### [MODIFY] [Type.kt](file:///Users/himanshu/.gemini/antigravity/scratch/pixel-countdown/app/src/main/java/com/pixelcountdown/ui/theme/Type.kt)
Refactor `Typography` to support dynamic `FontFamily`.

### [Main Navigation]

#### [MODIFY] [MainActivity.kt](file:///Users/himanshu/.gemini/antigravity/scratch/pixel-countdown/app/src/main/java/com/pixelcountdown/MainActivity.kt)
- Wrap `MainScreen` content in a `ModalNavigationDrawer`.
- Add state to track the current visible screen.
- Implement the hamburger menu icon in the `TopAppBar`.

## Verification Plan

### Automated Tests
- No new automated tests planned; focus on manual verification of UI and persistence.

### Manual Verification
- Verify that changing the theme in Settings immediately updates the app UI.
- Verify that the font selection changes the text style across the app.
- Verify that the About screen shows the correct version "1.0".
- Verify that the GitHub button opens the browser.
- Verify the Rickroll Easter egg triggers after 5 rapid taps on the hidden area.
