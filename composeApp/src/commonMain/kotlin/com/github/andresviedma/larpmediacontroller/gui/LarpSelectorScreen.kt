package com.github.andresviedma.larpmediacontroller.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.andresviedma.larpmediacontroller.LarpController
import com.github.andresviedma.larpmediacontroller.asyncLauncher
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun LarpSelectorScreen(basePaths: List<String>, larpLoading: MutableState<Boolean>, larpController: MutableState<LarpController?>) {
    Column(modifier = Modifier.padding(10.dp)) {
        Text("Elige el ReV", modifier = Modifier.padding(10.dp), fontSize = 24.sp)
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Divider()
            basePaths.forEach {
                val fileName = File(it).absolutePath.condensedDirectoryPath()
                TextButton(
                    onClick = {
                        asyncLauncher.launch {
                            larpLoading.value = true
                            larpController.value = LarpController.loadLarp(it)
                            larpLoading.value = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !larpLoading.value
                ) {
                    Text(fileName, modifier = Modifier.padding(10.dp))
                }
                Divider()
            }
        }
    }
}

private fun String.condensedDirectoryPath(): String =
    split('/').let { it.subList(maxOf(0, it.size - 2), it.size) }
        .joinToString("/")

