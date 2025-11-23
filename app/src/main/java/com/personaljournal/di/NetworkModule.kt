package com.personaljournal.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.personaljournal.util.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirestore(
        @ApplicationContext context: Context,
        logger: AppLogger
    ): FirebaseFirestore? {
        val app = ensureFirebaseApp(context, logger) ?: return null
        return FirebaseFirestore.getInstance(app)
    }

    @Provides
    @Singleton
    fun provideAnalytics(
        @ApplicationContext context: Context,
        logger: AppLogger
    ): FirebaseAnalytics? = runCatching { FirebaseAnalytics.getInstance(context) }
        .onFailure { logger.w("firebase_init", "Firebase Analytics disabled: ${it.message}", it) }
        .getOrNull()

    private fun ensureFirebaseApp(
        context: Context,
        logger: AppLogger
    ): FirebaseApp? {
        FirebaseApp.getApps(context).firstOrNull()?.let { return it }
        val hasGoogleConfig =
            context.resources.getIdentifier("google_app_id", "string", context.packageName) != 0
        if (!hasGoogleConfig) {
            logger.w(
                "firebase_init",
                "google-services.json not found. Remote sync will be skipped in this build."
            )
            return null
        }
        return FirebaseApp.initializeApp(context).also {
            if (it == null) {
                logger.w(
                    "firebase_init",
                    "Unable to initialize Firebase with provided resources."
                )
            }
        }
    }
}
