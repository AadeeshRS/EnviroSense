# EnviroSense

EnviroSense is a smart Android application designed to help users find their perfect environment for deep work and studying. By continuously monitoring ambient noise and light levels in real-time, the app calculates a live "Focus Score" to let you know if your current surroundings are optimal for productivity.

## Features

*   **Live Focus Score:** A dynamic, real-time score (0-100) calculated using an exponential moving average (EMA) based on your immediate environment.
*   **Ambient Noise Tracking:** Utilizes the device's microphone to monitor decibel (dB) levels and alerts you if noise spikes past your acceptable threshold.
*   **Light Sensor Integration:** Checks ambient lux values to ensure your workspace is properly lit (not too dim, not aggressively bright).
*   **Session Tracking:** Start, pause, and end focus sessions. 
*   **Local History (Room DB):** All completed sessions—including duration, final average focus score, and active location—are securely saved locally on the device using a Room SQLite database.
*   **Dynamic UI States:** Seamless transitions between First Launch, Default dashboard, Active Session monitoring, and Post-Session summary bottom sheets.

## Tech Stack

*   **Platform:** Android (Minimum SDK 24+)
*   **Language:** Java
*   **Architecture:** UI + Background Handlers + Local Storage
*   **Local Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room)
*   **UI Components:** XML ConstraintLayout, Material Design Components (Bottom Sheets, Material Buttons, custom drawables)
*   **Hardware APIs:** `MediaRecorder` (Audio Amplitude), `SensorManager` (Light Sensor)

## Getting Started

### Prerequisites
*   Android Studio (Latest Version Recommended)
*   An Android physical device or emulator (Note: A physical device is highly recommended to accurately test the microphone and light sensors).

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/yourusername/EnviroSense.git
    ```
2.  Open the project in Android Studio.
3.  Sync the Gradle files to download the required dependencies (Room DB, Material Components).
4.  Build and run the application on your connected device.

## Permissions

The app strictly prioritizes privacy and performs all hardware processing locally. It requires the following permissions:
*   `RECORD_AUDIO`: Used *only* to read the maximum amplitude of ambient sound to calculate decibel levels. No audio is ever recorded, saved, or transmitted.

## Roadmap (Upcoming Features)

*   [ ] **Analytics Dashboard:** A dedicated tab to view historical session trends, charts, and average scores per location.
*   [ ] **Settings & Customization:** Allow users to define their own custom "Optimal Thresholds" for noise and light.
*   [ ] **Geocoded Locations:** Implement `FusedLocationProviderClient` to auto-tag sessions with real-world locations (e.g., "Library", "Coffee Shop").
*   [ ] **Auto-Calibration:** Smart algorithms that learn from your best historical sessions to suggest your ideal environmental settings.

---
*Built with focus, Built for better focus.*
``` 🎯 Focus and Productivity.*
