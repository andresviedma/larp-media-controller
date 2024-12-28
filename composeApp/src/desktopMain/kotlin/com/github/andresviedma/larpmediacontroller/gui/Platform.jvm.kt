package com.github.andresviedma.larpmediacontroller.gui

import korlibs.io.file.VfsFile
import korlibs.io.file.std.LocalVfs

class JVMPlatform: Platform {
    override val name: String = "java"
    override val vfs: VfsFile = LocalVfs[System.getProperty("user.home")]
}

actual fun getPlatform(): Platform = JVMPlatform()
