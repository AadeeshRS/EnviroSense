# EnviroSense

EnviroSense is a productivity and environmental tracking Android application designed to help users maintain focus by monitoring their physical surroundings. The app tracks ambient noise levels and lighting conditions, gamifies the focus experience, and automatically manages system settings like "Do Not Disturb" to create the perfect work environment.

## Features (Multiplayer and Cloud Sync)

### 1. Focus Tracking (Home)
- **Sensor Monitoring**: Actively tracks ambient noise (dB) and light (Lux) levels using device hardware to ensure optimal studying/working conditions.
- **Background Persistence**: The tracking engine runs reliably in the background. Navigating across tabs will not kill active sessions.
- **Auto Do Not Disturb (DND)**: Automatically toggles the device's DND mode on during a session and off when finished (requires user permission).
- **Independent Thresholds**: Decoupled card-level click listeners in the Home screen to ensure only specific action chips (dB/lux) are interactive, preventing accidental UI triggers.

### 2. Global Communities and Real-time Chat
- **Persistent Communities**: Joined communities are stored in Firestore, following the user across logins and devices.
- **Global Group Chat**: Shared real-time messaging for each community powered by Firestore. All members see the same conversation history instantly.
- **Identity Clarity**: Messages carry the sender's real profile name and UID for accurate identity tracking and layout positioning.
- **Group Creation**: Create your own communities with custom descriptions and subjects (icons handled safely to ensure stability).

### 3. Analytics and Insights
- **Multi-User Data Isolation**: Private session history is securely partitioned per user. Even on shared devices, users only see their own focus data.
- **Interactive Charts**: Smooth bezier line charts with gradient fills displaying past focus sessions (powered by MPAndroidChart).
- **Study Metrics**: Tracks total session durations, environmental scores, and historical data.

### 4. Community Leaderboard
- **Real-time Rankings**: Social leaderboard that updates instantly via Firestore snapshot listeners.
- **Profile Integration**: Compare your average focus score and total hours against the global community.

### 5. Gamification and Achievements
- **Unlockable Badges**: Earn achievements based on lifetime focus hours, environmental scores, and consistency.
- **Custom Notifications**: Unique dark-themed Toast notifications appear when a new badge is unlocked.

### 6. Settings and Data Management
- **Customizable Thresholds**: Set personalized limits for maximum tolerable noise and minimum required light.
- **Data Export**: Dump your entire session history to a .csv file directly using MediaStore API.
- **Data Persistence**: Local database logic ensures session history survives sign-out/sign-in cycles.

## Technical Architecture

- **Language and UI**: Java, Modern XML Layouts, Material Design 3 Components.
- **Backend (Firebase)**:
  - **Firebase Auth**: Google Sign-In and Email/Password authentication.
  - **Cloud Firestore**: Real-time synchronization of group metadata, global chat messages, user profiles, and social stats.
- **Local Storage (Room)**: Robust SQLite abstraction for querying user-scoped session history and aggregated lifetime stats.
- **Navigation**: Dual-navigation approach using BottomNavigationView for core features and a NavigationView (Drawer) for account management.
- **Permissions**: Fully handles RECORD_AUDIO, POST_NOTIFICATIONS, INTERNET, and ACCESS_NOTIFICATION_POLICY.

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Minimum SDK: API 30 (Android 11.0)
- Target SDK: API 35 (Android 15)
- google-services.json file placed in the app/ directory.

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/EnviroSense.git
   cd EnviroSense
   ```

2. **Add Firebase Configuration**
   Obtain the google-services.json from the Firebase Console and place it in the app/ folder. This file is ignored by Git for security.

3. **Build and Run**
   Open the project in Android Studio, sync Gradle, and run it on a physical device or emulator.
