package com.github.andresviedma.larpmediacontroller.sound

import com.github.andresviedma.larpmediacontroller.DeviceController
import com.github.andresviedma.larpmediacontroller.gui.getPlatform
import io.github.oshai.kotlinlogging.KotlinLogging
import korlibs.audio.sound.PlaybackParameters
import korlibs.audio.sound.PlaybackTimes
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.playing
import korlibs.audio.sound.readMusic
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

class MusicController(
    private val baseLarpDir: String,
    private val config: MusicControllerConfig

) : DeviceController {
    private var openChannel: SoundChannel? = null
    private val channelMutex = Mutex()
    private val logger = KotlinLogging.logger {}

    val playbackStatus get() = MusicPlaybackStatus(
        hasCurrentMusic = (openChannel != null),
        isPlaying = (openChannel?.playing ?: false),
        currentPosition = openChannel?.current,
        currentLength = openChannel?.total,
    )

    override suspend fun reset() {
        channelMutex.withLock {
            openChannel?.stop()
            openChannel = null
            logger.info { "Music stopped ok" }
        }
    }

    override suspend fun off() {
        reset()
    }

    suspend fun play(music: MusicPlayback) {
        when {
            music.off -> reset()

            (config.files != null && music.file != null) -> {
                if (playbackStatus.isPlaying) reset()
                if (music.afterMillis != null && music.afterMillis > 0) {
                    delay(music.afterMillis)
                }

                channelMutex.withLock {
                    val vfs = if (music.file.startsWith("/")) {
                        getPlatform().vfs
                    } else {
                        getPlatform().vfs[baseLarpDir]
                    }
                    val base = vfs[config.files]
                    val file = base[music.file]
                    if (!file.exists()) {
                        logger.warn { "File not found: ${music.file}" }
                        println("${vfs.exists()} - ${vfs.absolutePath}")
                        println("${base.exists()} - ${base.absolutePath}")
                        println("${file.exists()} - ${file.absolutePath}")
                    } else {
                        logger.info { "Playing ${music.file}..." }
                        val sound = file.readMusic()
                        val channel = sound.play(PlaybackParameters(times = music.playbackTimes()))
                        openChannel = channel
                        logger.info { "Played ${music.file} started OK" }
                    }
                }
            }
        }
    }

    fun togglePlay() {
        openChannel?.togglePaused()
    }

    suspend fun stop() {
        reset()
    }

    fun goTo(position: Duration) {
        openChannel?.current = position
    }

    private fun MusicPlayback.playbackTimes(): PlaybackTimes =
        if (loop) PlaybackTimes.INFINITE else PlaybackTimes.ONE
}
