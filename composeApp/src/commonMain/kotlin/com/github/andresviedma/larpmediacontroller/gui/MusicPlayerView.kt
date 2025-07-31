package com.github.andresviedma.larpmediacontroller.gui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.andresviedma.larpmediacontroller.LarpController
import com.github.andresviedma.larpmediacontroller.asyncLauncher
import korlibs.time.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Composable
fun MusicPlayerView(larpController: LarpController) {
    var playback by remember { mutableStateOf(larpController.musicController.playbackStatus) }

    if (playbackUpdater == null) {
        playbackUpdater = asyncLauncher.launch {
            while(true) {
                delay(500)
                if (playbackSliderChangedValue == null) {
                    runCatching { playback = larpController.musicController.playbackStatus }
                }
            }
        }
    }

//     Column {
        Row(Modifier.width(200.dp)) {
            IconButton(
                onClick = {
                    asyncLauncher.launch {
                        larpController.musicController.togglePlay()
                        playback = larpController.musicController.playbackStatus
                    }
                },
                enabled = playback.hasCurrentMusic,
            ) {
                if (playback.hasCurrentMusic && playback.isPlaying) {
                    Icon(Icons.Default.Pause, contentDescription = "Play / pause")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play / pause")
                }
            }

            IconButton(
                onClick = {
                    asyncLauncher.launch {
                        larpController.musicController.stop()
                        playback = larpController.musicController.playbackStatus
                    }
                },
                enabled = playback.hasCurrentMusic,
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }

            Text(
                text = if (playback.hasCurrentMusic) {
                    "${playback.currentPosition!!.musicDescription()} / ${playback.currentLength!!.musicDescription()}"
                } else {
                    "-- / --"
                },
                modifier = Modifier.align(Alignment.CenterVertically),
            )
/*
            // Slider removed, because korge goTo did not work (not tested in android, though)
            Slider(
                valueRange = 0f..(playback.currentLength?.inWholeMilliseconds?.toFloat() ?: 1f),
                value = playback.currentPosition?.inWholeMilliseconds?.toFloat() ?: 0f,
                onValueChange = {
                    playbackSliderChangedValue =
                        it.also { println("Value: $it / ${playback.currentLength?.inWholeMilliseconds?.toFloat()}") }
                },
                onValueChangeFinished = {
                    playbackSliderChangedValue?.let { value ->
                        asyncLauncher.launch {
                            println("Goto: $value")
                            larpController.musicController.goTo(Duration.fromMilliseconds(value))
                        }
                    }
                    playbackSliderChangedValue = null
                },
                enabled = playback.hasCurrentMusic,
            )
 */
//        }
/*
        Row(Modifier.fillMaxWidth().padding(10.dp)) {
            Button(
                onClick = {
                    asyncLauncher.launch {
                        larpController.musicController.play(MusicPlayback("1. Selene/a.3. buffy1.mp3"))
                    }
                }
            ) {
                Text("Play song")
            }

            Button(
                onClick = {
                    asyncLauncher.launch {
                        larpController.musicController.goTo(Duration.fromSeconds(10))
                    }
                }
            ) {
                Text("Test")
            }
        }
 */
    }
}

private fun Duration.musicDescription(): String =
    "${this.inWholeMinutes}:${(this.seconds.toInt() % 60).toString().padStart(2, '0')}"

private var playbackSliderChangedValue: Float? = null
private var playbackUpdater: Job? = null
