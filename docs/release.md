# Release checklist

Краткий чек-лист перед релизом и перед выкладкой в магазин.

## Перед каждым релизом

1. **Версии**  
   В `app/build.gradle.kts`: увеличить `versionCode` на 1; выставить `versionName` по семантическому версионированию (MAJOR.MINOR.PATCH). См. [Стратегия версий](#стратегия-версий) в README.

2. **Сборка release**  
   ```bash
   ./gradlew :app:assembleRelease
   ```  
   (На Windows: `gradlew.bat :app:assembleRelease`.)  
   AAB для магазина: `./gradlew :app:bundleRelease` → `app/build/outputs/bundle/release/`.

3. **Unit-тесты**  
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```  
   Убедиться, что все проходят.

4. **Подпись AAB/APK**  
   Проверить, что release-сборка подписана настроенным keystore (signingConfig в `build.gradle.kts` или переменные окружения). Команда проверки подписи (пример):  
   `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`

5. **google-services.json**  
   Для продакшена заменить заглушку `app/google-services.json` на файл из Firebase-проекта продакшена (package name `ru.topskiy.personalassistant`). Не коммитить продакшен-файл с секретами в публичный репозиторий без необходимости.

6. **Проверка versionCode / versionName**  
   Убедиться, что `versionCode` больше, чем у предыдущей версии в магазине; `versionName` соответствует ожидаемой версии (например, 1.1.0).

## Опционально: перед выкладкой в магазин

- Прогнать **instrumented-тесты** на эмуляторе или устройстве:  
  `./gradlew :app:connectedDebugAndroidTest` (или `connectedReleaseAndroidTest` при настроенном тестовом билде).
- Ручная проверка ключевых сценариев: онбординг, главный экран, управление сервисами, настройки, смена темы.
- Проверить список разрешений и описание приложения в консоли магазина.
- При использовании ProGuard/R8 — убедиться, что mapping-файл загружается в Crashlytics (настроено в проекте).
