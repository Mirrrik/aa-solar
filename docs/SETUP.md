# Setup and troubleshooting

## First setup

1. Install AA Solar on your Android Auto phone, not on the head unit.
2. Follow [Shizuku's official instructions](https://shizuku.rikka.app/guide/setup/) to start Shizuku. Android's wireless debugging pairing is separate from pairing with the car.
3. In AA Solar, tap **Authorize Shizuku**, and allow AA Solar in the Shizuku permission dialog.
4. Tap **Use phone location** and grant approximate location, or manually enter latitude and longitude. Coordinates remain on the phone. They are not automatically refreshed during a journey.
5. Choose the margin. A margin of 0 uses sunrise and sunset; 15 changes to night 15 minutes before sunset and to day 15 minutes after sunrise.
6. Tap **Enable automation**. If Android opens its Alarms & reminders permission page, grant access, return to AA Solar, and tap Enable automation again. Allow notifications so that failures can be reported.
7. Test both modes while parked. Check Android Auto's day/night map preference and the actual car map, not just the phone's system theme.

## Test with a locked phone

Tap a Day or Night test button, choose **In 20 seconds**, then lock the phone. Wait for the test and check the car display. The switching log records whether the Android Auto preference was verified. A verified preference change does not by itself prove that every navigation app or car display applied it.

If a manual test leaves an unwanted mode selected, test the other mode or restore Android Auto Automatic mode. Solar automation continues to use its next planned transition unless disabled.

## After restarting the phone

Unlock the phone normally and restart Shizuku. Without root, this cannot be handled by AA Solar itself. Scheduling is restored after boot and time/time-zone changes; a failed scheduled switch is retried after approximately 15 minutes. Open AA Solar and toggle automation if you want to reapply the current solar mode immediately.

## Common problems

| Symptom | What to check |
| --- | --- |
| Start/authorize Shizuku error | Shizuku must be running and AA Solar must be authorized |
| Exact alarms not allowed | Enable AA Solar under Android's Alarms & reminders special access |
| Day/night setting not found | Use English or Slovak for Android Auto; an AA update may have changed its settings UI |
| UiAutomation/connection error | Stop other UI testing/automation sessions and retry |
| Switch logged but map unchanged | Confirm the AA preference changed and test the navigation app during an active connection; this combination may not be compatible |
| Wrong solar times after travel | Refresh the saved location and check the phone's date and time zone |
| Two launcher icons on a Samsung phone | Check for a Dual App/work-profile copy. Only install and configure AA Solar in the profile used by Android Auto |

Keep the app out of manufacturer-specific sleeping-app restrictions if the device suppresses its alarms or startup. No global theme change, PIN change, root access or modified car firmware is required by this implementation.
