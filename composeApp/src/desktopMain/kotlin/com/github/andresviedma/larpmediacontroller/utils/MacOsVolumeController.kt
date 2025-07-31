package com.github.andresviedma.larpmediacontroller.utils

object MacOsVolumeController {
    fun getVolume(): Int =
        runOsaScript("output volume of (get volume settings)").toInt()

    fun increaseVolume() =
        changeVolume { it + 10 }

    fun decreaseVolume() =
        changeVolume { it - 10 }

    private fun changeVolume(change: (Int) -> Int) {
        val current = getVolume()
        val newVolume = change(current).coerceIn(0..100)
        if (current != newVolume) {
            runOsaScript("set volume output volume $newVolume")
        }
    }

    private fun runOsaScript(command: String): String {
        val process = ProcessBuilder("osascript", "-e", command).start()
        val result = process.waitFor()

        val output = process.inputReader().readLines().firstOrNull().orEmpty().trim()
        if (result != 0) {
            val errorOutput = process.errorReader().readText()
            error("****** ERROR $result: -- $errorOutput")
        }
        return output
    }
}
