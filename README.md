# Gameboxd

Final Project for Android Development Course.

Gameboxd is an Android application for tracking and managing a video game backlog. It is built using Kotlin and follows modern Android development practices, specifically the MVVM architecture and Room Database. The app features a dark, retro 8-bit aesthetic.

## Features

- Add, edit, and delete games from your backlog.
- Change game status (Backlog, Playing, Finished).
- Rate finished games (1-5 stars).
- Custom drag-and-drop ordering to prioritize games.
- Sort games alphabetically or by rating.
- "Pick Random Game" feature to help you decide what to play next.
- Filter games by their current status.
- Swipe-to-delete with Undo functionality.

## Technical Details

- **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern.
- **Database:** Room Database (SQLite ORM).
- **UI:** Exclusively uses ViewBinding (no findViewById). 
- **Asynchronous Operations:** Kotlin Coroutines and Flow for reactive database queries.
- **List Management:** RecyclerView with ListAdapter and DiffUtil for efficient UI updates. ItemTouchHelper for drag & drop and swipe gestures.

## Project Structure

- `data/`: Contains Room Database components (`Game` entity, `GameDao`, `GameDatabase`).
- `repository/`: Contains `GameRepository` as the single source of truth.
- `viewmodel/`: Contains `GameViewModel` for UI state management.
- `adapter/`: Contains `GameAdapter` and `GameTouchHelper`.
- `ui/`: Contains `AddGameDialogFragment` for adding and editing games.
- `MainActivity.kt`: Main entry point handling the RecyclerView and toolbar menus.

## Setup Instructions

1. Clone this repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync.
4. Run the app on an emulator or a physical device.

Minimum API level required: 24.
