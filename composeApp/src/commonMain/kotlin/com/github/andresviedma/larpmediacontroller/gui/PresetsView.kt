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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.andresviedma.larpmediacontroller.LarpController
import com.github.andresviedma.larpmediacontroller.asyncLauncher
import korlibs.io.async.launch

@Composable
fun PresetsDialog(larpController: LarpController, shouldShowDialog: MutableState<Boolean>) {
    Dialog(
        onDismissRequest = { shouldShowDialog.value = false },
        properties = DialogProperties()
    ) {
        Surface {
            PresetsView(larpController)
        }
    }
}

@Composable
fun PresetsView(larpController: LarpController) {
    Column(modifier = Modifier.padding(10.dp)) {
        larpController.larp.presets.keys.sorted().forEach { preset ->
            Text(
                text = preset,
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { larpController.runPresetSettings(preset) }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
            Divider()
        }
        Row(modifier = Modifier.height(40.dp)) {}
        Divider()
        Row {
            Icon(Icons.Default.Clear, contentDescription = "Reset", modifier = Modifier.padding(5.dp))
            Text(
                text = "Reset",
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { larpController.reset() }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
        Row {
            Icon(Icons.Default.Lock, contentDescription = "Off", modifier = Modifier.padding(5.dp))
            Text(
                text = "Off",
                modifier = Modifier.clickable(
                    onClick = {
                        asyncLauncher.launch { larpController.off() }
                    }
                ).padding(10.dp).fillMaxWidth()
            )
        }
    }
}
