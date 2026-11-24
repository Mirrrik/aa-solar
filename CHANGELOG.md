# Changelog

## 0.1.2 — Portable build and CI fix

- Use the Android SDK already provided by the GitHub-hosted Ubuntu runner, avoiding the retired `tools` package requested by the old SDK setup action.
- Update GitHub-maintained build actions and avoid duplicate CI runs for release tags and documentation-only changes.
- Normalize the Gradle shell wrapper to LF and document invocation through `bash` for source archives without executable file metadata.
- Keep map-switching behaviour unchanged.

## 0.1.1 — Experimental public preview

- English app interface, status messages and error descriptions.
- English public setup, privacy and compatibility documentation.
- Standalone Gradle wrapper and build workflow; no dependency on private workspace paths or signing keys.
- Sunrise/sunset scheduling with a configurable 0–60 minute margin.
- Immediate and delayed Day/Night tests using Shizuku and isolated AA settings.

The core switching mechanism comes from the locally tested 0.1.0 prototype. The public build has not been separately road-tested. English and Slovak Android Auto settings labels are supported.

Compatibility report (2026-11-24): the device owner confirmed working day/night switching on a NAVITEL DS1020 DVR during an active connection.

