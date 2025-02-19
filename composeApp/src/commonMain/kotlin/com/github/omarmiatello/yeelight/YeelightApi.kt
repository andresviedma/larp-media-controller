package com.github.omarmiatello.yeelight

import com.github.omarmiatello.yeelight.YeelightScene.Companion.toJsonPrimitive
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val json = Json {
    encodeDefaults = true
    serializersModule = SerializersModule {
        contextual(String.serializer())
        contextual(Int.serializer())
        contextual(Long.serializer())
    }
}

private fun colorFlowBuilder(
    flowTuples: List<FlowTuple>,
    repeat: Int = 1,
    action: FlowEndAction = FlowEndAction.recover
) = primitiveListOf(repeat * flowTuples.size, action.id, flowTuples.joinToString(","))

// Docs: http://www.yeelight.com/download/Yeelight_Inter-Operation_Spec.pdf
object YeelightApi {
    fun getProperties(vararg propertiesNames: String) = YeelightCmd("get_prop", primitiveListOf(*propertiesNames))

    fun setCurrentAsDefault() = YeelightCmd("set_default")

    fun setPower(
        isOn: Boolean = true,
        effect: SpeedEffect = SpeedEffect.smooth,
        duration: Duration = 500.milliseconds
    ) = YeelightCmd("set_power", primitiveListOf(if (isOn) "on" else "off", effect.id, duration.inWholeMilliseconds))

    fun toggle() = YeelightCmd("toggle")

    /**
     * brightness: 1 - 100
     */
    fun setBrightness(
        brightness: Int,
        effect: SpeedEffect = SpeedEffect.smooth,
        duration: Duration = 500.milliseconds
    ) = YeelightCmd("set_bright", primitiveListOf(brightness.coerceIn(1..100), effect.id, duration.inWholeMilliseconds))

    fun startColorFlowRaw(
        count: Int,
        action: FlowEndAction,
        flowExpression: String
    ) = YeelightCmd("start_cf", primitiveListOf(count, action.id, flowExpression))

    fun startColorFlow(
        flowTuples: List<FlowTuple>,
        repeat: Int = 1,
        action: FlowEndAction = FlowEndAction.recover
    ) = YeelightCmd("start_cf", colorFlowBuilder(flowTuples, repeat, action))

    fun stopColorFlow() = YeelightCmd("stop_cf")

    fun setScene(scene: YeelightScene) = YeelightCmd("set_scene", primitiveListOf(scene.name) + scene.params)

    fun cronAdd(
        cron: YeelightCron
    ) = YeelightCmd("cron_add", primitiveListOf(cron.type, cron.duration.coerceAtLeast(1.minutes).inWholeMinutes.toInt()))

    fun cronGet() = YeelightCmd("cron_get")
    fun cronDel() = YeelightCmd("cron_del")

    /**
     * whiteTemperature: 1700 ~ 6500
     */
    fun setWhiteTemperature(
        whiteTemperature: Int,
        effect: SpeedEffect = SpeedEffect.smooth,
        duration: Duration = 500.milliseconds
    ) = YeelightCmd(
        "set_ct_abx",
        primitiveListOf(whiteTemperature.coerceIn(1700..6500), effect.id, duration.inWholeMilliseconds)
    )

    /**
     * color: 0x000000 - 0xFFFFFF
     */
    fun setColorRgb(
        color: Int,
        effect: SpeedEffect = SpeedEffect.smooth,
        duration: Duration = 500.milliseconds
    ) = YeelightCmd("set_rgb", primitiveListOf(color.coerceIn(0..0xffffff), effect.id, duration.inWholeMilliseconds))

    /**
     * hue: 0 - 359
     * sat: 0 - 100
     * */
    fun setColorHsv(
        hue: Int,
        sat: Int,
        effect: SpeedEffect = SpeedEffect.smooth,
        duration: Duration = 500.milliseconds
    ) = YeelightCmd(
        "set_hsv",
        primitiveListOf(hue.coerceIn(0..359), sat.coerceIn(0..100), effect.id, duration.inWholeMilliseconds)
    )
}


// All API

@Serializable
data class YeelightCmd(val method: String, val params: List<JsonPrimitive> = emptyList()) {
    val id = 1

    @Transient
    val realCommand = try {
        json.encodeToString(this)
    } catch (e: Exception) {
        println("encode fail: $id --> $this")
        throw e
    }
}

enum class SpeedEffect {
    sudden, smooth;

    val id = name
}

enum class FlowEndAction(val id: Int) { recover(0), stay(1), off(2) }
private enum class FlowMode(val id: Int) { colorRgb(1), colorTemperature(2), sleep(7) }


// Scene

sealed class YeelightScene(val name: String, val params: List<JsonPrimitive>) {
    constructor(name: String, vararg params: Any) : this(name, params.map { it.toJsonPrimitive() })

    companion object {
        private fun Any.toJsonPrimitive(): JsonPrimitive = when(this) {
            is Int -> JsonPrimitive(this)
            is Long -> JsonPrimitive(this)
            is String -> JsonPrimitive(this)
            else -> JsonPrimitive(this.toString())
        }
    }
}

private fun primitiveListOf(vararg values: Any): List<JsonPrimitive> = values.map {
    when(it) {
        is Int -> JsonPrimitive(it)
        is Long -> JsonPrimitive(it)
        is String -> JsonPrimitive(it)
        else -> JsonPrimitive(it.toString())
    }
}

/**
 * hue: 0 - 359
 * sat: 0 - 100
 * */
class SceneColorHsv(
    hue: Int,
    sat: Int = 100,
    brightness: Int = 100
) : YeelightScene("hsv", primitiveListOf(hue, sat, brightness))

/**
 * color: 0x000000 - 0xFFFFFF
 */
class SceneColorRgb(
    color: Int,
    brightness: Int = 100
) : YeelightScene("ct", primitiveListOf(color, brightness))

class SceneColorFlow(
    flowTuples: List<FlowTuple>,
    repeat: Int = 1,
    action: FlowEndAction = FlowEndAction.recover
) : YeelightScene("cf", colorFlowBuilder(flowTuples, repeat, action))

class SceneAutoDelayOff(
    brightness: Int = 100,
    duration: Duration = 1.minutes
) : YeelightScene("auto_delay_off", primitiveListOf(brightness, duration.coerceAtLeast(1.minutes).inWholeMinutes.toInt()))

// Cron
sealed class YeelightCron(val type: Int, val duration: Duration)

class CronPowerOff(
    duration: Duration
) : YeelightCron(0, duration)

// Color Flow

sealed class FlowTuple(
    val duration: Duration,
    private val mode: FlowMode,
    val value: Int,
    val brightness: Int
) {
    override fun toString() = "${duration.inWholeMilliseconds},${mode.id},$value,$brightness"
}

class FlowColor(
    color: Int,
    brightness: Int = 100,
    duration: Duration = 1.seconds,
) : FlowTuple(
    duration = duration.coerceAtLeast(50.milliseconds),
    mode = FlowMode.colorRgb,
    value = color.coerceIn(0x000000..0xFFFFFF),
    brightness = brightness.coerceIn(-1..100)
)

class FlowSleep(
    duration: Duration = 1.seconds
) : FlowTuple(
    duration = duration.coerceAtLeast(50.milliseconds),
    mode = FlowMode.sleep,
    value = 0,
    brightness = 0
)

class FlowWhiteTemperature(
    whiteTemperature: Int,
    brightness: Int = 100,
    duration: Duration = 1.seconds,
) : FlowTuple(
    duration = duration.coerceAtLeast(50.milliseconds),
    mode = FlowMode.colorTemperature,
    value = whiteTemperature.coerceIn(1700..6500),
    brightness = brightness.coerceIn(-1..100)
)

