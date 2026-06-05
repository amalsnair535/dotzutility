# Dotz Utility ProGuard Rules

# Keep Room entities
-keep class com.dotz.utility.data.** { *; }

# Keep Kotlin coroutine internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
