# PathPulse

**PathPulse** is an Android GPS tracking application that records your outdoor activities, maps your routes, and exports them in multiple formats for analysis and sharing.

![Build](https://github.com/unsmah/PathPulse/actions/workflows/build.yml/badge.svg)

---

## 📱 App Description

PathPulse is a feature-rich GPS tracking app for Android that allows you to:

- Record GPS tracks with real-time location updates
- View your tracks on interactive maps (MapLibre)
- Monitor live tracking with a dedicated map screen
- Manage waypoints during your activities
- Set up geofence alerts for location-based notifications
- Export tracks in multiple formats (GPX, GeoJSON, CSV, KML)
- Review track history with detailed statistics
- Configure app settings for customization

---

## ✨ Features

### Core Tracking
- **GPS Tracking**: Continuous location recording with high precision
- **Real-time Stats**: Live display of distance, speed, elevation, and duration
- **Auto-pause**: Intelligent moving time calculation (detects when you're stationary)

### Map Visualization
- **MapLibre Integration**: Open-source map rendering with OSM tile layers
- **Live Map Screen**: Real-time tracking display with current position
- **Track History**: Browse and visualize past tracks on the map

### Data Export
- **GPX Format**: Standard GPS Exchange Format for compatibility with other apps
- **GeoJSON Format**: GeoJSON for web mapping and GIS applications
- **CSV Format**: Spreadsheet-friendly tabular data
- **KML Format**: Keyhole Markup Language for Google Earth

### Waypoints & Geofences
- **Waypoints**: Add custom markers during tracking
- **Geofences**: Create circular regions with enter/exit event tracking

### Data Management
- **Track Storage**: Local SQLite database with Room persistence
- **Track Statistics**: Distance, duration, average/max speed, elevation gain/loss
- **Data Export**: Export individual tracks or manage all data

---

## 🚀 Build Instructions

### Prerequisites

- **Android Studio** (recommended) or command-line build tools
- **JDK 17** (Temurin distribution)
- **Android SDK** with API level 34 and Build Tools 34.0.0
- **Gradle 8.7**

### Build Locally

```bash
# Clone the repository
git clone https://github.com/unsmah/PathPulse.git
cd PathPulse

# Make gradlew executable (if needed)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug --no-daemon

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Build via CI (GitHub Actions)

Push to the `main` branch or create a tag `v*` to trigger the CI workflow:

```bash
# Push to main (triggers build)
git push origin main

# Or create a version tag (triggers build + release)
git tag v1.0.0
git push origin v1.0.0
```

The workflow will:
1. Checkout the code
2. Setup JDK 17 (Temurin)
3. Setup Android SDK (platform-tools, platforms;android-34, build-tools;34.0.0)
4. Setup Gradle 8.7
5. Build the debug APK
6. Upload the APK as an artifact (`app-debug`)

For tagged releases, it additionally creates a GitHub Release with the APK attached.

---

## 📥 Installing the APK to Device

### Option 1: Manual Installation

1. Download the APK from:
   - GitHub Actions artifacts (after a successful build)
   - GitHub Releases (for tagged versions)

2. Enable "Install from Unknown Sources" on your Android device:
   - Go to **Settings > Security > Unknown Sources** (or **Settings > Apps > Special App Access > Install Unknown Apps**)

3. Transfer the APK to your device (via USB, cloud storage, or direct download)

4. Tap the APK file to install

### Option 2: Using ADB

```bash
# Connect device via USB with debugging enabled
adb devices

# Install the debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or install from a downloaded APK
adb install ~/Downloads/PathPulse-debug.apk
```

### Option 3: Using the Download Script

A helper script is provided to download the latest APK from GitHub Actions:

```bash
# Make script executable
chmod +x scripts/gh-download-apk.sh

# Run the script (requires gh CLI installed and authenticated)
./scripts/gh-download-apk.sh
```

The script will:
1. Find the latest successful workflow run
2. Download the `app-debug` artifact
3. Save to `~/storage/downloads/PathPulse-debug.apk`

**Requirements**:
- `gh` CLI tool installed (`brew install gh` or `apt install gh`)
- Authenticated with GitHub (`gh auth login`)

---

## 🏗️ Architecture Overview

PathPulse follows a **clean architecture** approach with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐│
│  │  MainActivity│ │ ViewModels  │ │  Composables/Screens    ││
│  └─────────────┘ └─────────────┘ └─────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  TrackRepository (Use cases, business logic)            ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                                │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐ │
│  │   Room DB    │  │ DataStore    │  │ Exporter Modules  │ │
│  │  (DAO/Entity)│  │ (Settings)   │  │ GPX/GeoJSON/CSV  │ │
│  └──────────────┘  └──────────────┘  └───────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **UI** | `ui/` | Jetpack Compose screens, MainActivity, Theme |
| **ViewModels** | `ui/TrackScreen.kt`, etc. | UI state management, user interactions |
| **Repository** | `data/repository/TrackRepository.kt` | Business logic, coordinate DAO operations |
| **Database** | `data/PathPulseDatabase.kt` | Room database, entities, DAOs |
| **Export** | `export/` | GPX, GeoJSON, CSV, KML exporters |
| **Location** | `location/` | GPS tracking service, permissions |

### Data Entities

- **Track**: Core track record (id, name, start/end time, distance, stats)
- **TrackPoint**: Individual GPS sample (lat, lon, altitude, speed, timestamp)
- **Waypoint**: User-added markers during tracking
- **Geofence**: Circular region definitions
- **GeofenceEvent**: Enter/exit events for geofences

---

## 📝 Project Structure

```
PathPulse/
├── .github/
│   └── workflows/
│       └── build.yml          # CI/CD workflow
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/unsmah/pathpulse/
│   │   │   │   ├── data/           # Database, DAOs, Repository
│   │   │   │   ├── export/         # GPX, GeoJSON, CSV, KML exporters
│   │   │   │   ├── location/       # GPS service, permissions
│   │   │   │   ├── map/            # Map components
│   │   │   │   └── ui/             # Compose screens, ViewModels
│   │   │   └── res/                # Resources (layouts, strings, etc.)
│   │   └── test/                   # Unit tests
│   └── build.gradle.kts
├── scripts/
│   └── gh-download-apk.sh          # APK download helper
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔧 Configuration

### Gradle Build

The project uses Gradle 8.7 with Kotlin DSL. Key configurations:

```kotlin
// build.gradle.kts (app module)
android {
    compileSdk = 34
    defaultConfig {
        minSdk = 24  // Android 7.0+
        targetSdk = 34
    }
}
```

### Dependencies

- **Jetpack Compose**: Modern declarative UI
- **Room**: Local SQLite database
- **Kotlin Coroutines**: Async operations
- **MapLibre**: Open-source map rendering
- **Play Services Location**: GPS location provider

---

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Check the existing documentation in the codebase
