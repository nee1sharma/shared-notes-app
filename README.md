# NetBook (Android App)

NetBook is a local-first note-taking system for people on the same local network. This repository contains the Android application, which synchronizes shared notes through the designated laptop control plane.

## Key Features

- **Private & Shared Notes:** Clearly distinguish between personal notes kept on your device and notes shared within your household.
- **Household Synchronization:** Shared notes upload to and download from the laptop PostgreSQL replica. Private notes are never sent.
- **Device Registry:** NSD/mDNS finds the laptop control plane; registration yields a device token used for heartbeats, the device list, and sync.
- **Offline-first editing:** Notes remain editable while the laptop is unavailable and shared changes retry through WorkManager.
- **Conflict marker:** Divergent shared revisions are retained by the control plane and marked in the browser. A mobile conflict-resolution interface is still pending.

## App Architecture & Tech Stack

- **UI:** Built in **Java** with Material Components, Android XML layouts, and RecyclerView.
- **Dependency Injection:** Uses **Hilt** for a robust and testable architecture.
- **Local Storage:** Uses **Room** for device-local notes.
- **Discovery:** Uses Android NSD/mDNS to resolve the `_netbook._tcp` control-plane service.
- **Background Work:** Uses WorkManager for immediate shared-note sync and periodic presence heartbeats.

## Getting Started

### 1. Prerequisite: Control Plane
The Android app requires a running NetBook Control Plane on your local network to handle device registration and global synchronization.
- Ensure the control plane is active and advertising via mDNS.

### 2. Install and Register the App
- Deploy the Android application to your device.
- Open **Settings → Find or Join Household**, choose the discovered control plane, and follow the registration prompt.

### 3. Start Notetaking
- **Private Notes:** Stored only on your device, protected by local encryption.
- **Shared Notes:** Sync through the registered control plane after saving, at app launch, and when background work runs.

## Build & Deployment

### Local Development
Ensure you have the Android SDK installed and a device/emulator connected.

- **Build Debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```
- **Install and Run on Device:**
  ```bash
  ./gradlew installDebug
  ```
- **Run Unit Tests:**
  ```bash
  ./gradlew test
  ```

### GitHub Actions & Releases
This repository is configured with GitHub Actions to automate the build and release process:
- **Continuous Integration:** Every push or pull request to `main` triggers a build of the **release** variant to verify code integrity.
- **Automated Releases:** Pushing a tag starting with `v` (e.g., `v1.0.0`) automatically creates a GitHub Release and attaches the generated **Release APK**.
- **Artifacts:** The release APK is also available as a build artifact in the GitHub Actions tab for every successful build.

## Security & Sensitive Data

This project is configured to avoid pushing sensitive data to version control:
- `local.properties` and keystore files (`*.jks`, `*.keystore`) are ignored.
- Shared note payloads are encrypted at rest by the laptop control plane. The current Android control-plane transport is authenticated HTTP on a trusted home LAN; do not use it on an untrusted network until TLS is configured.
- No sensitive keys or email addresses are stored in the clear.

## Documentation
The detailed design documents describe the target architecture. The implemented connection scope and current limitations are recorded in [docs/requirements.md](docs/requirements.md).
