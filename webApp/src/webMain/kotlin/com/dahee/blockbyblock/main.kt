package com.dahee.blockbyblock

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class, kotlin.js.ExperimentalWasmJsInterop::class)
fun main() {
    ComposeViewport(viewportContainerId = "composeApp") {
        App()
        LaunchedEffect(Unit) {
            // Buffer 500ms so Skiko font engine completely decodes glyphs behind splash
            delay(500)
            hideSplashScreen()
        }
    }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { if (window.__hideSplash) window.__hideSplash(); }")
private external fun hideSplashScreen()