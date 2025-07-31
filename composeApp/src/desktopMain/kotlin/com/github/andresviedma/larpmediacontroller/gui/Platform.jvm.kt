package com.github.andresviedma.larpmediacontroller.gui

import com.github.andresviedma.larpmediacontroller.utils.MacOsVolumeController
import korlibs.io.file.VfsFile
import korlibs.io.file.std.LocalVfs

class JVMPlatform: Platform {
    override val name: String = "java"
    override val vfs: VfsFile = LocalVfs[System.getProperty("user.home")]
}

actual fun getPlatform(): Platform = JVMPlatform()

actual suspend fun increaseSystemVolume() {
    MacOsVolumeController.increaseVolume()
}

actual suspend fun decreaseSystemVolume() {
    MacOsVolumeController.decreaseVolume()
}
