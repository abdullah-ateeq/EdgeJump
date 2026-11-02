# EdgeJump 🎮

**EdgeJump** is a modern, feature-rich Android arcade jumping game built from the ground up using **Jetpack Compose** and **Material 3**. Designed with clean architecture and modern Android development best practices, EdgeJump delivers smooth gameplay, responsive controls, immersive audio-visual effects, and persistent game state management.

---

## 🌟 Features

- **Modern UI / UX:** Built entirely with Jetpack Compose and Material 3, featuring sleek animations, dynamic themes, and polished screens (Splash, Home, Game, Results, Settings, and About).
- **Arcade Gameplay Mechanics:** Dynamic platform generation, collision detection, and score tracking across multiple difficulty levels.
- **Immersive Feedback:** Integrated haptic feedback (`HapticController`) and sound effects (`SoundPlayer`) for tactile and audio immersion.
- **Persistent Preferences:** Powered by Jetpack DataStore to save user settings, game configurations, and high scores.
- **Robust Architecture:** MVVM architecture utilizing Kotlin Coroutines, StateFlow, ViewModel, and Navigation Compose for seamless screen transitions.

---

## 🛠️ Tech Stack & Libraries

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
- **Architecture:** MVVM (Model-View-ViewModel), StateFlow
- **Navigation:** Navigation Compose (`androidx.navigation.compose`)
- **Data Persistence:** Jetpack DataStore Preferences (`androidx.datastore`)
- **Concurrency:** Kotlin Coroutines & Flow
- **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`)

---

## 📂 Project Structure

```tree
EdgeJump/
├── app/
│   ├── src/main/java/com/example/edgejump/
│   │   ├── data/           # DataStore preferences repository
│   │   ├── game/           # Game engine, platform generator, sound & haptic controllers
│   │   ├── model/          # Game state, settings, difficulty, direction models
│   │   ├── ui/             # ViewModels, screens, navigation graph, and reusable components
│   │   └── MainActivity.kt # Entry point activity
│   └── build.gradle.kts
└── build.gradle.kts
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer recommended)
- **Android SDK** (Compile SDK 37, Min SDK 24)
- JDK 11 or higher

### Cloning and Building
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/EdgeJump.git
   cd EdgeJump
   ```
2. Open the project in Android Studio.
3. Sync project with Gradle files.
4. Run the app on an emulator or physical Android device (`API 24+`).

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

---

Developed with ❤️ by [Abdullah Ateeq](https://github.com/abdullah-ateeq).
