# Personal Journal & Mood Tracker

Native Android app that lets users journal securely, track their mood, attach rich media, and surface insights – even when offline. The app follows Clean Architecture with an offline‑first data model, encrypted storage, and Jetpack Compose UI aligned with the provided design system.

## Architecture

```
┌───────────────────────────────────────────────────────────────┐
│                       Presentation Layer                      │
│ Jetpack Compose screens, ViewModels (Hilt), Navigation, M3 UI │
└──────────────▲──────────────────────────┬─────────────────────┘
               │                          │
               │ uses                     │ emits state
┌──────────────┴──────────────────────────▼─────────────────────┐
│                         Domain Layer                          │
│ Use cases (CreateEntry, SearchEntries, ExportReport…),        │
│ models (JournalEntry, Mood, Tag, MediaAttachment),            │
│ repository interfaces (Journal, Mood, Security, Settings…)    │
└──────────────▲──────────────────────────┬─────────────────────┘
               │ implements               │
┌──────────────┴──────────────────────────▼─────────────────────┐
│                           Data Layer                          │
│ - Room + SQLCipher entities/DAO/TypeConverters                │
│ - Repository impls (local + optional Firebase remote)         │
│ - Infrastructure (Encrypted media storage, PIN crypto,        │
│   Reminder scheduler, Backup/export manager, Analytics)       │
└───────────────────────────────────────────────────────────────┘
```

## Feature coverage (F01–F15)

| Area | Highlights |
| --- | --- |
| Journaling | Create/edit/delete entries with rich text blocks, prompts, tags, factors, encrypted media attachments, search & multi‑criteria filters |
| Mood tracking | Circular selector for core mood (Excellent → Terrible), secondary emotions/factors, calendar & timeline heatmap |
| Media & tags | Encrypted photo/audio storage (AES‑GCM per file), quick tag chips (#work, #gratitude…), memo support hooks |
| Insights | Dashboard metrics (streak/entries), analytics screen with line chart, correlations scaffold, export as PDF/CSV, offline backup zip |
| Security | SQLCipher Room DB, EncryptedSharedPreferences, PIN lock with biometric fallback, TLS + pinning config, private storage exports |
| UX | Material 3 Compose, adaptive typography, high‑contrast palette, TalkBack friendly components, navigation bar between core screens |
| Settings | Theme (light/dark/system), FR/EN locale switch, reminder scheduling (WorkManager), backup/export + PIN/biometric toggles |

## Tech stack

- **Language/Tools:** Kotlin 1.9, Gradle 8, Compose 1.5, Material 3, Navigation Compose
- **Architecture:** Clean Architecture, MVVM, Flow/Coroutines, Hilt DI
- **Storage:** Room 2.6 + SQLCipher (SupportFactory), EncryptedSharedPreferences, encrypted media files
- **Infra:** WorkManager reminders, Firebase Auth/Firestore/Storage/Analytics ready via optional remote repository
- **Images/Media:** Coil 2.4, custom AES media vault, audio recorder utility
- **Testing:** JUnit5 (unit), MockK, Room instrumentation test, Compose ready hooks

## Getting started

1. **Install dependencies**
   ```bash
   ./gradlew tasks
   ```
   (First run downloads AGP, Kotlin, Compose BOM, SQLCipher, Firebase BOM, etc.)

2. **Build & run**
   ```bash
   ./gradlew assembleDebug
   ```
   Install on API 24–34 devices/emulators. App launches to the PIN keypad (default empty – set one under Settings → Security).

3. **Seed data**
   On first launch the `SeedBootstrapUseCase` preloads moods/tags. Additional sample entries live in `app/src/main/assets/seed/journal_seed.json` for UI previews/tests.

## Testing

| Scope | Command |
| --- | --- |
| Unit tests (use cases, repositories) | `./gradlew testDebugUnitTest` |
| Instrumented Room test | `./gradlew connectedDebugAndroidTest` |
| Lint (Android Lint) | `./gradlew lintDebug` |

Tests cover core flows (entry creation analytics hook, Room search filters). Add more UI/E2E tests by extending Compose test harness (`compose-ui-test` dependency already included).
Detekt/Ktlint configs can be wired easily if your CI requires them (Gradle already exposes the necessary dependencies).

## Offline-first & security notes

- **Offline-first:** ViewModels subscribe to `Flow` from Room; remote sync pushes pending changes to Firestore when connectivity exists. Conflict resolution uses last-writer timestamp.
- **Encryption at rest:** SQLCipher for Room via `SupportFactory`, keys stored in EncryptedSharedPreferences. Media saved through `EncryptedMediaStorage` (AES-256-GCM per file with IV header).
- **Preferences & PIN:** Stored in `SecurePreferences` (EncryptedSharedPreferences). PIN hashed with SHA-256. Lock state managed via `SecurityRepository`.
- **In transit:** Network Security Config denies cleartext and pins a placeholder host certificate; ready for production pins. Firebase stack uses TLS 1.3.
- **Export/backup:** Exports stay in app private storage (`files/exports`). Backup zip includes SQLCipher DB for manual copy; user-triggered only.

## Build modules & packages

- `com.personaljournal.di`: Hilt modules (Dispatchers, Database, Network, Repository bindings)
- `domain`: Models (`JournalEntry`, `Mood`, `Tag`, `MediaAttachment`, `StatsSnapshot`, etc.), repository interfaces, and use cases (`CreateJournalEntry`, `SearchEntries`, `GenerateStats`, `ExportReport`, security/reminder/theme settings)
- `data`: Room entities/DAO, mappers, repository implementations, Firebase remote adapter
- `infrastructure`: Security (AES cipher, encrypted preferences, Biometric helper), storage (media vault, report exporter, backup), reminders, analytics, media recorder
- `presentation`: Compose theme, navigation + screens (Dashboard, Journal, Editor, Analytics, Settings, Lock), shared components (MoodSelector, EntryCard, RichTextEditor, StatsChart)
- `util`: Prompt provider, notifier, logger, Resource wrapper

## Extending the app

- **Sync policies:** Plug a real Firestore strategy into `FirestoreJournalRemoteDataSource` (conflict resolver hook already present).
- **Media capture:** Connect `EditorRoute.onRequestMedia` to `ActivityResult` photo/audio pickers, pipe resulting bytes into `SaveMediaAttachmentUseCase`.
- **More analytics:** Feed tags/factors into `StatsSnapshot.correlations` and surface additional charts.
- **Detekt/Ktlint:** Config stubs in Gradle make it easy to add your preferred static analysis or CI gates.

---

Happy journaling! Let me know if you need help wiring new screens, cloud sync behavior, or CI/CD workflows. 💙
