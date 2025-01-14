package com.github.andresviedma.larpmediacontroller.projector

import com.github.andresviedma.larpmediacontroller.DeviceController
import com.github.andresviedma.larpmediacontroller.sound.MusicPlayback

interface ProjectorController : DeviceController {
    override suspend fun reset()

    override suspend fun off()

    suspend fun shutdown()

    suspend fun play(video: RemoteVideoPlayback)
    suspend fun play(music: MusicPlayback)
}
