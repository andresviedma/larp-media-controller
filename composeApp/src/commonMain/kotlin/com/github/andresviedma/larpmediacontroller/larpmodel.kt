package com.github.andresviedma.larpmediacontroller

import com.github.andresviedma.larpmediacontroller.lights.LightFlow
import com.github.andresviedma.larpmediacontroller.lights.LightsConfig
import com.github.andresviedma.larpmediacontroller.projector.ProjectorMediaConfig
import com.github.andresviedma.larpmediacontroller.projector.RemoteVideoPlayback
import com.github.andresviedma.larpmediacontroller.sound.MusicControllerConfig
import com.github.andresviedma.larpmediacontroller.sound.MusicPlayback
import com.github.andresviedma.larpmediacontroller.sound.SoundControllerConfig
import com.github.andresviedma.larpmediacontroller.sound.SoundPlayback
import kotlinx.serialization.Serializable

@Serializable
data class Larp(
    val metadata: LarpMetadata = LarpMetadata(),
    val devices: LarpDevicesConfig = LarpDevicesConfig(),
    val presets: Map<String, DevicesSettings> = emptyMap(),
    val scenes: List<LarpScene> = emptyList(),
) {
    inline val numberOfScenes: Int get() = scenes.size
    inline val hasScenes: Boolean get() = scenes.isNotEmpty()

    fun getAllMusicPlaybacks(): List<MusicPlayback> =
        presets.values.mapNotNull { it.music } +
            scenes.mapNotNull { it.settings?.music } +
            scenes.flatMap { it.actions.values.mapNotNull { action -> action.settings?.music } }

    fun getAllVideoPlaybacks(): List<RemoteVideoPlayback> =
        presets.values.mapNotNull { it.remoteVideo } +
                scenes.mapNotNull { it.settings?.remoteVideo } +
                scenes.flatMap { it.actions.values.mapNotNull { action -> action.settings?.remoteVideo } }
}

@Serializable
data class LarpMetadata(
    val name: String = "-",
)

@Serializable
data class LarpDevicesConfig(
    val yeelight: LightsConfig? = null,
    val music: MusicControllerConfig? = null,
    val sound: SoundControllerConfig? = null,
    val projector: ProjectorMediaConfig? = null,
) {
    fun overridenWith(overrides: LarpDevicesConfig?) = LarpDevicesConfig(
        yeelight = overrides?.yeelight ?: yeelight,
        music = overrides?.music ?: music,
        sound = overrides?.sound ?: sound,
        projector = overrides?.projector ?: projector,
    )
}

@Serializable
data class DevicesSettings(
    val preset: String? = null,
    val lightcolors: Map<String, Int>? = null,
    val lightwhites: Map<String, Int>? = null, // white temperature, bright will always be 100%
    val lightsoff: List<String>? = null,
    val lightflows: Map<String, LightFlow>? = null,
    val music: MusicPlayback? = null,
    val remoteVideo: RemoteVideoPlayback? = null,
    val remoteMusic: MusicPlayback? = null,
    val sound: SoundPlayback? = null,
    val remoteSound: SoundPlayback? = null,
    val delayed: Map<String, DelayedSettings>? = null
) {
    val effectiveLightColors: Map<String, Int>? get() =
        lightcolors?.minus(lightsoff?.toSet().orEmpty())?.minus(lightwhites?.keys.orEmpty())

    fun overridenWith(overrides: DevicesSettings) = DevicesSettings(
        preset = overrides.preset ?: preset,
        lightcolors = overrides.lightcolors ?: lightcolors,
        lightwhites = overrides.lightwhites ?: lightwhites,
        lightsoff = overrides.lightsoff ?: lightsoff,
        lightflows = overrides.lightflows ?: lightflows,
        music = overrides.music ?: music,
        remoteVideo = overrides.remoteVideo ?: remoteVideo,
        remoteMusic = overrides.remoteMusic ?: remoteMusic,
        sound = overrides.sound ?: sound,
        remoteSound = overrides.remoteSound ?: remoteSound,
        delayed = overrides.delayed ?: delayed,
    )
}

@Serializable
data class DelayedSettings(
    val afterMillis: Long,
    val settings: DevicesSettings,
)

@Serializable
data class DeviceAction(
    val text: String,
    val settings: DevicesSettings? = null,

    // These 2 should not be needed
    val sound: SoundPlayback? = null,
    val remoteSound: SoundPlayback? = null,
)

@Serializable
data class LarpScene(
    val number: String = "0",
    val title: String = "",
    val description: String = "",
    val settings: DevicesSettings? = null,
    val actions: Map<String, DeviceAction> = emptyMap(),
)

data class ScenePosition(
    val number: Int, // 1-based
    val count: Int,
) {
    inline val hasNext: Boolean get() = (number < count)
    inline val hasPrevious: Boolean get() = (number > 1)

    fun next(): ScenePosition =
        if (hasNext) ScenePosition(number + 1, count) else this

    fun previous(): ScenePosition =
        if (hasPrevious) ScenePosition(number - 1, count) else this

    fun withValidPosition(position: Int): ScenePosition =
        ScenePosition(position.coerceIn(1..count), count)
}
