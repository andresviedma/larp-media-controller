package com.github.andresviedma.larpmediacontroller.sound

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class MusicControllerConfig(
    val files: String? = null
)

@Serializable
data class MusicPlayback(
    val file: String? = null,
    val off: Boolean = false,
    val loop: Boolean = true,
    val afterMillis: Long? = null,
    val volume: Int? = null,
) {
    inline val effectiveVolumeLevel: Double get() = (volume ?: 100) / 100.0
}

@Serializable
data class SoundControllerConfig(
    val files: String? = null
)

@Serializable
data class SoundPlayback(
    val file: String,
    val volume: Int? = null,
) {
    inline val effectiveVolumeLevel: Double get() = (volume ?: 100) / 100.0
}

data class MusicPlaybackStatus(
    val hasCurrentMusic: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPosition: Duration? = null,
    val currentLength: Duration? = null,
)
