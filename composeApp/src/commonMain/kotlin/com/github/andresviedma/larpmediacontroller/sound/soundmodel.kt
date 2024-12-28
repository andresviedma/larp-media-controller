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
)

@Serializable
data class SoundControllerConfig(
    val files: String? = null
)

@Serializable
data class SoundPlayback(
    val file: String,
)

data class MusicPlaybackStatus(
    val hasCurrentMusic: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPosition: Duration? = null,
    val currentLength: Duration? = null,
)
