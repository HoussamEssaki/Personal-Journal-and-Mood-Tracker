package com.personaljournal.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.presentation.navigation.AppDestination
import com.personaljournal.presentation.navigation.FeatureDestination
import com.personaljournal.presentation.navigation.FeatureDestinations
import com.personaljournal.presentation.screens.analytics.AnalyticsRoute
import com.personaljournal.presentation.screens.calendar.CalendarViewRoute
import com.personaljournal.presentation.screens.dashboard.DashboardRoute
import com.personaljournal.presentation.screens.export.ExportShareRoute
import com.personaljournal.presentation.screens.editor.EditorRoute
import com.personaljournal.presentation.screens.goals.GoalsHabitsRoute
import com.personaljournal.presentation.screens.insights.TrendsInsightsRoute
import com.personaljournal.presentation.screens.journal.JournalRoute
import com.personaljournal.presentation.screens.mood.DetailedMoodSelectorRoute
import com.personaljournal.presentation.screens.notifications.NotificationsRemindersRoute
import com.personaljournal.presentation.screens.onboarding.SplashOnboardingScreen
import com.personaljournal.presentation.screens.personalization.PersonalizationRoute
import com.personaljournal.presentation.screens.profile.SettingsProfileScreen
import com.personaljournal.presentation.screens.search.SearchFiltersRoute
import com.personaljournal.presentation.screens.settings.SettingsRoute
import com.personaljournal.presentation.screens.support.SupportHelpRoute

@Composable
fun PersonalJournalApp(
    navController: NavHostController = rememberNavController()
) {
    val destinations = listOf(
        AppDestination.Dashboard,
        AppDestination.Journal,
        AppDestination.Analytics,
        AppDestination.Settings
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                destinations.forEach { destination ->
                    val selected = backStackEntry?.destination?.route == destination.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(AppDestination.Dashboard.route)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (destination) {
                                    AppDestination.Dashboard -> Icons.Outlined.Dashboard
                                    AppDestination.Journal -> Icons.Outlined.Book
                                    AppDestination.Analytics -> Icons.Outlined.Analytics
                                    AppDestination.Settings -> Icons.Outlined.Settings
                                    else -> Icons.Outlined.Dashboard
                                },
                                contentDescription = when (destination) {
                                    AppDestination.Dashboard -> "Dashboard tab"
                                    AppDestination.Journal -> "Journal tab"
                                    AppDestination.Analytics -> "Analytics tab"
                                    AppDestination.Settings -> "Settings tab"
                                    else -> destination.route
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppDestination.Dashboard.route) {
                DashboardRoute(
                    onCreateEntry = { target -> navController.navigate(editorRoute(captureTarget = target)) },
                    onOpenEntry = { id -> navController.navigate(editorRoute(entryId = id)) },
                    onOpenFeature = { destination: FeatureDestination ->
                        navController.navigate(destination.route)
                    },
                    onOpenSettings = { navController.navigate(AppDestination.Settings.route) }
                )
            }
            composable(AppDestination.Journal.route) {
                JournalRoute(onOpenEntry = { id ->
                    navController.navigate(editorRoute(entryId = id))
                })
            }
            composable(
                route = "${AppDestination.Editor.route}?entryId={entryId}&captureTarget={captureTarget}",
                arguments = listOf(navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }, navArgument("captureTarget") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStack ->
                val id = backStack.arguments?.getLong("entryId")?.takeIf { it != -1L }
                val captureValue = backStack.arguments?.getString("captureTarget").orEmpty()
                val captureTarget = captureValue.takeIf { it.isNotBlank() }?.let {
                    runCatching { QuickCaptureTarget.valueOf(it) }.getOrNull()
                }
                EditorRoute(
                    entryId = id,
                    onSaved = { navController.popBackStack() },
                    onOpenMoodDetails = {
                        navController.navigate(FeatureDestinations.DetailedMoodSelector)
                    },
                    captureTarget = captureTarget
                )
            }
            composable(AppDestination.Analytics.route) {
                AnalyticsRoute()
            }
            composable(AppDestination.Settings.route) {
                SettingsRoute(
                    onOpenProfile = { navController.navigate(FeatureDestinations.SettingsProfile) },
                    onOpenNotifications = { navController.navigate(FeatureDestinations.Notifications) },
                    onOpenExportCenter = { navController.navigate(FeatureDestinations.ExportShare) }
                )
            }
            composable(FeatureDestinations.TrendsInsights) {
                TrendsInsightsRoute()
            }
            composable(FeatureDestinations.CalendarView) {
                CalendarViewRoute(
                    onViewEntry = { id ->
                        navController.navigate(editorRoute(entryId = id))
                    }
                )
            }
            composable(FeatureDestinations.DetailedMoodSelector) {
                DetailedMoodSelectorRoute(
                    onDone = { navController.popBackStack() }
                )
            }
            composable(FeatureDestinations.ExportShare) {
                ExportShareRoute()
            }
            composable(FeatureDestinations.GoalsHabits) {
                GoalsHabitsRoute()
            }
            composable(FeatureDestinations.Notifications) {
                NotificationsRemindersRoute()
            }
            composable(FeatureDestinations.Personalization) {
                PersonalizationRoute()
            }
            composable(FeatureDestinations.SearchFilters) {
                SearchFiltersRoute(
                    onOpenEntry = { id ->
                        navController.navigate(editorRoute(entryId = id))
                    }
                )
            }
            composable(FeatureDestinations.SettingsProfile) {
                SettingsProfileScreen()
            }
            composable(FeatureDestinations.SplashOnboarding) {
                SplashOnboardingScreen(
                    onGetStarted = {
                        navController.popBackStack()
                        navController.navigate(AppDestination.Dashboard.route) {
                            popUpTo(AppDestination.Dashboard.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(FeatureDestinations.SupportHelp) {
                SupportHelpRoute()
            }
        }
    }
}

private fun editorRoute(
    entryId: Long? = null,
    captureTarget: QuickCaptureTarget? = null
): String {
    val idSegment = entryId ?: -1L
    val captureSegment = captureTarget?.name ?: ""
    return "${AppDestination.Editor.route}?entryId=$idSegment&captureTarget=$captureSegment"
}
