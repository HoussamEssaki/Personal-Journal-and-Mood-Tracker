package com.personaljournal.presentation.navigation

data class FeatureDestination(
    val route: String,
    val title: String,
    val description: String
)

object FeatureDestinations {
    const val TrendsInsights = "trends_insights"
    const val CalendarView = "calendar_view"
    const val DetailedMoodSelector = "detailed_mood_selector"
    const val ExportShare = "export_share"
    const val GoalsHabits = "goals_habits"
    const val Notifications = "notifications_reminders"
    const val Personalization = "personalization_themes"
    const val SearchFilters = "search_filters"
    const val SettingsProfile = "settings_profile"
    const val SplashOnboarding = "splash_onboarding"
    const val SupportHelp = "support_help"

    val showcase = listOf(
        FeatureDestination(TrendsInsights, "Trends & Insights", "Correlations and mood over time"),
        FeatureDestination(CalendarView, "Calendar View", "Browse entries in a calendar layout"),
        FeatureDestination(DetailedMoodSelector, "Detailed Mood Selector", "Tag nuanced feelings quickly"),
        FeatureDestination(ExportShare, "Export & Share", "Generate PDF, JSON, or CSV exports"),
        FeatureDestination(GoalsHabits, "Goals & Habits", "Track streaks and habit completion"),
        FeatureDestination(Notifications, "Notifications & Reminders", "Tune reminders and smart alerts"),
        FeatureDestination(Personalization, "Personalization & Themes", "Customize colors and layout"),
        FeatureDestination(SearchFilters, "Search & Filters", "Find entries by mood, tag, or range"),
        FeatureDestination(SettingsProfile, "Settings & Profile", "Manage account and privacy"),
        FeatureDestination(SplashOnboarding, "Splash & Onboarding", "First run onboarding carousel"),
        FeatureDestination(SupportHelp, "Support & Help", "Guides, resources, and crisis contacts")
    )
}
