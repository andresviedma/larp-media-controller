package com.github.andresviedma.larpmediacontroller.projector

import com.github.andresviedma.larpmediacontroller.runLoggingError
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SshConnection(
    private val username: String,
    private val password: String,
    private val host: String,
    private val port: Int
) {
    constructor(config: ProjectorSshConfig) : this(
        username = config.userName,
        password = config.password,
        host = config.host,
        port = config.port,
    )

    private var session: Session? = null
    private var lastChannel: ChannelExec? = null
    private val logger = KotlinLogging.logger {}

    suspend fun runCommand(command: String) {
        withContext(Dispatchers.IO) {
            synchronized(this) {
                withChannel { commandChannel ->
                    commandChannel.setCommand(command)
                    commandChannel.connect()
                    logger.info { "Sent command: $command" }
                }
            }
        }
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
                if (lastChannel != null) {
                    while (lastChannel!!.isConnected()) {
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

    private fun <T> withChannel(block: (ChannelExec) -> T): T {
        if (session?.isConnected == false) disconnect()
        if (session == null) connect()
        return disconnectIfFails {
            lastChannel = session!!.openChannel("exec") as ChannelExec
            lastChannel!!.outputStream = System.out

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
