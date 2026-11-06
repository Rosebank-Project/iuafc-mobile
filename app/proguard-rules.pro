# R8 rules for the release build (isMinifyEnabled = true).
# Reference: Android Developers. 2026. Enable app optimization. [Online]. Available at:
# https://developer.android.com/topic/performance/app-optimization/enable-app-optimization
# [Accessed 6 November 2026].

# Keep line numbers in crash reports (Play Console de-obfuscates them with the mapping file).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove debug and verbose logs from release builds; warnings and errors are kept.
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# kotlinx.serialization: keep the generated serializers of the @Serializable rows and models
# (the library ships its own rules; these cover the app's classes).
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations
-keep,includedescriptorclasses class com.app.iuafc.**$$serializer { *; }
-keepclassmembers class com.app.iuafc.** {
    *** Companion;
}
-keepclasseswithmembers class com.app.iuafc.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor and supabase-kt reference classes that are not used on Android.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**
