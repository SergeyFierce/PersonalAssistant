## PersonalAssistant

Android‑приложение‑органайзер с набором сервисов (дела, заметки, проекты, финансы, лекарства и т.п.), объединённых в одном интерфейсе с настраиваемым набором и порядком сервисов.

### Краткий архитектурный обзор (5–10 минут)

**Слои и пакеты:**

| Слой | Путь | Назначение |
|------|------|------------|
| **datastore** | `core/datastore/` | Хранение настроек: интерфейс `SettingsRepository`, реализация `DataStoreSettingsRepository`, модель `InitialSettings` для bootstrap. |
| **di** | `core/di/` | Hilt-модуль `AppModule` (предоставление `SettingsRepository`), EntryPoint для доступа к репозиторию из `Application`. |
| **model** | `core/model/` | Доменная модель: enum `ServiceId`, `AppService`, `ServiceCategory`, единый реестр `ServiceRegistry`. |
| **ui** | `core/ui/` | Экранная логика и навигация: ViewModel (`AppStateViewModel`), экраны (Bootstrap, Onboarding, Main, ManageServices, Settings), док-бар, drawer, общие параметры `ScreenParams`. |
| **theme** | `ui/theme/` | Compose-темы, цвета, типографика. |

**ServiceRegistry** — единственный источник истины по сервисам. Порядок отображения задаётся только списком `displayOrder` в `ServiceRegistry`; док, каталог и онбординг используют `all` и `groupedByCategory`. Добавление сервиса: новое значение в enum `ServiceId`, строка в `strings.xml`, запись в `servicesById` и позиция в `displayOrder`.

**Навигация:** стартовый маршрут — `bootstrap`. `BootstrapScreen` читает `getInitialSettings()` и переходит на `onboarding` (если онбординг не пройден) или `main`. Далее доступны маршруты `main`, `manage_services`, `settings`; переход в боковое меню (drawer) и назад через `NavController`.

**Где хранятся настройки:** DataStore Preferences, файл `settings` (имя задаётся в `preferencesDataStore(name = "settings")`). Ключи: включённые сервисы, избранный/последний сервис, флаг онбординга, тема (light/dark), режим каталога (список/сетка). При первом запуске тема один раз инициализируется по системной в `PersonalAssistantApp.onCreate()`.

### Технологический стек
- **Язык**: Kotlin
- **UI**: Jetpack Compose (Material 3, Navigation Compose)
- **DI**: Hilt
- **Хранилище настроек**: DataStore (Preferences)
- **Crashlytics и аналитика**: Firebase (Crashlytics, Analytics)
- **Прочее**: KSP, ViewModel, Coroutines

### Требования
- **minSdk**: 24  
- **targetSdk / compileSdk**: 36

### Сборка и запуск в Android Studio
- Откройте папку проекта `PersonalAssistant` в Android Studio.
- Дождитесь синхронизации Gradle.
- Выберите конфигурацию запуска `app` и нажмите **Run** (Shift+F10).

### Сборка через Gradle из командной строки
На Windows:
```bash
gradlew.bat :app:assembleDebug
```

На macOS / Linux:
```bash
./gradlew :app:assembleDebug
```

Готовый APK будет лежать в `app/build/outputs/apk/debug/`.

### Firebase (Crashlytics и Analytics)
В проекте подключены Firebase Crashlytics (сбор Java/Kotlin и нативных падений) и Firebase Analytics (логирование открытий экранов: онбординг, главная, настройки, управление сервисами). В репозитории лежит заглушка `app/google-services.json`. Для работы с реальным проектом замените её на файл из [Firebase Console](https://console.firebase.google.com/) (добавьте Android‑приложение с package name `ru.topskiy.personalassistant` и скачайте `google-services.json`).

