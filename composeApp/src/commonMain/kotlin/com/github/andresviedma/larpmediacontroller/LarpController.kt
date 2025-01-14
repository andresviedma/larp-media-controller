package com.github.andresviedma.larpmediacontroller

import com.github.andresviedma.larpmediacontroller.gui.getPlatform
import com.github.andresviedma.larpmediacontroller.lights.LightsController
import com.github.andresviedma.larpmediacontroller.projector.ProjectorController
import com.github.andresviedma.larpmediacontroller.projector.omxplayer.OmxPlayerProjectorController
import com.github.andresviedma.larpmediacontroller.sound.MusicController
import com.github.andresviedma.larpmediacontroller.sound.MusicControllerConfig
import com.github.andresviedma.larpmediacontroller.sound.SoundController
import com.github.andresviedma.larpmediacontroller.sound.SoundControllerConfig
import com.github.omarmiatello.yeelight.YeelightManager
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import larp_media_controller.composeapp.generated.resources.Res
import net.mamoe.yamlkt.Yaml
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class LarpController private constructor(
    private val larpFileName: String,
) {
    lateinit var larp: Larp
    lateinit var baseLarpDir: String
    private var loaded = false

    lateinit var lightsController: LightsController
    lateinit var musicController: MusicController
    lateinit var soundController: SoundController
    var remoteVideoController: ProjectorController? = null
    var remoteMusicController: ProjectorController? = null
    var remoteSoundController: ProjectorController? = null

    fun getFirstScenePosition(): ScenePosition =
        ScenePosition(1, larp.numberOfScenes).takeIf { larp.hasScenes }
            ?: throw RuntimeException("Larp with no scenes")

    fun getSceneInfo(position: ScenePosition): LarpScene =
        if (larp.hasScenes) larp.scenes[position.number - 1] else throw RuntimeException("Invalid scene position")

    suspend fun runSceneSettings(position: ScenePosition) {
        runDevicesSettings(getSceneInfo(position).settings)
    }

    suspend fun runDevicesSettings(settings: DevicesSettings?) {
        val actualSettings = settings.withFilledPresets()

        coroutineScope {
            (
                listOfNotNull(
                    actualSettings?.music?.let { async { musicController.play(it) } },
                    async {
                        actualSettings?.lightsoff?.let { lightsController.offBulbs(it) }
                        actualSettings?.effectiveLightColors?.let { lightsController.setBulbColors(it) }
                        actualSettings?.lightwhites?.let { lightsController.setWhites(it) }
                        actualSettings?.lightflows?.let { lightsController.setBulbFlows(it) }
                    },
                    actualSettings?.sound?.let { async { soundController.play(it) } },
                    actualSettings?.remoteVideo?.let { async { remoteVideoController?.play(it) } },
                    actualSettings?.remoteMusic?.let { async { remoteVideoController?.play(it) } },

                ) + actualSettings?.delayed?.values.orEmpty().map { delayed ->
                    async {
                        delay(delayed.afterMillis)
                        runDevicesSettings(delayed.settings)
                    }
                }
            ).awaitAll()
        }
    }

    suspend fun runPresetSettings(preset: String) {
        runDevicesSettings(getPresetSettings(preset))
    }

    suspend fun runSceneAction(position: ScenePosition, action: String) {
        getSceneInfo(position).actions[action]?.let { actionDef ->
            runDevicesSettings(actionDef.settings)
            actionDef.sound?.let { soundController.play(it) }
        }
    }

    fun getPresetSettings(preset: String): DevicesSettings? =
        larp.presets[preset]?.withFilledPresets()

    suspend fun reset() {
        coroutineScope {
            awaitAll(
                async { musicController.reset() },
                async { lightsController.reset() },
                async { remoteVideoController?.reset() },
                async { remoteMusicController?.reset() },
                async { remoteSoundController?.reset() },
            )
        }
    }

    suspend fun endOfScene() {
        coroutineScope {
            awaitAll(
                async {
                    lightsController.off()
                    delay(2000)
                    lightsController.reset()
                },
                async { musicController.reset() },
                async { remoteVideoController?.reset() },
                async { remoteMusicController?.reset() },
                async { remoteSoundController?.reset() },
            )
        }
    }

    suspend fun off() {
        coroutineScope {
            awaitAll(
                async {
                    musicController.off()
                    lightsController.off()
                },
                async {
                    runCatching { remoteMusicController?.reset() }
                    runCatching { remoteSoundController?.reset() }
                    runCatching { remoteVideoController?.off() }
                }
            )
        }
    }

    private fun DevicesSettings?.withFilledPresets(): DevicesSettings? = when {
        this == null -> null
        preset == null -> this
        else -> {
            val baseSettings = larp.presets[preset]?.withFilledPresets()
            baseSettings?.overridenWith(this.copy(preset = null)) ?: this
        }
    }

    private suspend fun load() {
        if (!loaded) {
            loadLarpInfo()

            val yeelightManager = YeelightManager(enableLog = true)
            lightsController = LightsController(
                lightsInfo = larp.devices.yeelight?.bulbs.orEmpty(),
                yeelight = yeelightManager
            )
            lightsController.refresh()
            musicController = MusicController(
                baseLarpDir = baseLarpDir,
                config = MusicControllerConfig(
                    files = larp.devices.music?.files
                )
            )
            soundController = SoundController(
                baseLarpDir = baseLarpDir,
                config = SoundControllerConfig(
                    files = larp.devices.sound?.files
                )
            )
            val projector = larp.devices.projector?.takeIf { !it.disabled }
            remoteVideoController = projector?.let { OmxPlayerProjectorController("video", it) }
            remoteMusicController = projector?.let { OmxPlayerProjectorController("music", it) }
            remoteSoundController = projector?.let { OmxPlayerProjectorController("sound", it) }

            loaded = true
        }
    }

    private suspend fun loadLarpInfo() {
        val (baseDir, yaml) = if (larpFileName.contains('/')) {
            getPlatform().vfs[larpFileName].let {
                it.absolutePath to it["larp.yaml"].readBytes().toString(Charsets.UTF_8)
            }
        } else {
            "" to Res.readBytes("files/$larpFileName.yaml").toString(Charsets.UTF_8)
        }
        val larp0 = Yaml.decodeFromString(Larp.serializer(), yaml)
        val devicesEnv = runCatching {
            val yamlEnv = Res.readBytes("files/$larpFileName-${getPlatform().name}.yaml").toString(Charsets.UTF_8)
            Yaml.decodeFromString(LarpDevicesConfig.serializer(), yamlEnv)
        }.getOrNull()
        larp = larp0.copy(devices = larp0.devices.overridenWith(devicesEnv))
        baseLarpDir = baseDir
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        suspend fun loadLarp(name: String): LarpController =
            LarpController(name).also {
                logger.info { "Loading LARP $name" }
                it.load()
                logger.info { "Loading LARP $name DONE" }
            }
    }
}
