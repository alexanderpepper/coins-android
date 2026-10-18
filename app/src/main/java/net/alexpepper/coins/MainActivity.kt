package net.alexpepper.coins

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var pageLoaded = false
    private var pendingArguments: String? = null

    override fun onPause() {
        super.onPause()
        if (::webView.isInitialized) {
            webView.onPause()
            webView.pauseTimers()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.onResume()
            webView.resumeTimers()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences("shakables", MODE_PRIVATE)
        val isCoins = BuildConfig.FLAVOR == "coins"

        setContent {
            var screen by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(MenuScreen.MAIN)
            }
            var results by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf<ShakableResults?>(null)
            }
            var showInstructions by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(false)
            }
            var coinCount by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableIntStateOf(
                    preferences.getInt("coinCount", 1).coerceIn(1, 10)
                )
            }
            val diceCounts = listOf(
                DieCountSetting("d6", "d6DieCount", androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableIntStateOf(
                        preferences.getInt("d6DieCount", 1).coerceIn(0, 4)
                    )
                }),
                DieCountSetting("d4", "d4DieCount", androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableIntStateOf(
                        preferences.getInt("d4DieCount", 0).coerceIn(0, 4)
                    )
                }),
                DieCountSetting("d8", "d8DieCount", androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableIntStateOf(
                        preferences.getInt("d8DieCount", 0).coerceIn(0, 4)
                    )
                }),
                DieCountSetting("d10", "d10DieCount", androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableIntStateOf(
                        preferences.getInt("d10DieCount", 0).coerceIn(0, 4)
                    )
                }),
                DieCountSetting("d12", "d12DieCount", androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableIntStateOf(
                        preferences.getInt("d12DieCount", 0).coerceIn(0, 4)
                    )
                }),
                DieCountSetting("d20", "d20DieCount", androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableIntStateOf(
                        preferences.getInt("d20DieCount", 0).coerceIn(0, 4)
                    )
                }),
                DieCountSetting("d100", "d100DieCount", androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableIntStateOf(
                        preferences.getInt("d100DieCount", 0).coerceIn(0, 4)
                    )
                })
            )

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            webView = this
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)

                            val assetLoader = WebViewAssetLoader.Builder()
                                .addPathHandler(
                                    "/assets/",
                                    WebViewAssetLoader.AssetsPathHandler(context)
                                )
                                .build()

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    pageLoaded = true
                                    pendingArguments?.let { arguments ->
                                        runShakables(arguments)
                                        pendingArguments = null
                                    }
                                }

                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest
                                ): WebResourceResponse? =
                                    assetLoader.shouldInterceptRequest(request.url)
                            }

                            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                                WebViewCompat.addWebMessageListener(
                                    this,
                                    "nativeBridge",
                                    setOf("https://appassets.androidplatform.net")
                                ) { _, message, _, _, _ ->
                                    val data = message.data ?: run {
                                        Log.w("WebViewJS", "Received an empty results message")
                                        return@addWebMessageListener
                                    }
                                    val decoded = Json.decodeFromString<ShakableResults>(data)
                                    runOnUiThread {
                                        if (screen == MenuScreen.SHAKING) {
                                            results = decoded
                                            screen = MenuScreen.RESULTS
                                        }
                                    }
                                }
                            }

                            loadUrl("https://appassets.androidplatform.net/assets/index.html")
                        }
                    }
                )

                when (screen) {
                    MenuScreen.MAIN -> MainMenu(
                        isCoins = isCoins,
                        coinCount = coinCount,
                        diceCounts = diceCounts,
                        onCoinCountChange = { coinCount = it },
                        onDieCountChange = { type, count ->
                            diceCounts.first { it.dieType == type }.count.intValue = count
                        },
                        onStart = {
                            if (isCoins) {
                                preferences.edit().putInt("coinCount", coinCount).apply()
                            } else {
                                preferences.edit().apply {
                                    diceCounts.forEach { setting ->
                                        putInt(setting.argumentName, setting.count.intValue)
                                    }
                                }.apply()
                            }
                            results = null
                            screen = MenuScreen.SHAKING
                            startShaking(createArgumentsJson(isCoins, coinCount, diceCounts))
                        },
                        onInstructions = { showInstructions = true }
                    )

                    MenuScreen.RESULTS -> ResultsMenu(
                        isCoins = isCoins,
                        results = requireNotNull(results),
                        onShakeAgain = {
                            screen = MenuScreen.SHAKING
                            startShaking(createArgumentsJson(isCoins, coinCount, diceCounts))
                        },
                        onMainMenu = { screen = MenuScreen.MAIN }
                    )

                    MenuScreen.SHAKING -> Unit
                }
            }

            if (showInstructions) {
                InstructionsDialog(onDismiss = { showInstructions = false })
            }
        }
    }

    private fun startShaking(arguments: String) {
        pendingArguments = arguments
        if (pageLoaded) {
            runShakables(arguments)
            pendingArguments = null
        }
    }

    private fun runShakables(arguments: String) {
        val javaScript = """
            if (window.shakables) {
                window.shakables.dispose()
            }
            window.shakables = new window.CoinBox($arguments)
        """.trimIndent()
        webView.evaluateJavascript(javaScript) { value ->
            Log.d("WebViewJS", "Shakables initialized: $value")
        }
    }

    private fun createArgumentsJson(
        isCoins: Boolean,
        coinCount: Int,
        diceCounts: List<DieCountSetting>
    ): String {
        val counts = diceCounts.associate { it.argumentName to it.count.intValue }
        val arguments = ShakableArguments(
            coinCount = if (isCoins) coinCount else 0,
            d4DieCount = if (isCoins) 0 else counts.getValue("d4DieCount"),
            d6DieCount = if (isCoins) 0 else counts.getValue("d6DieCount"),
            d8DieCount = if (isCoins) 0 else counts.getValue("d8DieCount"),
            d10DieCount = if (isCoins) 0 else counts.getValue("d10DieCount"),
            d12DieCount = if (isCoins) 0 else counts.getValue("d12DieCount"),
            d20DieCount = if (isCoins) 0 else counts.getValue("d20DieCount"),
            d100DieCount = if (isCoins) 0 else counts.getValue("d100DieCount")
        )
        return Json.encodeToString(arguments)
    }
}

private enum class MenuScreen {
    MAIN,
    RESULTS,
    SHAKING
}
