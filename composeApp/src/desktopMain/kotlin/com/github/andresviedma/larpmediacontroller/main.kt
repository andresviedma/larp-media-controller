package com.github.andresviedma.larpmediacontroller

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Larp Media Controller",
    ) {
        App()
    }
}
