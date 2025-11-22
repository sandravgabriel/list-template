# List Template: Android App Template

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.11.01-brightgreen.svg)](https://developer.android.com/jetpack/compose)

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
*   **Adaptive UI:** Implements a list-detail view for different screen sizes using [Material 3 Adaptive components](https://m3.material.io/libraries/adaptive/overview), specifically `NavigableListDetailPaneScaffold`.
*   **Testing:** Includes some examples for Unit and UI tests. (more to come)
*   **Clean Code Principles:** Focus on readability and maintainability.

## 📱 Adaptive UI

This template utilizes Material 3 Adaptive components to create user interfaces that respond to different screen sizes and orientations, focusing on a common list-detail pattern.

*   **`NavigableListDetailPaneScaffold`:** The core of the adaptive layout is the `NavigableListDetailPaneScaffold`. This component from the Material 3 Adaptive library automatically adjusts the layout based on available screen width.
    *   On **smaller screens** (like most phones in portrait mode), it typically displays a single pane (e.g., the list of items). Selecting an item navigates to its detail view, replacing the list.
    *   On **larger screens** (like tablets, foldables, or phones in landscape mode), it can display two panes side-by-side: the list pane and the detail pane. Selecting an item in the list updates the content of the detail pane.
*   **State-Driven Navigation:** The content of the detail pane is controlled by a custom `DetailPaneState` (which includes `ViewItem`, `EditItem`, and `Hidden` states), managed by the `rememberListDetailPaneScaffoldNavigator`. This allows for clear and testable navigation logic within the detail view.
*   **Compose-Powered:** Screen configurations and pane management are handled declaratively in Jetpack Compose, allowing ViewModels to remain largely unaware of specific display sizes.

This approach provides a robust foundation for building responsive list-detail interfaces that adapt to a wide range of devices.

## 🚀 Getting Started

### Prerequisites

*   Android Studio (Latest Stable Version Recommended)
*   JDK 17 or higher

## 🏛️ Architecture

This template follows the **MVVM (Model-View-ViewModel)** architectural pattern:

*   **View (Composables):** Observes `StateFlow` from the `ViewModel` and renders the UI. Sends user events to the `ViewModel`.
*   **ViewModel:** Holds UI-related state (exposed via `StateFlow`) and handles UI logic. Interacts with Repositories to fetch and save data. `ViewModel`s are lifecycle-aware and generally do not need to be aware of the specific screen configuration.
*   **Model (Repositories & Data Sources):** Abstract the data sources (Room database, file system for images). Repositories provide a clean API for `ViewModel`s to access data.

## 🛠️ How To...

### Add a New List Item Property

1.  **Update Room Entity:**
    *   Open `app/src/main/java/.../data/ItemEntry.kt`.
    *   Add the new property to the `ItemEntry` data class and ensure it's reflected in the `@Entity` definition if it's a new column.
    *   If it's a schema change, create a new Room migration.
2.  **Update Data Model:**
    *   Update `Item.kt` to include the new property.
    *   Adjust any mapping functions (e.g., `toItem()`, `fromItemEntry()`).
3.  **Update UI & ViewModels:**
    *   Update ItemDetails and extension functions in `ItemEntryViewModel.kt` (and also the function `validateInput` if needed)
    *   Adjust Composable functions `ItemEntryScreen.kt` and `ItemDetailsScreen.kt` to display and handle the new property.

### Create a New Screen

(This section primarily describes creating full-screen destinations outside the list-detail view. For new states within the detail pane, see `DetailPaneState.kt`)

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
    *   Define a new route object for your screen (similar to `HomeDestination` or `ItemEntryDestination`). This object typically defines at least a `route` string.
    *   Add a new `composable` entry for this route within the `NavHost` in your main navigation setup (in `ListRandomizerApp.kt`)
    *   Implement navigation actions (e.g., using `navController.navigate(YourNewScreenDestination.route)`) in other parts of your app to navigate to and from your new screen.

## 🧪 Testing

The project includes a starter setup for testing:

*   **Unit Tests:** Located in `app/src/test/`. These test ViewModels and other business logic components using JUnit 4 and MockK. Turbine is used for testing Kotlin Flows.
*   **Instrumented/UI Tests (Optional - if you add them):** Located in `app/src/androidTest/`.
