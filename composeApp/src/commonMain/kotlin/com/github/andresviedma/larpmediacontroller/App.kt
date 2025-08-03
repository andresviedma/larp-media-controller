package com.github.andresviedma.larpmediacontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.github.andresviedma.larpmediacontroller.gui.LarpControllerScreen
import com.github.andresviedma.larpmediacontroller.gui.LarpSelectorScreen
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

lateinit var asyncLauncher: CoroutineScope

@Composable
@Preview
fun App(coroutineContext: CoroutineContext = EmptyCoroutineContext) {
    asyncLauncher = rememberCoroutineScope { coroutineContext }

    val larpLoading = remember { mutableStateOf(false) }
    val larpController = remember { mutableStateOf<LarpController?>(null) }

    if (larpController.value != null) {
        globalLarpController = larpController.value
        // MusicPlayerView(larpController.value!!)
        LarpControllerScreen(larpController.value!!)
    } else {
        LarpSelectorScreen(LarpCatalog().getAvailableLarps(), larpLoading, larpController)
    }
}

private var globalLarpController: LarpController? = null
var globalLarpScenePosition: ScenePosition? = null

suspend fun triggerLarpKeyAction(keyCode: Int): Boolean {
    return globalLarpController?.shortcutAction(keyCode, globalLarpScenePosition ?: globalLarpController!!.getFirstScenePosition()) ?: false
}
