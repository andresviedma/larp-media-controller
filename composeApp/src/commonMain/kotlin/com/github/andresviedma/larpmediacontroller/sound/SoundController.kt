package com.github.andresviedma.larpmediacontroller.sound

import com.github.andresviedma.larpmediacontroller.DeviceController
import com.github.andresviedma.larpmediacontroller.gui.getPlatform
import io.github.oshai.kotlinlogging.KotlinLogging
import korlibs.audio.sound.PlaybackParameters
import korlibs.audio.sound.readSound

class SoundController(
    private val baseLarpDir: String,
    private val config: SoundControllerConfig,

) : DeviceController {
    private val logger = KotlinLogging.logger {}

    override suspend fun reset() {
    }

    override suspend fun off() {
    }

    suspend fun play(soundFile: SoundPlayback) {
        if (config.files != null) {
            val vfs = if (soundFile.file.startsWith("/")) {
                getPlatform().vfs
            } else {
                getPlatform().vfs[baseLarpDir]
            }
            val base = vfs[config.files]
            val file = base[soundFile.file]
            if (!file.exists()) {
                logger.warn { "File not found: ${soundFile.file}" }
            } else {
                logger.info { "Playing ${soundFile.file}..." }
                val sound = file.readSound(streaming = true) // Debería ser false, pero parece que no funciona en android ni en desktop
                sound.play(
                    PlaybackParameters(
                        volume = soundFile.effectiveVolumeLevel,
                    )
                )
            }
        }
    }
}
