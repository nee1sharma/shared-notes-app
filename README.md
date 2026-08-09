# SharedNoteBook (Android App)

SharedNoteBook is a secure, peer-to-peer note-taking system designed to prioritize user privacy and familiar note-editing experiences. This repository contains the Android application, which works in conjunction with a designated laptop control plane.

## Key Features

- **Private & Shared Notes:** Clearly distinguish between personal notes kept on your device and notes shared within your household.
- **Peer-to-Peer Synchronization:** Synchronize notes directly between Android devices on the same local network using mDNS discovery.
- **Security & Privacy:** Local and network data are encrypted.
- **Offline-First Design:** Create and edit notes even when the control plane is unavailable; changes sync once connectivity is restored.
- **Conflict Resolution:** Gracefully handles concurrent offline edits with a dedicated reconciliation interface.

## App Architecture & Tech Stack

- **UI:** Built in **Java** with Material Components, Android XML layouts, and RecyclerView.
- **Dependency Injection:** Uses **Hilt** for a robust and testable architecture.
- **Local Storage:** Uses **Room** with SQLCipher for encrypted local persistence.
- **Discovery (Planned):** Automated local network service discovery using NSD/mDNS is currently in development.
- **Background Work (Planned):** Periodic reconciliation via WorkManager is planned for a future update.

## Getting Started

### 1. Prerequisite: Control Plane
The Android app requires a running SharedNoteBook Control Plane on your local network to handle device registration and global synchronization.
- Ensure the control plane is active and advertising via mDNS.

### 2. Install and Register the App
- Deploy the Android application to your device.
- Upon first launch, the app will automatically search for your household on the local network.
- Follow the on-screen instructions to register your device.

### 3. Start Notetaking
- **Private Notes:** Stored only on your device, protected by local encryption.
- **Shared Notes:** Automatically sync with other registered devices in your household.

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
- Database content and network communication are encrypted.
- No sensitive keys or email addresses are stored in the clear.

## Documentation
For detailed app design specifications and architecture diagrams, please refer to the [docs/design.md](docs/design.md) file.
