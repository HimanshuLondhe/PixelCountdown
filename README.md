# PixelTimer - Countdown ⏳

A modern, minimalist countdown timer application designed and optimized specifically for **Google Pixel** devices. Built with **Jetpack Compose**, **Material 3 (Material You)**, and native **AppWidgetProvider**.

[![Build PixelTimer - Countdown APK](https://github.com/HimanshuLondhe/PixelCountdown/actions/workflows/build-apk.yml/badge.svg?branch=master)](https://github.com/HimanshuLondhe/PixelCountdown/actions/workflows/build-apk.yml)

---

## 🌟 Key Features

1. **Precision Countdown Breakdown**:
   - Displays exact remaining time in **Years, Months, Days, Hours, Minutes, and Seconds**.
   - Handles leap years, month length differences, and daylight savings transitions seamlessly.
2. **Personalization (Settings)**:
   - **Dynamic Theming**: Choose between Light, Dark, or System Default themes.
   - **Custom Typography**: Select from Default, Serif, Monospace, or **Public Sans** font styles.
   - **Hamburger Navigation**: A modern Navigation Drawer for quick access to your timers, settings, and about info.
3. **Advanced User Experience**:
   - **Drag-and-Drop Reordering**: Long-press any timer to manually arrange your list exactly how you want it.
   - **Adaptive Grid Layout**: Automatically switches to a 2-column grid on tablets or in landscape orientation for optimal space usage.
4. **Pixel Home Screen Widget**:
   - **Live Countdown**: Features a background foreground service that enables **live seconds** ticking directly on your home screen.
   - **Battery Efficient**: Updates automatically pause when the screen is off.
   - **Resizability**: Widget unit components expand vertically to fill space when the widget is resized.
   - **Theme-Aware**: The widget dynamically matches your in-app theme selection or system light/dark mode.
5. **Completion Notifications**:
   - Exact Android `AlarmManager` integration (`setExactAndAllowWhileIdle`).
   - High-priority notifications with sound and vibration fire the moment any timer finishes.
6. **Modern Branding**:
   - **Digital Hourglass Icon**: A custom-designed adaptive icon that supports Material 3 themed icons (monochrome).
   - Clean, lightweight typography with edge-to-edge system navigation.

---

## 📁 Project Structure

```
pixel-countdown/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/pixelcountdown/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── CountdownModel.kt
│   │   │   │   ├── CountdownRepository.kt
│   │   │   │   └── SettingsRepository.kt
│   │   │   ├── receiver/
│   │   │   │   ├── CountdownAlarmReceiver.kt
│   │   │   │   └── NotificationHelper.kt
│   │   │   ├── widget/
│   │   │   │   ├── PixelCountdownWidgetProvider.kt
│   │   │   │   └── WidgetUpdateService.kt
│   │   │   └── ui/
│   │   │       ├── settings/ SettingsScreen.kt
│   │   │       ├── about/ AboutScreen.kt
│   │   │       ├── components/
│   │   │       │   ├── CountdownCard.kt
│   │   │       │   └── CountdownEditDialog.kt
│   │   │       └── theme/
│   │   │           ├── Color.kt
│   │   │           ├── Theme.kt
│   │   │           └── Type.kt
│   │   └── res/
│   └── src/test/
│       └── java/com/pixelcountdown/CountdownCalculatorTest.kt
├── build.gradle.kts
├── settings.gradle.kts
└── .github/workflows/build-apk.yml
```

---

## 🛠️ Technical Details

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **External Dependencies**: `sh.calvin.reorderable` for robust list reordering.
- **Data Persistence**: SharedPreferences with KotlinX Serialization.
- **Architecture**: Modern Android architecture with Repositories and StateFlow.
- **Performance**: Optimized production builds with R8 minification and resource shrinking enabled.
- **Testing**: Comprehensive JUnit 4 test suite for countdown logic and serialization.

---

## 🚀 How to Build the APK

### Option A: Open in Android Studio
1. Open **Android Studio**.
2. Select **Open** and choose this project directory.
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)** or click **Run** to deploy directly to your device.

### Option B: Build with Gradle Command Line
```bash
./gradlew assembleDebug
```
The compiled APK will be created at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 👤 Developer
**Himanshu Londhe**
[GitHub Profile](https://github.com/HimanshuLondhe)

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
