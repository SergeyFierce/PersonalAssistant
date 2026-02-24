---
name: total-android-project-analysis
overview: Полный структурированный аудит Android-проекта Personal Assistant по 15 разделам (мета-информация, архитектура, UI, данные, асинхронность, безопасность, тесты, CI/CD и т.д.).
todos:
  - id: scan-meta
    content: Собрать и зафиксировать всю мета-информацию о проекте (Gradle, AGP, SDK, модули, структура файлов).
    status: completed
  - id: analyze-architecture
    content: Проанализировать архитектуру (слои, ViewModel, use cases, репозитории, DI, навигацию) и задокументировать паттерны/антипаттерны.
    status: in_progress
  - id: review-ui
    content: Провести детальный анализ UI (Compose-экраны, тема, адаптация под устройства, навигационные компоненты).
    status: in_progress
  - id: audit-storage-and-security
    content: Проанализировать хранение данных, миграции, сетевую безопасность и правила ProGuard/R8, зафиксировать сильные и слабые места.
    status: in_progress
  - id: review-tests-and-ci
    content: Проанализировать unit и instrumented тесты, конфигурацию CI, release checklist и стратегию версий.
    status: in_progress
  - id: compile-final-report
    content: Собрать итоговый отчёт по 15 разделам с оценками и рекомендациями.
    status: pending
isProject: false
---

# План тотального анализа Android‑проекта Personal Assistant

## Этап 1. Сбор мета‑информации о проекте

- **Gradle и Android конфигурация**: подробно разобрать `[build.gradle.kts](app/build.gradle.kts)`, корневой `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle-wrapper.properties`.
- **Манифест и базовые параметры**: извлечь package name, minSdk/targetSdk/compileSdk, компоненты и permissions из `[app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml)`.
- **Версии и сборка**: зафиксировать `versionCode`/`versionName`, build types, signing, flavors (если есть), AGP/Gradle/Java/Kotlin версии.
- **Структура модулей и файлов**: собрать список модулей из `settings.gradle.kts`, пройтись по дереву `app/src/main/java` и `app/src/main/res`, посчитать файлы по типам, оценить общий размер.

## Этап 2. Архитектура и паттерны

- **Слои и зависимости**: описать роли пакетов `core/datastore`, `core/domain`, `core/di`, `core/model`, `core/ui`, `ui/theme`; нарисовать mermaid‑диаграмму зависимостей слоёв.
- **Паттерн архитектуры**: классифицировать подход (MVVM + use cases, чистая архитектура light‑варианта), указать, где presentation/domain/data, где source of truth.
- **Управление состоянием**: разобрать `AppStateViewModel`, `AppStateUiState`, использование Flow/StateFlow/SharedFlow, единый messageEvent, UDF‑аспекты.
- **DI и Hilt**: проанализировать `[core/di/AppModule.kt](app/src/main/java/ru/topskiy/personalassistant/core/di/AppModule.kt)`, EntryPoint‑ы, связи ViewModel/UseCase/Repository, scopes.
- **Навигация**: разобрать `Navigation.kt`, стартовый поток (Bootstrap → Onboarding/Main), анимации переходов, передачу параметров.
- **Паттерны и антипаттерны**: отметить применения Repository, Use Case, Observer, Singleton (Application, Hilt‑модули), а также возможные антипаттерны (god‑object, дубли, жёсткие связи).

## Этап 3. UI и интерфейс

- **Технологии UI**: определить долю Compose vs XML, основные @Composable функции (`BootstrapScreen`, `OnboardingScreen`, `ServicesMainScreen`, `Settings*Screen` и т.д.).
- **Структура экранов**: описать роль каждого экрана и общих компонентов (`DrawerContent`, `DockBar`, `ServiceCatalogComponents`, `ScreenParams`).
- **Тема и стили**: проанализировать `[ui/theme/Theme.kt](app/src/main/java/ru/topskiy/personalassistant/ui/theme/Theme.kt)` и `[ui/theme/Color.kt](app/src/main/java/ru/topskiy/personalassistant/ui/theme/Color.kt)`, параметры MaterialTheme, поддержку dark/light.
- **Адаптация под экраны**: проверить использование `WindowInsets`, размеров в `dp`, поведение при повороте, наличие специальных ресурсов под sw‑квалификаторы (если есть).
- **Навигационные и AppBar элементы**: разобрать `TopBarDrawerGesture`, работу drawer, dock‑бара, тестовых tag‑ов для UI‑тестов.

## Этап 4. Данные и хранение

- **SettingsRepository слой**: детально описать интерфейс `SettingsRepository` и реализации `DataStoreSettingsRepository`, `EncryptedSettingsRepository`, ключи и формат хранения.
- **Миграция настроек**: разобрать `migrateDataStoreToEncryptedIfNeeded`, lazy‑миграцию в `EncryptedSettingsRepository.ensureMigrationDone()`, флаг `MIGRATION_DONE_KEY`.
- **Хранилища**: зафиксировать использование DataStore (Preferences), EncryptedSharedPreferences, отсутствие/наличие Room/SQLite/других БД и файлового хранения.
- **Модели данных**: описать `InitialSettings`, доменные модели в `core/model` (`ServiceId`, `ServiceRegistry`, `AppService`, `ServiceCategory`) и их связь с UI.

## Этап 5. Асинхронность и многопоточность

- **Корутинный стек**: рассмотреть использование `viewModelScope`, `ProcessLifecycleOwner.lifecycleScope`, `Dispatchers.IO/Main.immediate`, Mutex в миграции.
- **Потоки данных**: описать горячие/холодные потоки (StateFlow, SharedFlow), onStart/SharingStarted, обработку ошибок в flow.
- **Фоновые задачи**: проверить наличие WorkManager/Foreground Service/AlarmManager; если не используются — явно зафиксировать.

## Этап 6. Особенности Android и интеграции

- **Permissions и манифест**: проанализировать все permissions, exported‑компоненты, intent‑filters, deep links.
- **Network Security Config**: подтвердить запрет cleartext (`res/xml/network_security_config.xml`) и использование только HTTPS.
- **Firebase и Google сервисы**: описать подключение Crashlytics/Analytics, роль `google-services.json` (заглушка vs прод), Gradle‑плагины.

## Этап 7. Производительность и старт приложения

- **Инициализация Application**: разобрать `PersonalAssistantApp.onCreate()`, порядок инициализации Firebase, Crashlytics, темы/миграции.
- **Оптимизации UI**: просмотреть списки и анимации (каталог сервисов, онбординг), использование lazy‑компонентов, DiffUtil (если есть), управление alpha/тенями.
- **Память и батарея**: отследить потенциальные утечки (long‑lived scope, listeners), отсутствие лишних фоновых задач.

## Этап 8. Безопасность

- **Хранение секретов**: описать EncryptedSharedPreferences, миграцию, отсутствие хардкода секретов, использование MasterKeys/Keystore.
- **Сетевые аспекты**: подтвердить HTTPS‑только (Network Security Config), отсутствие ручного SSL pinning, минимум утечек в логах.
- **ProGuard/R8**: просмотреть `proguard-rules.pro`, оценить обфускацию, защиту Crashlytics маппингов.

## Этап 9. Тестирование

- **Unit‑тесты**: проанализировать тесты в `app/src/test` (`ServiceRegistryTest`, `SettingsRepositoryTest`, `MigrationTest`, `EncryptedSettingsRepositoryTest`, `AppStateViewModelTest`, `SettingsUseCaseTest`), их охват и сценарии.
- **Instrumented‑тесты**: рассмотреть `OnboardingMainManageSettingsUiTest`, `BootstrapFlowTest`, `AppContextAndSettingsInstrumentedTest` из `app/src/androidTest`, покрытие ключевых сценариев.
- **Стратегия тестирования**: оценить баланс unit/UI/интеграционных тестов, возможные пробелы.

## Этап 10. Зависимости и качество кода

- **Version catalog**: разобрать `gradle/libs.versions.toml` (если есть) или зависимости в `build.gradle.kts`, составить перечень библиотек (Compose, Hilt, DataStore, Security Crypto, Firebase и т.д.).
- **Линтеры и статический анализ**: описать конфиг detekt (`detekt.yml`), MagicNumber и другие правила, исключения.
- **Качество кода**: найти повторяющиеся куски, магические числа (вне UI‑разметки), TODO/FIXME, потенциальные code smells.

## Этап 11. Документация, CI/CD и релизы

- **README и docs/**: зафиксировать текущее содержание `README.md`, `docs/architecture.md`, `docs/release.md`; связь документации с реальным состоянием кода.
- **CI (GitHub Actions)**: проанализировать `.github/workflows/ci.yml`, шаги (assembleDebug, testDebugUnitTest), ограничения.
- **Release‑процесс**: описать чек‑лист из `docs/release.md`, стратегию версий (`versionCode`/`versionName`).

## Этап 12. Финальный отчёт

- **Структурированный отчёт по 15 разделам**: собрать результаты всех этапов в один большой отчёт, строго следуя структуре пользователя (части 1–15).
- **Диаграммы и таблицы**: добавить mermaid‑диаграммы для слоёв и навигации, сводные таблицы по зависимостям и тестам.
- **Оценки и рекомендации**: дать числовые оценки по архитектуре, качеству, производительности, безопасности, тестированию, документации; сформировать приоритезированный список улучшений.

