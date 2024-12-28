package com.github.andresviedma.larpmediacontroller.gui

import korlibs.io.file.VfsFile

interface Platform {
    val name: String
    val vfs: VfsFile
}

expect fun getPlatform(): Platform
