package com.github.andresviedma.larpmediacontroller.projector

import com.github.andresviedma.larpmediacontroller.utils.SshConfig
import kotlinx.serialization.Serializable

@Serializable
data class ProjectorMediaConfig(
    val ssh: SshConfig,
    val defaultSoundOutput: ProjectorSoundOutput = ProjectorSoundOutput.HDMI,
    val musicFiles: String? = null,
    val videoFiles: String? = null,
    val disabled: Boolean = true,
    val vlc: VlcConfig? = null, // if not null, VLC will be used instead of OMX player
) {
    inline val mutedOutputIdentifier: String get() =
        defaultSoundOutput.opposite.commandIdentifier

    inline val defaultOutputIdentifier: String get() =
        defaultSoundOutput.commandIdentifier
}

@Serializable
data class VlcConfig(
    val port: Int = 8080,
    val userName: String = "",
    val password: String = "x",
)

@Serializable
enum class ProjectorSoundOutput {
    HDMI, LOCAL; // also both, alsa

    inline val commandIdentifier: String get() = name.lowercase()

    inline val opposite: ProjectorSoundOutput get() =
        if (this == HDMI) LOCAL else HDMI
}

@Serializable
data class MusicControllerConfig(
    val files: String? = null
)

@Serializable
data class RemoteVideoPlayback(
    val file: String? = null,
    val silent: Boolean = false,
    val loop: Boolean = true,
    val off: Boolean = false,
)
