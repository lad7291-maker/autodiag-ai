# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# =============================================================================
# Room Database Rules
# =============================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Entity class * {
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.Embedded <fields>;
    <init>(...);
}
-keepclassmembers @androidx.room.Dao class * {
    @androidx.room.Query <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
}
-dontwarn androidx.room.paging.**

# =============================================================================
# Koin Dependency Injection Rules
# =============================================================================
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}
-keep @org.koin.core.annotation.* class * { *; }
-dontwarn org.koin.core.parameter.DefinitionParameters

# =============================================================================
# TensorFlow Lite Rules
# =============================================================================
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-keep class org.tensorflow.lite.nnapi.** { *; }
-keepclassmembers class * {
    @org.tensorflow.lite.Interpreter *;
}
-dontwarn org.tensorflow.lite.**

# =============================================================================
# Kotlin Serialization Rules
# =============================================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Transient <fields>;
}
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keep class * implements kotlinx.serialization.Serializable { *; }

# =============================================================================
# Kotlin Coroutines Rules
# =============================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**

# =============================================================================
# Ktor Client Rules
# =============================================================================
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**
-keep class io.ktor.client.engine.android.** { *; }

# =============================================================================
# Compose Rules
# =============================================================================
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# =============================================================================
# Navigation Component
# =============================================================================
-keep class androidx.navigation.** { *; }
-keep class * extends androidx.navigation.NavArgs { *; }
-keepclassmembers class * {
    @androidx.navigation.NavArgsFactory <methods>;
}

# =============================================================================
# DataStore Rules
# =============================================================================
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# =============================================================================
# Accompanist Permissions
# =============================================================================
-keep class com.google.accompanist.permissions.** { *; }

# =============================================================================
# Vico Charts
# =============================================================================
-keep class com.patrykandpatrick.vico.** { *; }

# =============================================================================
# OBD Java API
# =============================================================================
-keep class com.github.pires.obd.** { *; }
-dontwarn com.github.pires.obd.**

# =============================================================================
# Firebase Rules (if using ProGuard with Firebase)
# =============================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# =============================================================================
# General Android Rules
# =============================================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable implementations
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}

# Remove println statements
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
}
