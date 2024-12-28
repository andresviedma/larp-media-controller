package com.github.andresviedma.larpmediacontroller

import io.github.oshai.kotlinlogging.KLogger

inline fun <T> KLogger.runLoggingError(block: () -> T) {
    try {
        block()
    } catch (exception: Throwable) {
        error { exception.message }
    }
}
