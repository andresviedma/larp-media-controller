@file:OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)

package com.github.andresviedma.larpmediacontroller.gui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.github.andresviedma.larpmediacontroller.LarpController
import com.github.andresviedma.larpmediacontroller.LarpScene
import com.github.andresviedma.larpmediacontroller.asyncLauncher
import com.mikepenz.markdown.m2.Markdown
import korlibs.io.async.launch

@Composable
fun LarpControllerScreen(larpController: LarpController) {
    var scenePosition by remember { mutableStateOf(larpController.getFirstScenePosition()) }
    var scene by remember { mutableStateOf(larpController.getSceneInfo(scenePosition)) }

    val pagerState = rememberPagerState(pageCount = { larpController.larp.numberOfScenes })

    val bulbsDialogShown = remember { mutableStateOf(false) }
    if (bulbsDialogShown.value) {
        BulbsDialog(larpController, bulbsDialogShown)
    }
    val presetsDialogShown = remember { mutableStateOf(false) }
    if (presetsDialogShown.value) {
        PresetsDialog(larpController, presetsDialogShown)
    }
    val musicDialogShown = remember { mutableStateOf(false) }
    if (musicDialogShown.value) {
        MusicSelectorDialog(larpController, musicDialogShown)
    }
    val videoDialogShown = remember { mutableStateOf(false) }
    if (videoDialogShown.value) {
        ProjectorDialog(larpController, videoDialogShown)
    }
    val serverDialogShown = remember { mutableStateOf(false) }
    if (serverDialogShown.value) {
        ProjectorControlDialog(larpController, serverDialogShown)
    }

    MaterialTheme {
        val scaffoldState = rememberScaffoldState()
        Scaffold(
            modifier = Modifier.padding(),
            scaffoldState = scaffoldState,
            drawerContent = {
                ModalDrawer(
                    drawerContent = {
                        Text("Main menu")
                    }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        DrawerButton("Apagar", Icons.Default.Lock) {
                            asyncLauncher.launch { larpController.off() }
                        }
                        DrawerButton("Luces", Icons.Default.Star) {
                            bulbsDialogShown.value = true
                        }
                        DrawerButton("Música", Icons.Default.PlayArrow) {
                            musicDialogShown.value = true
                        }
                        larpController.remoteVideoController?.let {
                            DrawerButton("Vídeos", Icons.Default.PlayArrow) {
                                videoDialogShown.value = true
                            }
                        }
                        DrawerButton("Presets", Icons.Default.Settings) {
                            presetsDialogShown.value = true
                        }
                        Divider()
                        Row(modifier = Modifier.height(40.dp)) {}
                        Divider()
                        if (larpController.remoteVideoController != null) {
                            DrawerButton("Proyector") {
                                serverDialogShown.value = true
                            }
                        }
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text("${scene.number}. ${scene.title}")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                asyncLauncher.launch {
                                    scaffoldState.drawerState.open()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(
                            enabled = scenePosition.hasPrevious,
                            onClick = {
                                asyncLauncher.launch {
                                    pagerState.scrollToPage(scenePosition.previous().number - 1)
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Previous")
                        }
                        IconButton(
                            enabled = scenePosition.hasNext,
                            onClick = {
                                asyncLauncher.launch {
                                    pagerState.scrollToPage(scenePosition.next().number - 1)
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Next")
                        }
                        IconButton(
                            onClick = {
                                asyncLauncher.launch { larpController.runPresetSettings("cutwarning") }
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Scene end warning")
                        }
                        IconButton(
                            onClick = {
                                asyncLauncher.launch { larpController.endOfScene() }
                            }
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "End of scene")
                        }
                        IconButton(
                            onClick = {
                                asyncLauncher.launch { larpController.reset() }
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Reset")
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        asyncLauncher.launch { larpController.runSceneSettings(scenePosition) }
                    }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play scene")
                }
            },
            bottomBar = {
                BottomAppBar(modifier = Modifier.height(90.dp)) {
                    FlowRow {
                        MusicPlayerView(larpController)
                        for ((actionKey, action) in scene.actions) {
                            Button(
                                onClick = {
                                    asyncLauncher.launch { larpController.runSceneAction(scenePosition, actionKey) }
                                }
                            ) {
                                Text(action.text)
                            }
                        }
                    }
                }
            },
        ) {
            HorizontalPager(state = pagerState) { page ->
                if (page != scenePosition.number - 1) {
                    scenePosition = scenePosition.withValidPosition(page + 1)
                    scene = larpController.getSceneInfo(scenePosition)
                }
                SceneDetailView(scene)
            }
        }
    }
}

@Composable
fun SceneDetailView(scene: LarpScene) {
    Markdown(
        content = scene.description,
        modifier = Modifier.padding(24.dp)
            .padding(bottom = (24 * 4).dp)
            .verticalScroll(rememberScrollState())
    )
}

@Composable
fun DrawerButton(text: String, icon: ImageVector? = null, onClick: () -> Unit) {
    Row {
        icon?.let { Icon(it, contentDescription = text, modifier = Modifier.padding(10.dp)) }
        Text(text, modifier = Modifier.clickable(onClick = onClick).padding(10.dp).fillMaxWidth())
    }
    Divider()
}
