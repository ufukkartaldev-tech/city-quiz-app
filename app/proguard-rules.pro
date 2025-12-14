# ============================================
# PROJECT SPECIFIC PROGUARD RULES
# ============================================

# ============================================
# OPTIMIZATION & OBFUSCATION
# ============================================
# Aggressive optimization for smaller APK
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preserve line numbers for Crashlytics debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# DATA MODELS & SERIALIZATION
# ============================================
# Keep Data Models for Gson/Firebase Serialization
-keep class com.example.oyun.data.** { *; }
-keepclassmembers class com.example.oyun.data.** { *; }

# Keep model field names for JSON serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================
# DEPENDENCY INJECTION (HILT)
# ============================================
-keep class com.example.oyun.Hilt_** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ============================================
# FIREBASE
# ============================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# Firebase Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# ============================================
# ADMOB (GOOGLE MOBILE ADS)
# ============================================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.ads.**

# ============================================
# ROOM DATABASE
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============================================
# VIEWBINDING
# ============================================
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(android.view.View);
    public static ** inflate(android.view.LayoutInflater);
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# ============================================
# KOTLIN & COROUTINES
# ============================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ============================================
# ANDROID COMPONENTS
# ============================================
# Keep Activities, Services, Receivers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ============================================
# SECURITY & ANTI-TAMPERING
# ============================================
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove debug code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullParameter(...);
}

# ============================================
# CRASHLYTICS
# ============================================
# Keep Crashlytics annotations
-keepattributes *Annotation*
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ============================================
# GSON (IF USED)
# ============================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================
# WARNINGS TO IGNORE
# ============================================
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**