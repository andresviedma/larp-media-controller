package com.github.andresviedma.larpmediacontroller.lights

import com.github.omarmiatello.yeelight.FlowColor
import com.github.omarmiatello.yeelight.FlowEndAction
import com.github.omarmiatello.yeelight.FlowSleep
import com.github.omarmiatello.yeelight.SpeedEffect
import com.github.omarmiatello.yeelight.YeelightDevice
import com.github.omarmiatello.yeelight.YeelightManager
import kotlin.time.Duration.Companion.milliseconds

class LightConnection(
    val bulb: Bulb,
    private val yeelightRoom: YeelightManager
) {
    private var yeelightDevice: YeelightDevice? = null

    suspend fun setColor(red: Int, green: Int, blue: Int) {
        if (red and green and blue == 0xff) {
            setWhite()
            return
        }

        getYeelightDevice()?.let { device ->
            val brightness = listOf(red, green, blue).maxOf { it.brightnessLevel() }
            device.setPower(isOn = true)
            device.setColorRgb(rgb(red, green, blue), SpeedEffect.smooth, 1000.milliseconds)
            device.setBrightness(brightness, SpeedEffect.sudden, 0.milliseconds)
        }
    }

    suspend fun setWhite(temperature: Int = 100, brightness: Int = 100) {
        getYeelightDevice()?.let { device ->
            device.setPower(isOn = true)
            val temperatureLevel = (temperature.coerceIn(0..100) * 48.0).toInt() + 1700
            device.setWhiteTemperature(temperatureLevel, SpeedEffect.smooth, 1000.milliseconds)
            device.setBrightness(brightness.coerceIn(1..100), SpeedEffect.sudden, 0.milliseconds)
        }
    }

    suspend fun setColor(rgb: Int) {
        rgb.decomposeRgb().let { (red, green, blue) ->
            setColor(red, green, blue)
        }
    }

    suspend fun setFlow(flow: LightFlow) {
        getYeelightDevice()?.let { device ->
            device.setPower(isOn = true)
            device.startColorFlow(
                flowTuples = flow.colors.flatMap { color ->
                    listOfNotNull(
                        FlowColor(
                            duration = flow.durationMillis.milliseconds,
                            color = maxOf(color, 1),
                            brightness = color.colorBrightness()
                        ),
                        flow.stayDurationMillis?.let { FlowSleep(it.milliseconds) },
                    )
                },
                repeat = 0, // infinite
                action = FlowEndAction.stay,
            )
        }
    }

    suspend fun toggleOnOff() {
        getYeelightDevice()?.toggle()
    }

    suspend fun off() {
        getYeelightDevice()?.setPower(isOn = false)
    }

    suspend fun on() {
        getYeelightDevice()?.setPower(isOn = true)
    }

    suspend fun detect(forceRefresh: Boolean = true) {
        yeelightDevice = null
        getYeelightDevice(forceRefresh)
    }

    fun isConnected(): Boolean =
        (yeelightDevice != null)

    private fun Int.brightnessLevel(): Int =
        maxOf(((toDouble() / 0xff) * 100).toInt(), 1)

    private fun Int.decomposeRgb(): Triple<Int, Int, Int> =
        Triple(this / 65536, (this % 65536) / 256, this % 256)

    private fun Int.colorBrightness(): Int =
        decomposeRgb().let { (red, green, blue) ->
            listOf(red, green, blue).maxOf { it.brightnessLevel() }
        }

    private fun rgb(red: Int, green: Int, blue: Int): Int =
        maxOf((red shl 16) + (green shl 8) + blue, 1)

    private suspend fun getYeelightDevice(forceRefresh: Boolean = true): YeelightDevice? =
        (yeelightDevice ?: yeelightRoom.findDeviceById(bulb.deviceId, forceRefresh = forceRefresh))
            .also { yeelightDevice = it }
}
