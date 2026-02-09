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
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.android.HiltAndroidApp class *
-dontwarn dagger.hilt.**

# ---------------------------------------------------------------------------
# DataStore Preferences (сериализация ключей и значений)
# ---------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.preferences.core.Preferences { *; }
-keepclassmembers class androidx.datastore.preferences.core.Preferences$Builder { *; }
-keep,allowobfuscation class androidx.datastore.preferences.protobuf.** { *; }

# ---------------------------------------------------------------------------
# Модели для DataStore и сериализации (ServiceId по имени в Preferences)
# ---------------------------------------------------------------------------
-keep class ru.topskiy.personalassistant.core.model.ServiceId { *; }
-keepclassmembers class ru.topskiy.personalassistant.core.model.ServiceId {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class ru.topskiy.personalassistant.core.datastore.InitialSettings { *; }
-keep class ru.topskiy.personalassistant.core.datastore.SettingsRepository { *; }
-keep class ru.topskiy.personalassistant.core.datastore.DataStoreSettingsRepository { *; }

# ---------------------------------------------------------------------------
# Firebase Crashlytics (читаемые стектрейсы и загрузка mapping)
# ---------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ---------------------------------------------------------------------------
# Firebase Analytics
# ---------------------------------------------------------------------------
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.firebase.analytics.**
-dontwarn com.google.android.gms.measurement.**