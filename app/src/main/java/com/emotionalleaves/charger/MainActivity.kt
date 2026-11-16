package com.emotionalleaves.charger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emotionalleaves.charger.data.CaseViewModel
import com.emotionalleaves.charger.logic.StepId
import com.emotionalleaves.charger.ui.*

class MainActivity : ComponentActivity() {
    private val vm: CaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargerTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val c by vm.state.collectAsStateWithLifecycle()
                    // Screen route: "home", "about", "script", or a StepId name.
                    var route by rememberSaveable { mutableStateOf("home") }
                    var scriptReturn by rememberSaveable { mutableStateOf("home") }
                    val step = StepId.entries.firstOrNull { it.name == route }

                    when {
                        step != null -> {
                            BackHandler { route = "home" }
                            StepScreen(
                                step = step, c = c, vm = vm,
                                onBack = { route = "home" },
                                onNext = { next -> route = next?.name ?: "home" },
                                onScript = { scriptReturn = step.name; route = "script" },
                            )
                        }
                        route == "script" -> ScriptScreen(c) { route = scriptReturn }
                        route == "about" -> AboutScreen(vm) { route = "home" }
                        else -> HomeScreen(c, onOpen = { route = it.name }, onAbout = { route = "about" })
                    }
                }
            }
        }
    }
}
