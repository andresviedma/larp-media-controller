package com.github.andresviedma.larpmediacontroller

import com.github.andresviedma.larpmediacontroller.gui.getPlatform
import java.io.File

class LarpCatalog {
    fun getAvailableLarps(): List<String> {
        val vfs = getPlatform().vfs
        return listOf(
            "Usr/Larps",
            "Usr",
            "",
            "Downloads/rol",
            "Downloads",
        ).flatMap { baseDir ->
            val file = File(vfs[baseDir].absolutePath)
            file.listFiles().orEmpty().flatMap { dir ->
                dir.takeIf { File(it, "larp.yaml").exists() }?.absolutePath

                // (x / larp)
                dir.listFiles().orEmpty().mapNotNull { subdir ->
                    val larpSubdir = File(subdir, "larp")
                    larpSubdir.takeIf { File(it, "larp.yaml").exists() }?.absolutePath
                }
            }
        } + listOf("test") // In resources, for easier testing
    }
}
