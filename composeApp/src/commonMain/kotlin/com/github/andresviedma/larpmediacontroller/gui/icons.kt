package com.github.andresviedma.larpmediacontroller.gui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.Pause: ImageVector
    get() {
        if (_pause != null) {
            return _pause!!
        }
        _pause = materialIcon(name = "Filled.Pause") {
            materialPath {
                listOf(6.0f, 14.0f).forEach { x ->
                    moveTo(x, 5.0f)
                    verticalLineToRelative(14.0f)
                    horizontalLineToRelative(3.0f)
                    verticalLineToRelative(-14.0f)
                    horizontalLineToRelative(-3.0f)
                    close()
                }
            }
        }
        return _pause!!
    }

private var _pause: ImageVector? = null

val Icons.Filled.Stop: ImageVector
    get() {
        if (_stop != null) {
            return _stop!!
        }
        _stop = materialIcon(name = "Filled.Stop") {
            materialPath {
                moveTo(6.0f, 5.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(-14.0f)
                horizontalLineToRelative(-11.0f)
                close()
            }
        }
        return _stop!!
    }

private var _stop: ImageVector? = null
