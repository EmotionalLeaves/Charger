package com.emotionalleaves.charger.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.data.CaseViewModel
import com.emotionalleaves.charger.logic.Court
import com.emotionalleaves.charger.logic.Guide
import com.emotionalleaves.charger.logic.Link

/** Big, calm text to read from in court. Keeps the screen on. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptScreen(c: CaseData, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val view = LocalView.current
    DisposableEffect(Unit) { view.keepScreenOn = true; onDispose { view.keepScreenOn = false } }
    var big by remember { mutableStateOf(true) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Hearing script") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = { TextButton(onClick = { big = !big }) { Text(if (big) "Smaller text" else "Bigger text") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("When your case is called, say you're ready and would like to testify. After the oath:",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Guide.hearingScript(c).forEachIndexed { i, line ->
                Row(Modifier.padding(vertical = 10.dp)) {
                    Text("${i + 1}.", fontSize = if (big) 22.sp else 17.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(if (big) 36.dp else 28.dp))
                    Text(line, fontSize = if (big) 24.sp else 18.sp, lineHeight = if (big) 34.sp else 26.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Note("Speak slowly. It's fine to read straight from this screen. If the judge asks a question, answer it simply and truthfully.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(vm: CaseViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var confirm by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Paragraph("Charger walks you through an adult name change in Nebraska district court and fills in the official court forms for you.")
            Section("Your privacy") {
                Paragraph("Everything you type stays on this phone. The app has no account, no tracking, and doesn't send your answers anywhere. Forms are only shared when you tap Send.")
            }
            Section("Not legal advice") {
                Paragraph("This is a guide built from the Nebraska Judicial Branch's own instructions (checked November 2026). Rules and fees can change, and each county has local rules. When in doubt, ask the clerk, Legal Aid of Nebraska, or a lawyer.")
                LinkRow(Link("Find a lawyer (limited help is available)", Court.FIND_LAWYER))
            }
            Section("Sources") { Guide.sources.forEach { LinkRow(it) } }
            Section("Start over") {
                OutlinedButton(onClick = { confirm = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Erase all my answers")
                }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Erase everything?") },
            text = { Text("This deletes all your answers and progress from this phone. It can't be undone.") },
            confirmButton = { TextButton(onClick = { vm.reset(); confirm = false; onBack() }) { Text("Erase", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } },
        )
    }
}
