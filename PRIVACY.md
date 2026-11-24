# Privacy

AA Solar has no Internet permission, account, analytics SDK, advertising SDK or remote backend.

The app stores the following in its private local Android preferences:

- Saved latitude and longitude, entered manually or obtained with approximate-location permission.
- The sunrise/sunset margin and automation-enabled state.
- A bounded local status/switching log.

It does not continuously track location or upload coordinates. Refreshing location is a user action. The manifest disables app backup, although device-transfer behaviour can vary between Android manufacturers.

Shizuku authorization permits privileged operations. This implementation launches Android Auto settings on an isolated display, reads the accessibility nodes of AA windows on that display and changes the day/night map preference. It does not unlock the physical phone, read contacts, messages or call history, or save display images. Frame buffers of the auxiliary settings display are drained and discarded.

Status messages are also written to Android's system log for development diagnostics. This code does not intentionally include saved coordinates in those messages. Do not post unreviewed device logs or screenshots publicly.

Uninstalling the app removes its local preferences. No data is sent to the project maintainer unless you choose to submit it in an issue or other message.
