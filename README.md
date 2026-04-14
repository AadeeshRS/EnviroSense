# EnviroSense

EnviroSense is a productivity and environmental tracking Android application designed to help users maintain focus by monitoring their physical surroundings. The app tracks ambient noise levels and lighting conditions, gamifies the focus experience, and automatically manages system settings like "Do Not Disturb" to create the perfect work environment.

## Features (Single-Player MVP)

### 1. Focus Tracking (Home)
- **Sensor Monitoring**: Actively tracks ambient noise (dB) and light (Lux) levels using device hardware to ensure optimal studying/working conditions.
- **Background Persistence**: The tracking engine runs reliably in the background. Navigating across tabs will not kill active sessions.
- **Auto Do Not Disturb (DND)**: Automatically toggles the device's DND mode on during a session and off when finished (requires user permission).

### 2. Analytics & Insights
- **Interactive Charts**: Beautiful, smooth bezier line charts with gradient fills displaying past focus sessions (powered by MPAndroidChart).
- **Study Metrics**: Tracks total session durations, environmental scores, and historical data.

### 3. Gamification & Achievements
- **Unlockable Badges**: Earn achievements based on lifetime focus hours, environmental scores, and consistency.
- **Custom Notifications**: Unique dark-themed Toast notifications pop up when a new badge is unlocked.
- **Progress Tracking**: Detailed bottom sheets show exact progress (e.g., "112 / 100 hrs").

### 4. Settings & Data Management
- **Customizable Thresholds**: Set personalized limits for maximum tolerable noise and minimum required light via interactive bottom sheets.
- **Data Export**: Dump your entire session history to a `.csv` file directly using `MediaStore` API (safely compatible with Android 10+ without dangerous storage permissions).
- **Data Control**: Completely wipe the local database and reset your progress at any time.

## Technical Architecture

- **Language & UI**: Java, Modern XML Layouts, Material Design Components (`BottomNavigationView`, `BottomSheetDialogFragment`).
- **Navigation**: Structured using a dual-navigation approach: `BottomNavigationView` for core tabs and a `NavigationView` (Hamburger Drawer) for Settings & Profile. Optimized Fragment Transactions (`add()`, `hide()`, `show()`) to preserve state, and backstack navigation for sub-screens.
- **Local Storage**: 
  - **Room Database**: Robust SQLite abstraction (`FocusSessionDao`) for querying aggregated lifetime stats and session history.
  - **SharedPreferences**: Lightweight key-value storage for user settings, visual preferences, and badge unlock states.
- **System Services**: `NotificationManager` integration for DND profile access.
- **File I/O**: Native Java `FileWriter` for CSV generation.

## Upcoming: Multiplayer & Community
*This project is actively transitioning to Phase 2 (Firebase Integration).*
Recently added a fully prototyped **Community Tab**, navigation drawer, and Profile editing screen. Next steps include wiring up Firestore to:
- Sync user profiles, sessions, and achievements to the cloud.
- Power the real-time Community Leaderboard.
- Enable joining cooperative Study Groups.

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer recommended)
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- A physical Android device (highly recommended for sensor accuracy)

### Installation

1. **Clone the repository**
   Open your terminal or command prompt and run:
   ```bash
   git clone https://github.com/your-username/EnviroSense.git
   cd EnviroSense