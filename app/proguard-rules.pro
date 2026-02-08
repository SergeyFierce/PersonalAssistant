# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ---------------------------------------------------------------------------
# Hilt
# ---------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class *_HiltComponents* { *; }
-keep class *_Factory { *; }
-keep class *_MembersInjector { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-dontwarn dagger.hilt.**

# ---------------------------------------------------------------------------
# DataStore Preferences
# ---------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.preferences.core.Preferences { *; }

# ---------------------------------------------------------------------------
# Модели для DataStore/Preferences и сериализации (ServiceId по имени в Preferences)
# ---------------------------------------------------------------------------
# Enum ServiceId сохраняется как .name и читается через valueOf() — нужны класс и константы
-keep class ru.topskiy.personalassistant.core.model.ServiceId { *; }
-keepclassmembers class ru.topskiy.personalassistant.core.model.ServiceId {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# InitialSettings — результат getInitialSettings(), используется при bootstrap
-keep class ru.topskiy.personalassistant.core.datastore.InitialSettings { *; }

# ---------------------------------------------------------------------------
# Остальное (опционально для отладки)
# ---------------------------------------------------------------------------
# Uncomment this to preserve the line number information for debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile