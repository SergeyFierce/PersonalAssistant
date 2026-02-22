## PersonalAssistant

Android‑приложение‑органайзер с набором сервисов (дела, заметки, проекты, финансы, лекарства и т.п.), объединённых в одном интерфейсе с настраиваемым набором и порядком сервисов.

### Краткий архитектурный обзор (5–10 минут)

**Слои и пакеты:**

| Слой | Путь | Назначение |
|------|------|------------|
| **datastore** | `core/datastore/` | Хранение настроек: интерфейс `SettingsRepository`, реализация `EncryptedSettingsRepository` (EncryptedSharedPreferences), модель `InitialSettings` для bootstrap. Unit-тесты используют `DataStoreSettingsRepository`. |
| **domain** | `core/domain/` | Use case настроек: интерфейс `SettingsUseCase`, реализация `SettingsUseCaseImpl` (делегирует в репозиторий). ViewModel и Application зависят от use case, а не от репозитория напрямую. |
| **di** | `core/di/` | Hilt-модуль `AppModule` (предоставление `SettingsRepository` и `SettingsUseCase`), EntryPoint для доступа к use case из `Application`. |
| **model** | `core/model/` | Доменная модель: enum `ServiceId`, `AppService`, `ServiceCategory`, единый реестр `ServiceRegistry`. |
| **ui** | `core/ui/` | Экранная логика и навигация: ViewModel (`AppStateViewModel`), экраны (Bootstrap, Onboarding, Main, ManageServices, Settings), док-бар, drawer, общие параметры `ScreenParams`. |
| **theme** | `ui/theme/` | Compose-темы, цвета, типографика. |

Каталог сервисов (экран «Управление сервисами») вынесен в отдельный файл `core/ui/ServiceCatalogComponents.kt`: переиспользуемые компоненты — карточка сервиса, список по категориям, сетка карточек, переключатель с `testTag` для UI-тестов.

**ServiceRegistry** — единственный источник истины по сервисам. Порядок отображения задаётся только списком `displayOrder` в `ServiceRegistry`; док, каталог и онбординг используют `all` и `groupedByCategory`. Добавление сервиса: новое значение в enum `ServiceId`, строка в `strings.xml`, запись в `servicesById` и позиция в `displayOrder`.

**Навигация:** стартовый маршрут — `bootstrap`. `BootstrapScreen` читает `getInitialSettings()` и переходит на `onboarding` (если онбординг не пройден) или `main`. Далее доступны маршруты `main`, `manage_services`, `settings`; переход в боковое меню (drawer) и назад через `NavController`.

**Где хранятся настройки:** в продакшене — EncryptedSharedPreferences (androidx.security:security-crypto), файл `encrypted_settings`; ключи те же (включённые сервисы, избранный/последний сервис, онбординг, тема, режим каталога, уведомления). При первом запуске после обновления с версии на обычном DataStore выполняется однократная миграция данных в зашифрованное хранилище. При первом запуске приложения тема инициализируется по системной в `PersonalAssistantApp.onCreate()`.

**Тесты:** unit-тесты на JVM покрывают `ServiceRegistry`, `SettingsRepository` и `AppStateViewModel`; instrumented-тесты — старт приложения, доступ к настройкам через Hilt и ключевые UI-сценарии (онбординг, главная, управление сервисами, настройки).

### Технологический стек
- **Язык**: Kotlin
- **UI**: Jetpack Compose (Material 3, Navigation Compose)
- **DI**: Hilt
- **Хранилище настроек**: EncryptedSharedPreferences (androidx.security:security-crypto) для зашифрованного хранения; при сбое инициализации — fallback на DataStore (Preferences) без шифрования.
- **Crashlytics и аналитика**: Firebase (Crashlytics, Analytics)
- **Прочее**: KSP, ViewModel, Coroutines

### Требования
- **minSdk**: 24  
- **targetSdk / compileSdk**: 36

### Стратегия версий
- **versionCode** — целое число, инкрементируется при каждом релизе (для магазина должно быть строго больше предыдущей версии).
- **versionName** — семантическое версионирование **MAJOR.MINOR.PATCH** (например, 1.2.0). Задаётся в `app/build.gradle.kts`; чек-лист перед релизом — [docs/release.md](docs/release.md).

### Структура проекта

Дерево пакетов и основных файлов (исходники в `app/src/main/java/`):

```
ru.topskiy.personalassistant
├── MainActivity.kt
├── PersonalAssistantApp.kt
├── core.datastore
│   ├── EncryptedSettingsRepository.kt
│   └── SettingsRepository.kt
├── core.domain
│   ├── SettingsUseCase.kt
│   └── SettingsUseCaseImpl.kt
├── core.di
│   └── AppModule.kt
├── core.model
│   ├── AppService.kt
│   ├── ServiceCategory.kt
│   ├── ServiceId.kt
│   └── ServiceRegistry.kt
├── core.ui
│   ├── AppStateViewModel.kt
│   ├── BootstrapScreen.kt
│   ├── DockBar.kt
│   ├── DrawerContent.kt
│   ├── ManageServicesScreen.kt
│   ├── Navigation.kt
│   ├── OnboardingScreen.kt
│   ├── ScreenParams.kt
│   ├── ServiceCatalogComponents.kt
│   ├── ServicesMainScreen.kt
│   ├── SettingsAboutScreen.kt
│   ├── SettingsAppearanceScreen.kt
│   ├── SettingsComponents.kt
│   ├── SettingsNotificationsScreen.kt
│   ├── SettingsPrivacyScreen.kt
│   ├── SettingsScreen.kt
│   ├── ThemeAnimation.kt
│   └── TopBarDrawerGesture.kt
└── ui.theme
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

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

### Запуск тестов

**Unit-тесты** (JVM, без эмулятора):

На Windows:
```bash
gradlew.bat :app:testDebugUnitTest
```

На macOS / Linux:
```bash
./gradlew :app:testDebugUnitTest
```

Отчёты: `app/build/reports/tests/testDebugUnitTest/`.

**Instrumented-тесты** (требуется устройство или эмулятор):

```bash
./gradlew :app:connectedDebugAndroidTest
```
(На Windows: `gradlew.bat :app:connectedDebugAndroidTest`.)

### CI (GitHub Actions)
В пайплайне (`.github/workflows/ci.yml`) при push/PR в `main`/`master` выполняются: **assembleDebug** и **testDebugUnitTest**. Instrumented-тесты в CI не запускаются; их нужно прогонять вручную или локально на устройстве/эмуляторе (`connectedDebugAndroidTest`).

### Безопасность
- **Настройки**: хранятся в EncryptedSharedPreferences (androidx.security:security-crypto); ключ через MasterKeys.
- **Fallback**: при ошибке инициализации EncryptedSettingsRepository (например, на эмуляторе без поддержки) используется обычный DataStore без шифрования.
- **Сеть**: cleartext-трафик запрещён через Network Security Config (`res/xml/network_security_config.xml`, `base-config cleartextTrafficPermitted="false"`).
- **Секреты**: ключи, токены и пароли не захардкожены в коде; в логах (Log.*) только обобщённые сообщения и исключения без вывода пользовательских данных.
- **Firebase**: для продакшена замените заглушку `app/google-services.json` на файл из вашего проекта в Firebase Console.

Подробнее об архитектурных решениях по безопасности — в [docs/architecture.md](docs/architecture.md).

### Firebase (Crashlytics и Analytics)
В проекте подключены Firebase Crashlytics (сбор Java/Kotlin и нативных падений) и Firebase Analytics (логирование открытий экранов: онбординг, главная, настройки, управление сервисами). В репозитории лежит заглушка `app/google-services.json`. Для работы с реальным проектом замените её на файл из [Firebase Console](https://console.firebase.google.com/) (добавьте Android‑приложение с package name `ru.topskiy.personalassistant` и скачайте `google-services.json`).

