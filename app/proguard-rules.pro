# Proguard rules for PixelTimer - Countdown

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable ** Companion;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable ** $serializer;
}

# Keep models used in serialization
-keep @kotlinx.serialization.Serializable class com.pixelcountdown.data.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
