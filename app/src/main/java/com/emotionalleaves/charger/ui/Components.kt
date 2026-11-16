package com.emotionalleaves.charger.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emotionalleaves.charger.logic.Dates
import com.emotionalleaves.charger.logic.Deadlines
import com.emotionalleaves.charger.logic.Link
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun Section(title: String? = null, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().padding(top = 20.dp)) {
        if (title != null) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun Paragraph(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    caps: KeyboardCapitalization = KeyboardCapitalization.Words,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        supportingText = hint?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(capitalization = caps, keyboardType = keyboard),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun YesNo(question: String, value: Boolean?, onChange: (Boolean) -> Unit, help: String? = null) {
    Column(Modifier.fillMaxWidth()) {
        Text(question, style = MaterialTheme.typography.titleMedium)
        if (help != null) Text(help, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Choice("Yes", value == true, Modifier.weight(1f)) { onChange(true) }
            Choice("No", value == false, Modifier.weight(1f)) { onChange(false) }
        }
    }
}

@Composable
fun Choice(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier.heightIn(min = 48.dp)) {
            Icon(Icons.Filled.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier.heightIn(min = 48.dp)) { Text(label) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, value: String, onChange: (String) -> Unit, hint: String? = null) {
    var open by remember { mutableStateOf(false) }
    val d = Dates.parse(value)
    PickerBox(label, d?.let { Dates.pretty(it) } ?: "", hint, Icons.Filled.DateRange) { open = true }
    if (open) {
        val initial = (d ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val st = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    st.selectedDateMillis?.let { ms ->
                        onChange(java.time.Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    if (value.isNotBlank()) TextButton(onClick = { onChange(""); open = false }) { Text("Clear") }
                    TextButton(onClick = { open = false }) { Text("Cancel") }
                }
            },
        ) { DatePicker(state = st) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(label: String, value: String, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val t = Dates.parseTime(value)
    PickerBox(label, t?.let { Dates.time(it) } ?: "", null, Icons.Filled.Notifications) { open = true }
    if (open) {
        val st = rememberTimePickerState(initialHour = t?.hour ?: 9, initialMinute = t?.minute ?: 0, is24Hour = false)
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton(onClick = { onChange(LocalTime.of(st.hour, st.minute).toString()); open = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
            text = { TimePicker(state = st) },
        )
    }
}

@Composable
private fun PickerBox(label: String, shown: String, hint: String?, icon: ImageVector, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(shown.ifBlank { "Tap to choose" }, style = MaterialTheme.typography.bodyLarge,
                        color = if (shown.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                }
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (hint != null) Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(label: String, value: String, options: List<String>, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { onChange(o); open = false }) }
        }
    }
}

enum class Tone { INFO, GOOD, WARN, BAD }

@Composable
fun Note(text: String, tone: Tone = Tone.INFO) {
    val cs = MaterialTheme.colorScheme
    val (bg, fg, icon) = when (tone) {
        Tone.INFO -> Triple(cs.primaryContainer, cs.onPrimaryContainer, Icons.Filled.Info)
        Tone.GOOD -> Triple(cs.secondaryContainer, cs.onSecondaryContainer, Icons.Filled.CheckCircle)
        Tone.WARN -> Triple(cs.tertiaryContainer, cs.onTertiaryContainer, Icons.Filled.Warning)
        Tone.BAD -> Triple(cs.errorContainer, cs.onErrorContainer, Icons.Filled.Warning)
    }
    Surface(color = bg, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp).padding(top = 2.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, color = fg, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CheckNote(check: Deadlines.Check?) {
    check ?: return
    Note(check.message, when (check.level) { Deadlines.Level.OK -> Tone.GOOD; Deadlines.Level.WARN -> Tone.WARN; Deadlines.Level.PROBLEM -> Tone.BAD })
}

@Composable
fun ActionRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LinkRow(link: Link) {
    val ctx = LocalContext.current
    ActionRow(Icons.Filled.Search, link.label, Uri.parse(link.url).host?.removePrefix("www.")) { openUrl(ctx, link.url) }
}

@Composable
fun CheckRow(text: String, checked: Boolean, link: Link? = null, onToggle: () -> Unit) {
    val ctx = LocalContext.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(Modifier.weight(1f)) {
            Text(text, style = MaterialTheme.typography.bodyLarge,
                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
            if (link != null) Text(link.label + " ↗", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { openUrl(ctx, link.url) }.padding(vertical = 2.dp))
        }
    }
}

@Composable
fun StepDot(done: Boolean, current: Boolean, number: Int) {
    val cs = MaterialTheme.colorScheme
    Surface(shape = CircleShape, color = when { done -> cs.secondary; current -> cs.primary; else -> cs.surfaceVariant }, modifier = Modifier.size(28.dp)) {
        Box(contentAlignment = Alignment.Center) {
            if (done) Icon(Icons.Filled.Check, null, tint = cs.onSecondary, modifier = Modifier.size(16.dp))
            else Text("$number", style = MaterialTheme.typography.labelMedium, color = if (current) cs.onPrimary else cs.onSurfaceVariant)
        }
    }
}

// ---- Intents ----

fun openUrl(ctx: Context, url: String) {
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (e: Exception) { Toast.makeText(ctx, "No browser found", Toast.LENGTH_SHORT).show() }
}

fun dial(ctx: Context, phone: String) {
    try { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.filter { it.isDigit() || it == '+' })).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (e: Exception) { Toast.makeText(ctx, phone, Toast.LENGTH_LONG).show() }
}

fun openMap(ctx: Context, query: String) {
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (e: Exception) { openUrl(ctx, "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query)) }
}

fun addToCalendar(ctx: Context, title: String, date: LocalDate, time: LocalTime?, location: String?, notes: String?) {
    val zone = ZoneId.systemDefault()
    val i = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, title)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (time != null) {
        val start = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
        i.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start).putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 60 * 60 * 1000)
    } else {
        val start = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        i.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 24 * 60 * 60 * 1000)
    }
    if (!location.isNullOrBlank()) i.putExtra(CalendarContract.Events.EVENT_LOCATION, location)
    if (!notes.isNullOrBlank()) i.putExtra(CalendarContract.Events.DESCRIPTION, notes)
    try { ctx.startActivity(i) } catch (e: Exception) { Toast.makeText(ctx, "No calendar app found", Toast.LENGTH_SHORT).show() }
}
