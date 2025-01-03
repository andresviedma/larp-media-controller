package com.github.andresviedma.larpmediacontroller.projector

import kotlinx.serialization.Serializable

@Serializable
data class ProjectorMediaConfig(
    val ssh: ProjectorSshConfig,
    val defaultSoundOutput: ProjectorSoundOutput = ProjectorSoundOutput.HDMI,
    val musicFiles: String? = null,
    val videoFiles: String? = null,
    val disabled: Boolean = true,
) {
    inline val mutedOutputIdentifier: String get() =
        defaultSoundOutput.opposite.commandIdentifier

    inline val defaultOutputIdentifier: String get() =
        defaultSoundOutput.commandIdentifier
}

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
data class ProjectorSshConfig(
    val host: String = "raspberrypi.local",
    val port: Int = 22,
    val userName: String = "pi",
    val password: String = "raspberry"
)

@Serializable
data class RemoteVideoPlayback(
    val file: String? = null,
    val silent: Boolean = false,
    val loop: Boolean = true,
    val off: Boolean = false,
)
