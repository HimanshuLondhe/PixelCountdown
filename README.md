# Pixel Countdown ⏳

A modern, minimalist countdown timer application designed and optimized specifically for **Google Pixel** devices, built with **Jetpack Compose**, **Material 3 (Material You)**, and native **AppWidgetProvider**.

---

## 🌟 Key Features

1. **Precision Countdown Breakdown**:
   - Displays exact remaining time in **Years, Months, Days, Hours, Minutes, and Seconds**.
   - Handles leap years, month length differences, and daylight savings transitions seamlessly.
2. **Multiple Countdowns Management**:
   - Add new timers with event title, target date (DatePicker), and target time (TimePicker).
   - Edit existing timers or delete timers with confirmation dialogs.
   - Pinned widget selector: choose which countdown is displayed on your home screen widget.
3. **Completion Notifications**:
   - Exact Android `AlarmManager` integration (`setExactAndAllowWhileIdle`).
   - High-priority notification with sound and vibration fires the moment any timer finishes.
   - Automatically reschedules future alarms across device reboots (`RECEIVE_BOOT_COMPLETED`).
4. **Optimized for Google Pixel**:
   - **Dynamic Color (Material You)**: Automatically harmonizes app and widget colors with your system wallpaper palette (`dynamicLightColorScheme` / `dynamicDarkColorScheme`).
   - Clean, lightweight typography with edge-to-edge system navigation.
5. **Pixel Home Screen Widget**:
   - Companion home screen widget (3x2 or resizable) with live countdown chips.
   - Tap widget to directly open and manage countdowns in the app.

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
│   │   │   │   └── CountdownRepository.kt
│   │   │   ├── receiver/
│   │   │   │   ├── CountdownAlarmReceiver.kt
│   │   │   │   └── NotificationHelper.kt
│   │   │   ├── widget/
│   │   │   │   └── PixelCountdownWidgetProvider.kt
│   │   │   └── ui/
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

## 🚀 How to Build the APK

### Option A: Open in Android Studio
1. Open **Android Studio**.
2. Select **Open** and choose `/Users/himanshu/.gemini/antigravity/scratch/pixel-countdown`.
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)** or click **Run** to deploy directly to your Pixel device or emulator.

### Option B: Build with Gradle Command Line
```bash
cd /Users/himanshu/.gemini/antigravity/scratch/pixel-countdown
./gradlew assembleDebug
```
The compiled APK will be created at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Option C: Cloud CI (GitHub Actions)
Push this repository to GitHub, and the included `.github/workflows/build-apk.yml` workflow will automatically build and provide the downloadable `app-debug.apk` in the GitHub Actions artifacts!
