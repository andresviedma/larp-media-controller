package com.github.andresviedma.larpmediacontroller.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.andresviedma.larpmediacontroller.LarpController
import com.github.andresviedma.larpmediacontroller.asyncLauncher
import com.github.andresviedma.larpmediacontroller.projector.ProjectorController
import com.github.andresviedma.larpmediacontroller.projector.RemoteVideoPlayback
import com.github.andresviedma.larpmediacontroller.utils.ServerStatus
import kotlinx.coroutines.launch

@Composable
fun ProjectorControlDialog(larpController: LarpController, shouldShowDialog: MutableState<Boolean>) {
    val projectorController = larpController.remoteVideoController ?: return
    val serverStatus = remember { mutableStateOf<ServerStatus?>(null) }

    Dialog(
        onDismissRequest = { shouldShowDialog.value = false },
        properties = DialogProperties()
    ) {
        Surface {
            val currentServerStatus = serverStatus.value
            if (currentServerStatus == null) {
                serverStatus.refresh(projectorController)
                Text("Loading...", Modifier.padding(30.dp))
            } else {
                ProjectorControlView(projectorController, serverStatus)
            }
        }
    }
}

private fun MutableState<ServerStatus?>.refresh(projectorController: ProjectorController) {
    value = null
    asyncLauncher.launch {
        try {
            value = projectorController.getServerStatus()
        } catch (_: Throwable) {
            value = ServerStatus.notConnected()
        }
    }
}

@Composable
fun ProjectorControlView(projectorController: ProjectorController, serverStatusVar: MutableState<ServerStatus?>) {
    val serverStatus = serverStatusVar.value!!
    Column(modifier = Modifier.padding(10.dp)) {
        FieldInfo("Server connected",  serverStatus.serverConnected)
        if (serverStatus.serverConnected) {
            FieldInfo("Video service connected", serverStatus.serviceConnected)
            FieldInfo("Power OK", serverStatus.powerIsEnough)
            FieldInfo("Power always OK", serverStatus.powerAlwaysEnough)
            FieldInfo("Power Volts", serverStatus.currentPowerVolts)
        }
        Row(modifier = Modifier.height(40.dp)) {}
        Divider()
        Row {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.padding(5.dp))
            Text(
                text = "Refresh",
                modifier = Modifier.clickable(
                    onClick = {
                        serverStatusVar.refresh(projectorController)
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
        Row {
            Icon(Icons.Default.PlayArrow, contentDescription = "Test video", modifier = Modifier.padding(5.dp))
            Text(
                text = "Test video",
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { projectorController.play(RemoteVideoPlayback("../../test.mp4")) }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
        Row {
            Icon(Icons.Default.Clear, contentDescription = "Reset", modifier = Modifier.padding(5.dp))
            Text(
                text = "Reset",
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { projectorController.reset() }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
        Row {
            Icon(Icons.Default.Clear, contentDescription = "Restart service", modifier = Modifier.padding(5.dp))
            Text(
                text = "Restart service",
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { projectorController.restartService() }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
        Row {
            Icon(Icons.Default.Lock, contentDescription = "Reboot", modifier = Modifier.padding(5.dp))
            Text(
                text = "Reboot",
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { projectorController.reboot() }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
        Row {
            Icon(Icons.Default.Lock, contentDescription = "Shutdown", modifier = Modifier.padding(5.dp))
            Text(
                text = "Shutdown",
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { projectorController.shutdown() }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
    }
}

@Composable
fun FieldInfo(field: String, value: Any?) {
    Row {
        Text(
            text = "$field: ${value?.toString().orEmpty()}",
            modifier = Modifier.padding(10.dp).fillMaxWidth()
        )
    }
    Divider()
}
