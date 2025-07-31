package com.github.andresviedma.larpmediacontroller

import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import korlibs.io.async.launch

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Larp Media Controller",
        onPreviewKeyEvent = {
            asyncLauncher.launch { triggerLarpKeyAction(it.key.nativeKeyCode) }
            true
        }
    ) {
        App()
    }
}
