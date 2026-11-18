---
name: android-manual-test-handoff
description: Build, install, and launch the current Android debug app after implementing a feature or changing application code, leaving the relevant screen ready for developer testing and feedback. Also use when explicitly asked to run the app for review. Documentation-only changes do not trigger an app launch.
---

# Android manual test handoff

Make the implemented behavior available for the developer to try. A successful build alone is not evidence that the app is installed or running.

## Prepare the current build and device

- Read the current Gradle configuration and manifest for the application ID, build variant, launcher activity, and SDK requirements; do not rely on a previous task's APK or device name.
- Use the developer-designated device when provided. Otherwise inspect `adb devices -l` and available emulator profiles. Prefer an already-running suitable emulator. When several targets are plausible, resolve the intended target before installing; use `adb -s SERIAL` for device commands.
- If necessary, start a suitable existing emulator with the available SDK tooling and wait for it to finish booting. If no suitable device/profile, SDK, or permissions are available, report the exact blocker and the steps needed to launch locally. Do not install new system images or change device settings just to complete the handoff.
- Run checks appropriate to the implementation and build the current debug variant with the repository's Gradle wrapper, normally `./gradlew :app:assembleDebug`. If compilation fails, fix relevant implementation errors before proceeding. Do not substitute an older APK.

## Install, launch, and verify

- Install the APK produced by that build on the chosen target using an update install, normally `adb -s SERIAL install -r app/build/outputs/apk/debug/app-debug.apk`. Preserve the existing Room database, image cache, and user settings. Do not uninstall, clear app data, wipe the emulator, or downgrade as a workaround without explicit authorization.
- Launch the actual launcher component, for example with `adb -s SERIAL shell am start -W -n APPLICATION_ID/LAUNCHER_ACTIVITY`, substituting values inspected from the project. Navigate to the implemented feature with available tools and leave that screen open. If navigation cannot be completed, state which screen is open and give the remaining steps.
- Confirm that the app remains open after initial loading. Inspect the resumed activity/process and recent package-specific crash output; a successful activity-start response alone is insufficient. Observe the screen directly or capture a screenshot when useful to verify the visible state. Do not clear device logs to simplify inspection.
- If the app crashes, investigate and correct failures caused by this implementation, then rebuild, reinstall, and repeat the startup check. For an external blocker or failure that needs developer input, report the observed failure and its evidence instead of claiming the app is ready. Avoid repeating the same failed launch without new information.
- Keep screenshots, logs, device IDs, and machine-specific paths out of commits unless deliberately requested and appropriate. Do not alter network settings or erase cached data to demonstrate error states without authorization.

## Hand off for feedback

In the final response, state:

- The device/emulator name and the build tested (variant, version, and commit plus local changes if applicable).
- The screen left open, startup-check result, and any access or verification limitation. Distinguish startup verification from full feature testing.
- A short manual test path with expected results for the implemented behavior. Include relevant loading, empty, error, pagination, or offline cases only when they belong to the feature and can be exercised without unauthorized device changes.
- A compact feedback format: **steps → expected result → actual result**, with a screenshot or error message when helpful. Invite the developer's feedback without blocking delivery or automatically starting unrelated changes.

Only claim the app is running and ready when installation, launch, and the startup check actually succeeded. If blocked, provide the concrete build/launch commands and manual navigation steps the developer can use, clearly marked as not verified on a device.
