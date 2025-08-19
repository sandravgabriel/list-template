# List Template: Android App Template

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.08.00-brightgreen.svg)](https://developer.android.com/jetpack/compose)

A modern Android application template built with Kotlin and Jetpack Compose, designed to provide a solid foundation for list-based applications. It features local data storage using Room and image handling with PhotoPicker and Coil.

## ✨ Features

*   **100% Kotlin:** Including asynchronous operations with Coroutines and Flows.
*   **Modern UI Toolkit:** Built entirely with [Jetpack Compose](https://developer.android.com/jetpack/compose) for a declarative and reactive UI.
*   **Navigation:** Handled by [Navigation Compose](https://developer.android.com/jetpack/compose/navigation).
*   **Local Data Persistence:** Utilizes [Room Persistence Library](https://developer.android.com/training/data-storage/room) for structured local data storage.
*   **Image Handling:**
    *   Image selection via [PhotoPicker](https://developer.android.com/training/data-storage/shared/photopicker).
    *   Efficient image loading and caching with [Coil](https://coil-kt.github.io/coil/).
*   **MVVM Architecture:** Follows a Model-View-ViewModel pattern for a clear separation of concerns and testability.
*   **Adaptive UI:** Basic support for different screen sizes (e.g., list-detail view on larger screens).
*   **Testing:** Includes some examples for Unit and (optional) UI tests. (more to come)
*   **Clean Code Principles:** Focus on readability and maintainability.

## 📱 Adaptive UI

This template incorporates a basic structure for building adaptive user interfaces that respond to different screen sizes and orientations.

*   **Responsive Layouts:** The UI is designed to adjust its layout based on the available screen width. For example, a typical list-detail scenario might display as a single pane (list navigates to detail) on smaller screens (phones) and transition to a two-pane layout (list and detail side-by-side) on larger screens (tablets, foldables, desktops).
*   **Compose-Powered:** Leveraging the power of Jetpack Compose, screen configurations are typically determined at the Composable level, allowing ViewModels to remain largely unaware of specific display sizes.
*   **Foundation for Growth:** While this template provides a foundational adaptive setup, it is designed to be easily extensible. You can build upon this base to implement more sophisticated adaptive behaviors and support a wider range of screen configurations as your application evolves.
<!-- Further details on the specific implementation and how to extend the adaptive behavior can be found in the project's [Wiki](LINK_TO_YOUR_WIKI_PAGE_LATER). -->

## 🚀 Getting Started

### Prerequisites

*   Android Studio [Latest Stable Version Recommended]
*   JDK 17 or higher

### Key Components

*   **`app/src/main/java/de/gabriel/listtemplate/data/`**: Contains all data handling logic.
    *   **`local/`**: Room Database, Data Access Objects (DAOs), and Entities.
    *   **`PhotoSaverRepository.kt`**: Handles saving and retrieving image files selected via PhotoPicker.
    *   **`ItemsRepository.kt`**: Interface and implementation for accessing item data (combining Room data with file information).
*   **`app/src/main/java/de/gabriel/listtemplate/ui/`**: Contains all UI-related code.
    *   **`screens/`**: Each sub-package typically represents a screen with its `ViewModel` and Composable functions.
    *   **`navigation/AppNavigation.kt`**: Defines the navigation graph using Navigation Compose.
*   **`libs.versions.toml`**: Centralized dependency version management.

## 🏛️ Architecture

This template follows the **MVVM (Model-View-ViewModel)** architectural pattern:

*   **View (Composables):** Observes `StateFlow` from the `ViewModel` and renders the UI. Sends user events to the `ViewModel`. The UI is designed to be adaptive, utilizing Jetpack Compose's capabilities to adjust layouts based on available screen width. For instance, list-detail patterns may be displayed as a single pane on smaller screens and a two-pane layout on larger screens.
*   **ViewModel:** Holds UI-related state (exposed via `StateFlow`) and handles UI logic. Interacts with Repositories to fetch and save data. `ViewModel`s are lifecycle-aware and generally do not need to be aware of the specific screen configuration, as this is handled at the Composable level.
*   **Model (Repositories & Data Sources):** Abstract the data sources (Room database, file system for images). Repositories provide a clean API for `ViewModel`s to access data.

## 🛠️ How To...

### Add a New List Item Property

1.  **Update Room Entity:**
    *   Open `app/src/main/java/.../data/local/ItemEntry.kt`.
    *   Add the new property to the `ItemEntry` data class and ensure it's reflected in the `@Entity` definition if it's a new column.
    *   If it's a schema change, create a new Room migration.
2.  **Update Data Model (if separate):**
    *   If you have a domain `Item.kt` model, update it accordingly.
    *   Adjust any mapping functions (e.g., `toItem()`, `fromItemEntry()`).
3.  **Update UI & ViewModel:**
    *   Modify `ItemDetailsUiState.kt` and `HomeUiState.kt` (if the property is shown in the list) to include the new data.
    *   Update the `ViewModel`s (`HomeViewModel.kt`, `ItemDetailsViewModel.kt`) to handle the new property.
    *   Adjust Composable functions in `app/src/main/java/.../ui/screens/` to display or input the new property.

### Create a New Screen

1.  **Define State:** Create a `data class` for your new screen's UI state (e.g., `NewScreenUiState.kt`).
2.  **Create ViewModel:**
    *   Create a `NewScreenViewModel.kt` inheriting from `androidx.lifecycle.ViewModel`.
    *   Expose UI state via `StateFlow<NewScreenUiState>`.
    *   Implement logic to fetch/update data via repositories.
3.  **Create Composable:**
    *   Create `NewScreen.kt` with a `@Composable` function.
    *   Observe the `ViewModel`'s state.
    *   Build your UI using Jetpack Compose components.
4.  **Add Navigation:**
    *   Define a new route in `app/src/main/java/.../ui/navigation/AppNavigation.kt`.
    *   Add a `composable` destination for your new screen in the `NavHost`.
    *   Implement navigation actions to and from your new screen.

## 🧪 Testing

The project includes a starter setup for:

*   **Unit Tests:** Located in `app/src/test/`. These test ViewModels and other business logic components using JUnit 4 and MockK. Turbine is used for testing Kotlin Flows.
    *   Run with: `./gradlew testDebugUnitTest`
*   **Instrumented/UI Tests (Optional - if you add them):** Located in `app/src/androidTest/`.
    *   Run with: `./gradlew connectedAndroidTest`
