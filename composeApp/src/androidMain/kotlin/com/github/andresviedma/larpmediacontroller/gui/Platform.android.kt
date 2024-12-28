package com.github.andresviedma.larpmediacontroller.gui

import android.os.Environment
import korlibs.io.file.std.localVfs

class AndroidPlatform : Platform {
    override val name: String = "android"
    override val vfs = localVfs(Environment.getExternalStorageDirectory())
}

actual fun getPlatform(): Platform = AndroidPlatform()
