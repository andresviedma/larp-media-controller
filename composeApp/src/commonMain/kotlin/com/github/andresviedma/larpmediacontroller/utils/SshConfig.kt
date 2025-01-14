package com.github.andresviedma.larpmediacontroller.utils

import kotlinx.serialization.Serializable

@Serializable
data class SshConfig(
    val host: String = "raspberrypi.local",
    val port: Int = 22,
    val userName: String = "pi",
    val password: String = "raspberry"
)

