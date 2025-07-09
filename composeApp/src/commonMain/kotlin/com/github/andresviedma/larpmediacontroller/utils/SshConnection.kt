package com.github.andresviedma.larpmediacontroller.utils

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class SshConnection(
    private val username: String,
    private val password: String,
    private val host: String,
    private val port: Int
) {
    constructor(config: SshConfig) : this(
        username = config.userName,
        password = config.password,
        host = config.host,
        port = config.port,
    )

    private var session: Session? = null
    private var lastChannel: ChannelExec? = null
    private val logger = KotlinLogging.logger {}

    suspend fun runCommand(command: String) {
        runCommandWithOutStream(command)
    }

    private suspend fun runCommandWithOutStream(command: String, out: OutputStream = System.out) {
        withContext(Dispatchers.IO) {
            synchronized(this) {
                withChannel(out) { commandChannel ->
                    commandChannel.setCommand(command)
                    commandChannel.connect()
                    logger.info { "Sent command: $command" }
                }
            }
        }
    }

    suspend fun runCommandAndGetOutput(command: String): String {
        val out = ByteArrayOutputStream()
        runCommandWithOutStream(command, out)
        waitForLastCommand()
        return out.toString(Charsets.UTF_8)
    }

    suspend fun stopLastCommand() {
        withContext(Dispatchers.IO) {
            synchronized(this) {
                logger.runLoggingError {
                    lastChannel?.sendSignal("KILL")
                    lastChannel?.disconnect()
                }
                lastChannel = null
            }
        }
    }

    suspend fun stopAll(processExpression: String) {
        runCommand("kill \$(ps aux | grep '$processExpression' | grep -v 'grep' | awk '{print \$2}')")
    }

    suspend fun waitForLastCommand() {
        withContext(Dispatchers.IO) {
            synchronized(this) {
                lastChannel?.let { channel ->
                    while (channel.isConnected()) {
                        Thread.sleep(100)
                    }
                    lastChannel = null
                }
            }
        }
    }

    suspend fun release() {
        withContext(Dispatchers.IO) {
            synchronized(this) {
                disconnect()
            }
        }
    }

    private fun <T> withChannel(out: OutputStream = System.out, block: (ChannelExec) -> T): T {
        if (session?.isConnected == false) disconnect()
        if (session == null) connect()
        return disconnectIfFails {
            lastChannel = session!!.openChannel("exec") as ChannelExec
            lastChannel!!.outputStream = out

            block(lastChannel!!)
        }
    }

    private fun connect() {
        disconnectIfFails {
            logger.info { "Opening new SSH session" }
            session = JSch().getSession(username, host, port).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                connect()
            }
        }
    }

    private fun disconnect() {
        logger.info { "Closing SSH session" }
        runCatching { lastChannel?.disconnect() }
        runCatching { session?.disconnect() }
        lastChannel = null
        session = null
    }

    private fun <T> disconnectIfFails(block: () -> T): T =
        try {
            block()
        } catch (exception: Throwable) {
            disconnect()
            throw exception
        }
}
