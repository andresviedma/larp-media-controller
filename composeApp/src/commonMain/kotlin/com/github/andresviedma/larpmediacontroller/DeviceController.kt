package com.github.andresviedma.larpmediacontroller

interface DeviceController {
    suspend fun reset()
    suspend fun off()
}