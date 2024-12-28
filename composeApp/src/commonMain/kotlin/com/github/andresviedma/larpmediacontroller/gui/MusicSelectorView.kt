package com.github.andresviedma.larpmediacontroller.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.andresviedma.larpmediacontroller.LarpController
import com.github.andresviedma.larpmediacontroller.asyncLauncher
import korlibs.io.async.launch

@Composable
fun MusicSelectorDialog(larpController: LarpController, shouldShowDialog: MutableState<Boolean>) {
    Dialog(
        onDismissRequest = { shouldShowDialog.value = false },
        properties = DialogProperties()
    ) {
        Surface {
            MusicSelectorView(larpController)
        }
    }
}

@Composable
fun MusicSelectorView(larpController: LarpController) {
    Column(modifier = Modifier.padding(10.dp)) {
        Row {
            Text(
                text = "Stop",
                textAlign = TextAlign.Right,
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { larpController.musicController.reset() }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
        Divider()
        Column(modifier = Modifier.padding(0.dp).verticalScroll(rememberScrollState())) {
            larpController.larp.getAllMusicPlaybacks()
                .filter { it.file != null }
                .map { it.file!!.substringBeforeLast('.') to it }
                .sortedBy { (fileName, _) -> fileName }
                .forEach { (fileName, playback) ->
                    Text(
                        text = fileName,
                        modifier = Modifier.clickable(
                            onClick = {
                                asyncLauncher.launch { larpController.musicController.play(playback) }
                            }
                        ).padding(10.dp).fillMaxWidth()
                    )
                    Divider()
                }
        }
    }
}
