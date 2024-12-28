package com.github.andresviedma.larpmediacontroller.lights

import kotlinx.serialization.Serializable

@Serializable
data class Bulb(
    val id: String,
    val name: String,
    val deviceId: String,
)

@Serializable
data class LightsConfig(
    val bulbs: List<Bulb> = emptyList(),
)

@Serializable
data class LightFlow(
    val durationMillis: Long = 1000,
    val colors: List<Int> = emptyList()
)