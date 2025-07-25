package com.github.andresviedma.larpmediacontroller.projector.omxplayer

import com.github.andresviedma.larpmediacontroller.projector.ProjectorController
import com.github.andresviedma.larpmediacontroller.projector.ProjectorMediaConfig
import com.github.andresviedma.larpmediacontroller.projector.RemoteVideoPlayback
import com.github.andresviedma.larpmediacontroller.utils.runLoggingError
import com.github.andresviedma.larpmediacontroller.sound.MusicPlayback
import com.github.andresviedma.larpmediacontroller.sound.SoundPlayback
import com.github.andresviedma.larpmediacontroller.utils.ServerStatus
import com.github.andresviedma.larpmediacontroller.utils.SshConnection
import com.github.andresviedma.larpmediacontroller.utils.getServerStatus
import com.github.andresviedma.larpmediacontroller.utils.reboot
import com.github.andresviedma.larpmediacontroller.utils.shutdown
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

class OmxPlayerProjectorController(
    private val name: String,
    private val projectorMediaConfig: ProjectorMediaConfig,
) : ProjectorController {

    private val ssh = SshConnection(projectorMediaConfig.ssh).takeIf { !projectorMediaConfig.disabled }
    private val logger = KotlinLogging.logger {}

    override suspend fun reset() {
        if (ssh == null) return
        logger.info { "$name: Stopping potential current command" }
        logger.runLoggingError { ssh.stopLastCommand() }
    }

    override suspend fun off() {
        if (ssh == null) return
        ssh.stopLastCommand()
        logger.info { "$name: Stopping all omxplayer processes" }
        logger.runLoggingError { ssh.stopAll("omxplayer.bin") }
        logger.info { "$name: Releasing SSH" }
        ssh.release()
    }

    override suspend fun shutdown() {
        ssh?.shutdown()
    }
    override suspend fun reboot() {
        ssh?.reboot()
    }

    override suspend fun play(video: RemoteVideoPlayback) {
        if (ssh == null) return
        logger.runLoggingError { ssh.stopLastCommand() }
        if (!video.off) {
            video.file?.let {
                logger.info { "$name: Playing video $it" }
                val path = File(projectorMediaConfig.videoFiles.orEmpty(), it).path
                runSshCommand(file = path, loop = video.loop, muted = video.silent, retry = true)
            }
        }
    }

    override suspend fun play(music: MusicPlayback) {
        if (ssh == null) return
        logger.runLoggingError { ssh.stopLastCommand() }
        if (!music.off) {
            music.file?.let {
                logger.info { "$name: Playing music $it" }
                val path = File(projectorMediaConfig.musicFiles.orEmpty(), it).path
                runSshCommand(file = path, loop = music.loop, muted = false, retry = true)
            }
        }
    }

    suspend fun play(sound: SoundPlayback) {
        if (ssh == null) return
        ssh.stopLastCommand()
        logger.info { "$name: Playing sound ${sound.file}" }
        val path = File(projectorMediaConfig.musicFiles.orEmpty(), sound.file).path
        runSshCommand(file = path, loop = false, muted = false, retry = true)
    }

    override suspend fun getServerStatus(): ServerStatus =
        ssh!!.getServerStatus()

    private suspend fun runSshCommand(file: String, loop: Boolean, muted: Boolean, retry: Boolean = false) {
        // *** Unused flags:
        // --vol n = initial volume in millibels (def 0)
        // --amp n = initial amplification (def 0)
        // --passthrough Audio passthrough
        // --hw Hw audio decoding
        // --refresh Adjust framerate / resolution to video

        try {
            val soundOutput = if (muted) {
                projectorMediaConfig.mutedOutputIdentifier
            } else {
                projectorMediaConfig.defaultOutputIdentifier
            }
            val additional = "--loop".takeIf { loop }.orEmpty()
            val cmd =  "omxplayer -o $soundOutput --no-keys $additional $file"
            ssh?.runCommand(cmd)

        } catch (exception: Exception) {
            if (retry) {
                logger.error { "Error running SSH command to play $file -- ${exception.message}" }
                runSshCommand(file, loop, muted, retry = false)
            } else {
                logger.error(exception) { "Error running SSH command to play $file" }
            }
        }
    }
}
