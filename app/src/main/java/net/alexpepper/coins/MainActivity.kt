package net.alexpepper.coins

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature


class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView

    override fun onPause() {
        super.onPause()
        webView.onPause()
        webView.pauseTimers()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            val assetLoader: WebViewAssetLoader = WebViewAssetLoader.Builder()
              .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this.context))
              .build()

            this.webViewClient = object : WebViewClient() {
              override fun onPageFinished(view: WebView?, url: String?) {
                  super.onPageFinished(view, url)

                  // 3. Define the JS snippet you want to run
                  val javaScript = """
                      window.onload = new window.CoinBox({ coinCount: 1 })
                  """.trimIndent()

                  // 4. Safely execute the JavaScript snippet
                  view?.evaluateJavascript(javaScript, ValueCallback { value ->
                      // Logs the return value from the script ("Success! Background color changed.")
                      Log.d("WebViewJS", "Return value: $value")
                  })
              }

              override fun shouldInterceptRequest(
                  view: WebView?,
                  request: WebResourceRequest
              ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
            }

            // 1. Ensure the feature is supported
            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {

              // 2. Define allowed web origins for safety
              val allowedOrigins = setOf("https://appassets.androidplatform.net")

              // 3. Add the listener
              WebViewCompat.addWebMessageListener(
                  this,
                  "nativeBridge",     // Name of the object injected into JS
                  allowedOrigins,     // Allowed domains list
                  { view, message, sourceOrigin, isMainFrame, replyProxy ->
                      // Handle the message sent from JavaScript
                      val receivedText = message.data

                      // Execute your native code here (e.g., show a toast or log data)
                      println("Received from JS: $receivedText")
                  }
              )
            }

            loadUrl("https://appassets.androidplatform.net/assets/index.html")
        }
        setContentView(this.webView)
    }
}