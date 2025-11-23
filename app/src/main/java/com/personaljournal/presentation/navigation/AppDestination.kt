package com.personaljournal.presentation.navigation

sealed class AppDestination(val route: String, val icon: String) {
    data object Dashboard : AppDestination("dashboard", "ic_dashboard")
    data object Journal : AppDestination("journal", "ic_journal")
    data object Editor : AppDestination("editor", "ic_editor")
    data object Analytics : AppDestination("analytics_dashboard", "ic_analytics")
    data object Settings : AppDestination("settings", "ic_settings")
    data object Lock : AppDestination("lock", "ic_lock")
}
