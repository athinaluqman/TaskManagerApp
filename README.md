# TaskManagerApp

A simple Android Task Manager application built using Kotlin and Jetpack Compose. The app allows users to view, create, edit and delete tasks. Tasks can be filtered by status (All, Completed, Pending) and edited individually.

# Architecture

This app follows the MVVM (Model-View-ViewModel) architecture for clear separation of concerns. 

# State Management

The app uses the following for state management:
1. StateFlow inside the TaskViewModel to maintain the UI state.
2. TaskUiState holds the list of tasks, selected task, loading state, and filter selection.
3. Composables collect state using collectAsStateWithLifecycle(), ensuring safe lifecycle-aware updates.
4. Pagination is handled inside the ViewModel to prevent UI duplication or inconsistent data.

# API Integration

Retrofit is used for network calls to fetch, insert, update, and delete tasks.

# Known Issues / Limitations

1. Offline Caching: Offline functionality may not show the latest tasks
2. Theme Switching: Basic theme switching is implemented (light/dark toggle), but it does not persist across app restarts.
3. Date Handling: Dates from API are stored as ISO strings. If the API format changes or is missing, parsing may fail.
4. Pagination: Basic pagination works with API nextPageToken and only available with internet connection.

# Future Improvements

1. Implement offline caching with Room database and integrate pagination for cached tasks.
2. Improve theme persistence using SharedPreferences.
3. Handle date parsing more robustly, possibly storing dates as Long timestamps in the database.
4. Add search and sorting functionality for tasks.
5. Implement notifications for due tasks.
6. Enhance UI/UX with animations and better accessibility features.

