package com.github.andresviedma.larpmediacontroller.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.andresviedma.larpmediacontroller.LarpController
import com.github.andresviedma.larpmediacontroller.asyncLauncher
import kotlinx.coroutines.launch

@Composable
fun BulbsDialog(larpController: LarpController, shouldShowDialog: MutableState<Boolean>) {
    val version = remember { mutableStateOf(0) }
    Dialog(
        onDismissRequest = { shouldShowDialog.value = false },
        properties = DialogProperties()
    ) {
        Surface {
            BulbsView(larpController, version)
        }
    }
}

@Composable
fun BulbsView(larpController: LarpController, version: MutableState<Int>) {
    val bulbs = larpController.larp.devices.yeelight?.bulbs.orEmpty()
    var refreshing by remember { mutableStateOf(false) }
    Column(Modifier.padding(20.dp)) {
        bulbs.forEach { bulb ->
            val connection = larpController.lightsController.getLightConnection(bulb.id)
            val enabled = (connection?.isConnected() ?: false)

            val padding = Modifier.padding(5.dp)
            Text(
                text = bulb.name,
                color = if (enabled) Color.Black else Color.LightGray,
                modifier = padding
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(
                    "red" to 0xff0000,
                    "green" to 0x00ff00,
                    "blue" to 0x0000ff,
                    "yellow" to 0xffa800,
                    "magenta" to 0xff00ff,
                    "cyan" to 0x00ffff,
                    "orange" to 0xff800d,
                    "white" to 0xffffff,
                    "read" to 0xffffa0
                ).forEach { (_, rgb) ->
                    Button(
                        onClick = {
                            asyncLauncher.launch {
                                larpController.lightsController.getLightConnection(bulb.id)?.setColor(rgb)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(rgb + 0xff000000),
                            disabledBackgroundColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(2.dp),
                        contentPadding = PaddingValues(),
                        enabled = enabled,
                        modifier = padding.height(20.dp).width(20.dp)
                    ) {}
                }
                IconButton(
                    onClick = {
                        asyncLauncher.launch {
                            larpController.lightsController.getLightConnection(bulb.id)?.toggleOnOff()
                        }
                    },
                    enabled = enabled,
                    modifier = padding
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Toggle on/off")
                }
            }
        }

        Button(
            onClick = {
                asyncLauncher.launch {
                    refreshing = true
                    larpController.lightsController.refresh()
                    refreshing = false
                    version.value++ // force refresh
                }
            },
            enabled = !refreshing
        ) {
            Text("Refresh bulbs")
        }
    }
}
