package com.github.andresviedma.larpmediacontroller.lights

import com.github.andresviedma.larpmediacontroller.DeviceController
import com.github.omarmiatello.yeelight.YeelightManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LightsController(
    lightsInfo: List<Bulb>,
    private val yeelight: YeelightManager
) : DeviceController {
    private val lights: List<LightConnection> = lightsInfo.map { LightConnection(it, yeelight) }

    override suspend fun reset() = ignoreErrors {
        lights.forEach { it.setWhite(temperature = 40) }
    }

    override suspend fun off() = ignoreErrors {
        lights.forEach { it.off() }
    }

    suspend fun toggleOnOff(bulbId: String) = ignoreErrors {
        getLightConnection(bulbId)?.toggleOnOff()
    }

    suspend fun setBulbColors(lightColors: Map<String, Int>) = ignoreErrors {
        lightColors.entries.asyncProcess { (bulbId, color) ->
            getLightConnection(bulbId)?.setColor(color)
        }
    }

    suspend fun setWhites(whites: Map<String, Int>) = ignoreErrors {
        whites.entries.asyncProcess { (bulbId, temperature) ->
            getLightConnection(bulbId)?.setWhite(temperature = temperature)
        }
    }

    suspend fun setAllColors(color: Int) = ignoreErrors {
        lights.asyncProcess { it.setColor(color) }
    }

    suspend fun setBulbFlows(lightFlows: Map<String, LightFlow>) = ignoreErrors {
        lightFlows.entries.asyncProcess { (bulbId, flow) ->
            getLightConnection(bulbId)?.setFlow(flow)
        }
    }

    suspend fun offBulbs(bulbIds: Collection<String>) = ignoreErrors {
        bulbIds.forEach {
            getLightConnection(it)?.off()
        }
    }

    fun getLightConnection(bulbId: String): LightConnection? =
        lights.firstOrNull { it.bulb.id == bulbId }

    fun getAllConnections(): List<LightConnection> =
        lights

    suspend fun refresh() = ignoreErrors {
        lights.forEachIndexed { index, conn -> conn.detect(forceRefresh = (index == 0)) }
    }

    private inline fun ignoreErrors(block: () -> Unit) {
        runCatching {
            block()
        }
    }

    private suspend fun <T, C: Collection<T>> C.asyncProcess(block: suspend (T) -> Unit) {
        coroutineScope {
            map {
                async {
                    block(it)
                }
            }.awaitAll()
        }
    }
}
