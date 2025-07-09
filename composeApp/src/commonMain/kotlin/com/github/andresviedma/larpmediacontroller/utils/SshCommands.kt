package com.github.andresviedma.larpmediacontroller.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import korlibs.memory.hasBitSet
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

suspend fun SshConnection.shutdown() =
    logger.runLoggingError { runCommand("sudo shutdown now") }

suspend fun SshConnection.reboot() =
    logger.runLoggingError { runCommand("sudo reboot now") }

suspend fun SshConnection.getPowerLevel(): BigDecimal? =
    logger.runLoggingError {
        runCommandAndGetOutput("vcgencmd measure_volts core")
            .substringAfter("volt=").substringBefore("V")
            .ifEmpty { "0.0" }
            .toBigDecimal()
    }

@OptIn(ExperimentalStdlibApi::class)
suspend fun SshConnection.getPowerFlags(): Pair<Boolean, Boolean>? =
    logger.runLoggingError {
        runCommandAndGetOutput("vcgencmd get_throttled")
            .substringAfter("throttled=0x")
            .trim()
            .ifEmpty { "0" }
            .hexToInt()
            .let {
                // Current voltage inssuficient / Sessino has had insuffient voltage
                it.hasBitSet(0) to it.hasBitSet(16)
            }
    }

data class ServerStatus(
    val serverConnected: Boolean,
    val currentPowerVolts: BigDecimal?,
    val powerIsEnough: Boolean,
    val powerAlwaysEnough: Boolean,
    val serviceConnected: Boolean,
) {
    companion object {
        fun notConnected(): ServerStatus = ServerStatus(
            serverConnected = false,
            currentPowerVolts = null,
            powerIsEnough = false,
            powerAlwaysEnough = false,
            serviceConnected = false,
        )
    }
}

suspend fun SshConnection.getServerStatus(): ServerStatus {
    val powerLevel = getPowerLevel()
    val (insufficientVoltage, insufficientVoltageInSession) = getPowerFlags() ?: (false to false)
    return ServerStatus(
        serverConnected = powerLevel != null,
        currentPowerVolts = powerLevel,
        powerIsEnough = !insufficientVoltage,
        powerAlwaysEnough = !insufficientVoltageInSession,
        serviceConnected = false, // this will depend of the concrete service we are checking
    )
}
