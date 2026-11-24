# AA Solar

**Sunrise and sunset scheduling for Android Auto map day/night mode — independent of your phone's dark theme.**

[Download the APK](https://github.com/Mirrrik/aa-solar/releases) · [Setup guide](docs/SETUP.md) · [Report an issue](https://github.com/Mirrrik/aa-solar/issues)

AA Solar runs on your **phone**. It selects Day or Night in Android Auto's own map settings using [Shizuku](https://shizuku.rikka.app/). No companion app or modified firmware is needed on the car display.

> **Experimental preview.** This uses Android Auto's settings UI and privileged Android APIs, not an official Android Auto day/night API. Compatibility is limited; an Android or Android Auto update may break it. Please test while parked before relying on the schedule.

## Features

- Calculates sunrise and sunset locally from a saved location.
- Defaults to Night **15 minutes before sunset** and Day **15 minutes after sunrise**. Configurable margin: 0–60 minutes.
- Changes Android Auto map mode without changing the phone's system theme.
- Uses exact alarms rather than keeping a foreground service running all day.
- Includes immediate and delayed Day/Night tests, a switching log, and a button to restore Android Auto's Automatic setting.
- Operates on an isolated auxiliary display and verifies the selected mode. It does not type a PIN or unlock the physical phone screen.
- English app interface; recognizes **English and Slovak** Android Auto settings labels.
- No ads, analytics, account, Internet permission, or background location tracking.

## Requirements

| Requirement | Details |
| --- | --- |
| Phone | Android 13 or later; the minimum version is not a compatibility guarantee |
| Android Auto | Installed, configured, and using English or Slovak settings labels |
| Shizuku | Installed, running, and authorized for AA Solar |
| Permissions | Exact alarms, notifications, and optionally approximate location |
| Location | Saved coordinates; refresh them manually after travelling a significant distance |

**Without root, Shizuku must be started again after restarting the phone.** AA Solar cannot bypass this requirement. See the [official Shizuku setup guide](https://shizuku.rikka.app/guide/setup/).

## Download and setup

1. Open [Releases](https://github.com/Mirrrik/aa-solar/releases) and download the `AA-Solar-*.apk` asset. The source ZIP is for developers, not an installable app.
2. Install the APK on the phone running Android Auto. Android may request permission to install APKs from your browser or file manager.
3. Start Shizuku and authorize **AA Solar**.
4. Open AA Solar, select **Use phone location** or enter coordinates, and choose a margin.
5. Enable exact alarms and notifications, then select **Enable automation**.
6. While parked, try **Test day mode** and **Test night mode**, including the 20-second delayed test with the phone locked. Check the actual map on your car display.

More detail: [Setup and troubleshooting](docs/SETUP.md).

## Compatibility and limits

The 0.1.0 prototype was physically tested on a Samsung SM-S942B reporting Android 17, Android Auto 18.0.664264-release, and Shizuku 13.6.0. Scheduled Day/Night changes were verified in Android Auto settings, including a locked-phone test, while the phone's dark theme stayed enabled. Version 0.1.1 adds the English interface and standalone public build packaging; it has build, lint, and solar calculation checks, but has not been separately road-tested.

- **NAVITEL DS1020 DVR:** the device owner reported successful day/night switching during a live connection on 2026-11-24. This is a user-reported result, not a compatibility guarantee for other head units.
- A complete overnight sunrise transition has not yet been verified as part of the public release.
- AA Solar does not continuously update location, detect tunnels, or use the ambient light sensor.
- Polar day/night locations are not supported. Sunrise/sunset times are approximate and do not account for terrain or the local horizon.
- Only one UiAutomation session can operate at a time; other testing/automation software may conflict.
- Disabling automation leaves the last selected AA mode. **Restore Android Auto automatic mode** disables AA Solar scheduling and selects Automatic in AA.
- A manual test changes the current mode; it does not change the next scheduled solar transition.

## How it works

A short-lived Shizuku user service runs as shell. It creates an isolated virtual display, launches Android Auto settings there, locates the day/night preference by text, selects the requested option, and checks the resulting selected state. It then disconnects UiAutomation and releases the display.

The automation is restricted to Android Auto windows on that auxiliary display. Unknown labels or missing options produce an error instead of coordinate-based blind clicks. The service does not capture or store screenshots of the physical phone display. Shizuku access is powerful: review the [source](app/src/main/java/sk/octavia/aasolar) and [privacy information](PRIVACY.md) before authorizing any privileged app.

## Build from source

Requirements: JDK 17, Android SDK Platform 36, and an Android SDK configured through `ANDROID_HOME` or an untracked `local.properties` file.

```sh
# Linux/macOS
./gradlew :app:assembleDebug :app:lintDebug
```

```powershell
# Windows PowerShell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

For an unsigned release build, run `:app:assembleRelease`. Sign it with **your own private Android signing key**. Signing keys, passwords, personal configuration and device logs are not included in this repository. A self-signed build cannot update the published APK unless it uses the same signing key; uninstalling to switch keys removes local settings.

Run the standalone solar tests with JDK 17:

```sh
mkdir -p build/solar-tests
javac -d build/solar-tests app/src/main/java/sk/octavia/aasolar/SunTimes.java tests/SunTimesTest.java
java -cp build/solar-tests SunTimesTest
```

The GitHub Actions workflow builds and lints an unsigned release and runs these tests. Its unsigned build artifact is for developers; install the signed APK from Releases for normal use.

## Contributing

Bug reports and small fixes are welcome. Include your phone model, Android version, Android Auto version, Shizuku version, AA language, and the error shown in AA Solar. Remove coordinates and personal information from screenshots and logs before posting.

## License and acknowledgements

AA Solar source is available under the [MIT License](LICENSE). See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for Shizuku and Gradle notices. The isolated-display approach was informed by [scrcpy's virtual-display implementation](https://github.com/Genymobile/scrcpy/blob/master/server/src/main/java/com/genymobile/scrcpy/video/NewDisplayCapture.java).

This project is independent and is not affiliated with or endorsed by Google, Samsung, NAVITEL, or the Shizuku project. Android Auto and other product names belong to their respective owners.

