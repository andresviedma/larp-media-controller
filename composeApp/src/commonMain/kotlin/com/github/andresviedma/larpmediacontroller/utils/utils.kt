package com.github.andresviedma.larpmediacontroller.utils

import io.github.oshai.kotlinlogging.KLogger

inline fun <T> KLogger.runLoggingError(block: () -> T): T? {
    try {
        return block()
    } catch (exception: Throwable) {
        error(exception) { exception.message }
        return null
    }
}
