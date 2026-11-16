package com.emotionalleaves.charger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.logic.Dates
import com.emotionalleaves.charger.logic.Deadlines
import com.emotionalleaves.charger.logic.Guide
import com.emotionalleaves.charger.logic.StepId
import java.time.LocalDate

@Composable
fun HomeScreen(c: CaseData, onOpen: (StepId) -> Unit, onAbout: () -> Unit, onScript: () -> Unit = {}, today: LocalDate = LocalDate.now()) {
    val steps = Guide.visibleSteps(c)
    val next = Guide.nextStep(c)
    val (done, total) = Guide.progress(c)
    var showAll by rememberSaveable { mutableStateOf(false) }
    val ctx = LocalContext.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Charger", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (c.newName.isNotBlank()) "Becoming ${c.newName}" else "Your Nebraska name change",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onAbout) { Icon(Icons.Filled.Info, "About and settings", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }

        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else done / total.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            drawStopIndicator = {},
        )
        Text("$done of $total steps done", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))

        // Hearing today or tomorrow: script and directions come first.
        val hearing = Dates.parse(c.hearingDate)
        val hearingSoon = hearing != null && c.decreeDate.isBlank() && Dates.daysBetween(today, hearing) in 0L..1L
        if (hearing != null && hearingSoon) {
            Spacer(Modifier.height(20.dp))
            ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text(if (hearing == today) "YOUR HEARING IS TODAY" else "YOUR HEARING IS TOMORROW", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text(listOfNotNull(Dates.parseTime(c.hearingTime)?.let(Dates::time), c.courtroom.takeIf { it.isNotBlank() }?.let { "Courtroom $it" }).joinToString(" · ").ifBlank { "District Court" },
                        style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(top = 4.dp))
                    Text("Arrive 15 minutes early. Bring ID and 2 copies of your decree.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(top = 6.dp))
                    Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onScript, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("My script") }
                        if (c.courthouseAddress.isNotBlank()) OutlinedButton(onClick = { openMap(ctx, c.courthouseAddress) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Directions") }
                    }
                }
            }
        }

        // The one thing to do now (unless the hearing card above already is that).
        Spacer(Modifier.height(20.dp))
        if (next != null && !(hearingSoon && next == StepId.HEARING_DAY)) {
            val n = steps.indexOf(next) + 1
            ElevatedCard(onClick = { onOpen(next) }, modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("NEXT · STEP $n", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(next.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 4.dp))
                    Text(Guide.intro(next, c).first(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 6.dp), maxLines = 3)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { onOpen(next) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text(if (n == 1 && done == 0) "Start" else "Continue")
                        Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.ArrowForward, null, Modifier.size(18.dp))
                    }
                }
            }
        } else if (next == null) {
            ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("All done", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Your name is legally ${c.newName.ifBlank { "changed" }}. Keep your certified copies somewhere safe.",
                        style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }

        // First run: what to expect, in four lines.
        if (done == 0) {
            Section("At a glance") {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Guide.glance.forEachIndexed { i, (k, v) ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(k, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(96.dp))
                                Text(v, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Dates that matter, only when there are some.
        val upcoming = Deadlines.upcoming(c, today).take(4)
        if (upcoming.isNotEmpty()) {
            Section("Coming up · tap to add to calendar") {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        upcoming.forEachIndexed { i, item ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(Modifier.clickable { val isHearing = item.kind == Deadlines.Kind.HEARING
                                addToCalendar(ctx, if (isHearing) "Name change hearing" else item.title, item.date, if (isHearing) Dates.parseTime(c.hearingTime) else null, if (isHearing) c.courthouseAddress else null, item.detail) }
                                .padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.width(64.dp)) {
                                    Text(item.date.month.name.take(3), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Text("${item.date.dayOfMonth}", style = MaterialTheme.typography.titleLarge)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                                    Text("${Dates.relative(item.date, today).replaceFirstChar { it.uppercase() }} · ${item.detail}",
                                        style = MaterialTheme.typography.bodyMedium, color = if (item.date.isBefore(today)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Filled.DateRange, "Add to calendar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Every step, folded away unless wanted.
        Section {
            TextButton(onClick = { showAll = !showAll }, contentPadding = PaddingValues(0.dp)) {
                Text(if (showAll) "Hide all steps" else "See all steps", fontWeight = FontWeight.Medium)
                Icon(if (showAll) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, null)
            }
            if (showAll) {
                steps.forEachIndexed { i, s ->
                    val isDone = s.name in c.done
                    Surface(onClick = { onOpen(s) }, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            StepDot(isDone, s == next, i + 1)
                            Spacer(Modifier.width(12.dp))
                            Text(s.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f),
                                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("A guide, not legal advice. Your county's clerk has the final word on local rules.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
