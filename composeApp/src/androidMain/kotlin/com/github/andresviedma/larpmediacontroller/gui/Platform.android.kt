package com.github.andresviedma.larpmediacontroller.gui

import android.media.AudioManager
import android.os.Environment
import androidx.core.content.getSystemService
import korlibs.io.android.androidContext
import korlibs.io.file.std.localVfs
import kotlin.coroutines.coroutineContext

class AndroidPlatform : Platform {
    override val name: String = "android"
    override val vfs = localVfs(Environment.getExternalStorageDirectory())
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual suspend fun increaseSystemVolume() {
    changeVolume(AudioManager.ADJUST_RAISE)
}

actual suspend fun decreaseSystemVolume() {
    changeVolume(AudioManager.ADJUST_LOWER)
}

private suspend fun changeVolume(direction: Int) {
    val audioManager = coroutineContext.androidContext().getSystemService<AudioManager>()
    audioManager?.adjustVolume(
        direction,
        AudioManager.FLAG_PLAY_SOUND
    )
}
