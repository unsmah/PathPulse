# PathPulse ProGuard/R8 rules (release builds only; debug is not minified).

# Keep @JavascriptInterface members reachable from the WebView JS bridge.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
