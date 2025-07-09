package com.github.andresviedma.larpmediacontroller.projector

import com.github.andresviedma.larpmediacontroller.DeviceController
import com.github.andresviedma.larpmediacontroller.sound.MusicPlayback
import com.github.andresviedma.larpmediacontroller.utils.ServerStatus

interface ProjectorController : DeviceController {
    override suspend fun reset()
    override suspend fun off()

    suspend fun shutdown()
    suspend fun reboot()
    suspend fun restartService() {}

    suspend fun play(video: RemoteVideoPlayback)
    suspend fun play(music: MusicPlayback)

    suspend fun getServerStatus(): ServerStatus
}
