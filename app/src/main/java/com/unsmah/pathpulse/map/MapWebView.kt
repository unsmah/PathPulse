package com.unsmah.pathpulse.map

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Minimal WebView host for the JS map engines (MapLibre GL JS / Leaflet)
 * served from assets/ through the WebViewAssetLoader https origin.
 */
@SuppressLint("SetJavaScriptEnabled")
class MapWebView(context: Context) : WebView(context) {

    companion object {
        const val MAP_URL = "https://appassets.androidplatform.net/assets/index.html"
    }

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        webViewClient = WebViewClient()
    }

    fun loadMap(engine: String = "maplibre") {
        loadUrl("$MAP_URL?engine=$engine")
    }

    fun switchEngine(engine: String) {
        loadUrl("$MAP_URL?engine=$engine")
    }
}
