package com.github.andresviedma.larpmediacontroller.projector.vlc

import com.github.andresviedma.larpmediacontroller.utils.SshConfig
import kotlinx.serialization.Serializable

@Serializable
data class VlcConfig(
    val host: String = "raspberrypi.local",
    val port: Int = 8080,
    val userName: String = "",
    val password: String = "x",
    val ssh: SshConfig,
)
