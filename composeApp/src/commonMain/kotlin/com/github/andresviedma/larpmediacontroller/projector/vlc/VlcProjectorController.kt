package com.github.andresviedma.larpmediacontroller.projector.vlc

import com.github.andresviedma.larpmediacontroller.projector.ProjectorController
import com.github.andresviedma.larpmediacontroller.projector.ProjectorMediaConfig
import com.github.andresviedma.larpmediacontroller.projector.RemoteVideoPlayback
import com.github.andresviedma.larpmediacontroller.sound.MusicPlayback
import com.github.andresviedma.larpmediacontroller.utils.SshConnection
import com.github.andresviedma.larpmediacontroller.utils.runLoggingError
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class VlcProjectorController(
    private val name: String,
    private val projectorMediaConfig: ProjectorMediaConfig,
) : ProjectorController {

    private val ssh = SshConnection(projectorMediaConfig.ssh).takeIf { !projectorMediaConfig.disabled }
    private val vlc = projectorMediaConfig.vlc!!.let {
        VlcApi(host = projectorMediaConfig.ssh.host, port = it.port, userName = it.userName, password = it.password)
    }
    private val playMutex = Mutex()
    private val logger = KotlinLogging.logger {}

    override suspend fun reset() {
        logger.info { "$name: Stopping video" }
        logger.runLoggingError {
            playMutex.withLock {
                vlc.clearQueue()
                val status = vlc.play(BLACK_LOOPED_IMG)
                setLoopAndMute(status, loop = true, mute = false)
            }
        }
    }

    override suspend fun off() {
        reset()
    }

    override suspend fun shutdown() {
        if (ssh == null) return
        logger.runLoggingError { ssh.runCommand("sudo shutdown now") }
    }

    override suspend fun play(video: RemoteVideoPlayback) {
        if (!video.off) {
            video.file?.let {
                logger.info { "$name: Playing video $it" }
                playFile(videoPath(it), video.loop, video.silent)
            }
        }
    }

    override suspend fun play(music: MusicPlayback) {
        if (!music.off) {
            music.file?.let {
                logger.info { "$name: Playing music $it" }
                playFile(musicPath(it), music.loop, silent = false)
            }
        }
    }

    private suspend fun playFile(path: String, loop: Boolean, silent: Boolean) {
        logger.runLoggingError {
            playMutex.withLock {
                logger.runLoggingError { vlc.clearQueue() }
                val status = vlc.play(path)

                if (loop) {
                    setLoopAndMute(status, true, silent)
                } else {
                    vlc.enqueue(BLACK_VIDEO)
                    setLoopAndMute(status, false, silent)
                }
            }
        }
    }

    private suspend fun setLoopAndMute(status: VlcStatus, loop: Boolean, mute: Boolean) {
        if (status.loop != loop) vlc.toggleLoop()
        if (status.isMuted != mute) {
            if (mute) vlc.mute() else vlc.setMaxVolume()
        }
    }

    private fun videoPath(file: String): String =
        File(projectorMediaConfig.videoFiles.orEmpty(), file).path

    private fun musicPath(file: String): String =
        File(projectorMediaConfig.musicFiles.orEmpty(), file).path

    companion object {
        private const val BLACK_LOOPED_IMG = "black.png"
        private const val BLACK_VIDEO = "black.mp4"
    }
}
