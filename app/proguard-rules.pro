# PathPulse release keep rules (R8).

# Keep the WebView JS bridge surface intact.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Room entity metadata usable under reflection-based tooling.
-keep class com.unsmah.pathpulse.data.entity.** { *; }

# Kotlin coroutines debug metadata trimming.
-dontwarn kotlinx.coroutines.**
