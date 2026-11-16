package com.emotionalleaves.charger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.data.CaseViewModel
import com.emotionalleaves.charger.logic.*
import com.emotionalleaves.charger.pdf.PdfFiller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepScreen(step: StepId, c: CaseData, vm: CaseViewModel, onBack: () -> Unit, onNext: (StepId?) -> Unit, onScript: () -> Unit) {
    val steps = Guide.visibleSteps(c)
    val index = steps.indexOf(step).coerceAtLeast(0)
    val missing = Guide.missing(step, c)
    val isDone = step.name in c.done
    val scroll = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Step ${index + 1} of ${steps.size}", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    if (isDone) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { vm.setDone(step.name, false) }, modifier = Modifier.heightIn(min = 52.dp)) { Text("Not done") }
                            Button(onClick = { onNext(Guide.nextStep(c)) }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Next step") }
                        }
                    } else {
                        Guide.stillNeeded(missing)?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp), maxLines = 2)
                        }
                        Button(
                            onClick = {
                                vm.setDone(step.name, true)
                                onNext(Guide.nextStep(c.copy(done = c.done + step.name)))
                            },
                            enabled = missing.isEmpty(),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        ) { Text(if (step == StepId.UPDATE) "Finish" else "Done, next step") }
                    }
                }
            }
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(scroll).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            // Where you are: one segment per step.
            Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                steps.forEachIndexed { i, s ->
                    val color = when {
                        i == index -> MaterialTheme.colorScheme.primary
                        s.name in c.done -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Surface(color = color, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f).height(4.dp)) {}
                }
            }
            Text(step.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Guide.intro(step, c).forEach { Paragraph(it) } }
            when (step) {
                StepId.ELIGIBLE -> Eligible(c, vm)
                StepId.DETAILS -> Details(c, vm)
                StepId.MONEY -> Money(c, vm)
                StepId.SAFETY -> Safety(c, vm)
                StepId.FILE -> FileStep(c, vm)
                StepId.HEARING -> HearingStep(c, vm)
                StepId.PUBLISH -> Publish(c, vm)
                StepId.PREPARE -> Prepare(c, vm, onScript)
                StepId.HEARING_DAY -> HearingDay(c, vm, onScript)
                StepId.UPDATE -> Update(c, vm)
            }
        }
    }
}

// ---------------- Steps ----------------

@Composable
private fun Eligible(c: CaseData, vm: CaseViewModel) {
    Section("Where you live") {
        Dropdown("County", c.county, Court.counties) { v -> vm.update { it.copy(county = v) } }
        if (!c.county.equals("Lancaster", true) && c.city.equals("Lincoln", true)) {
            Note("Lincoln (the city) is in Lancaster County. Lincoln County is a different place, in western Nebraska.", Tone.WARN)
        }
    }
    Section("Three questions") {
        YesNo("Are you 19 or older?", c.isAdult, { v -> vm.update { it.copy(isAdult = v) } },
            help = "Also counts as yes if you're married or a court has emancipated you.")
        YesNo("Have you lived in ${c.county} County for at least 1 year?", c.livedOneYear, { v -> vm.update { it.copy(livedOneYear = v) } })
        YesNo("Is your reason honest?", c.honestReason, { v -> vm.update { it.copy(honestReason = v) } },
            help = "You can't change your name to avoid debts, a lawsuit, or criminal charges. Any honest reason is fine.")
    }
    when {
        c.isAdult == false -> Note("If you're under 19, a parent or guardian files for you using a different (minor) process.", Tone.BAD)
        c.livedOneYear == false -> Note("Wait until you've lived in the county a full year, then file there. The app will keep your answers.", Tone.BAD)
        c.honestReason == false -> Note("The court won't allow a change made to avoid debts, lawsuits or charges.", Tone.BAD)
        c.isAdult == true && c.livedOneYear == true && c.honestReason == true -> Note("You can file in ${c.county} County.", Tone.GOOD)
    }
}

@Composable
private fun Details(c: CaseData, vm: CaseViewModel) {
    Section("Current legal name") {
        Field("First", c.curFirst, { v -> vm.update { it.copy(curFirst = v) } })
        Field("Middle (optional)", c.curMiddle, { v -> vm.update { it.copy(curMiddle = v) } })
        Field("Last", c.curLast, { v -> vm.update { it.copy(curLast = v) } })
    }
    Section("New name") {
        Field("First", c.newFirst, { v -> vm.update { it.copy(newFirst = v) } })
        Field("Middle (optional)", c.newMiddle, { v -> vm.update { it.copy(newMiddle = v) } })
        Field("Last", c.newLast, { v -> vm.update { it.copy(newLast = v) } }, hint = "Keeping the same last name? Type it again.")
        if (c.newName.isNotBlank()) Note("Your new name will read: ${c.newName}")
        Guide.nameWarnings(c).forEach { Note(it, Tone.WARN) }
    }
    Section("Your reason") {
        Field("Why you want to change your name", c.reason, { v -> vm.update { it.copy(reason = v) } },
            caps = KeyboardCapitalization.Sentences, singleLine = false, minLines = 3,
            hint = "Plain and true is best. You'll say this to the judge, so keep it to a sentence or two.")
        if (c.reason.isBlank()) {
            Text("Need a start? Tap one, then edit it to fit you.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Guide.exampleReasons.forEach { r ->
                SuggestionChip(onClick = { vm.update { it.copy(reason = r) } }, label = { Text(r, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 6.dp)) },
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
    Section("Address and contact") {
        Field("Street address", c.street, { v -> vm.update { it.copy(street = v) } })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("City", c.city, { v -> vm.update { it.copy(city = v) } }, Modifier.weight(1.4f))
            Field("ZIP", c.zip, { v -> vm.update { it.copy(zip = v.filter(Char::isDigit).take(5)) } }, Modifier.weight(1f), keyboard = KeyboardType.Number)
        }
        Field("Phone", c.phone, { v -> vm.update { it.copy(phone = v) } }, keyboard = KeyboardType.Phone)
        if (!c.noEmail) Field("Email", c.email, { v -> vm.update { it.copy(email = v.trim()) } }, keyboard = KeyboardType.Email, caps = KeyboardCapitalization.None,
            hint = "The court sends case notices here.")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(c.noEmail, { v -> vm.update { it.copy(noEmail = v) } })
            Text("I can't get email", style = MaterialTheme.typography.bodyLarge)
        }
        if (c.noEmail) Field("Why you can't get email", c.noEmailReason, { v -> vm.update { it.copy(noEmailReason = v) } }, caps = KeyboardCapitalization.Sentences)
    }
    Section("Private") {
        DateField("Birth date", c.birthDate, { v -> vm.update { it.copy(birthDate = v) } },
            hint = "Goes only on the confidential form, which isn't public.")
    }
}

@Composable
private fun Money(c: CaseData, vm: CaseViewModel) {
    Section("Filing fee") {
        YesNo("Can you pay the filing fee?", c.askFeeWaiver?.not(), { v -> vm.update { it.copy(askFeeWaiver = !v) } },
            help = "If not, you'll ask the judge to waive it when you file.")
        if (c.askFeeWaiver == true) {
            Note("Fill out these two forms and bring them when you file. Be honest and complete. The judge decides; if they say no, you'll pay the fee to continue.")
            Guide.feeWaiverLinks.forEach { LinkRow(it) }
        }
    }
    Section("Typical total") {
        Note("Court about ${Court.FILING_FEE} + newspaper (call to ask) + certified copies (a few dollars each). New license and passport fees come later.")
        LinkRow(Link("Current court fees", Court.FEE_SCHEDULE))
    }
}

@Composable
private fun Safety(c: CaseData, vm: CaseViewModel) {
    Section("The question") {
        YesNo("Would printing your name change in the newspaper put you in danger?", c.needsSafetyWaiver,
            { v -> vm.update { it.copy(needsSafetyWaiver = v, waiverResult = if (!v) "" else it.waiverResult) } })
    }
    if (c.needsSafetyWaiver == false) Note("No waiver needed. You'll publish the notice in step 7.", Tone.GOOD)
    if (c.needsSafetyWaiver == true) {
        Section("Ask to skip the newspaper") {
            Paragraph("File this request at the same time as your petition, with paper copies of your proof (for example a protection order, police reports, or court records).")
            FormCard(CourtForm.WAIVER, c, note = "Your name, contact details and case type are filled in. Write your reasons and list your proof by hand.")
            LinkRow(Link("Waiver guide (Appendix A) in the court packet", Court.FORMS_PAGE))
        }
        WaiverDecision(c, vm)
    }
}

@Composable
private fun WaiverDecision(c: CaseData, vm: CaseViewModel) {
    Section("The judge's decision on skipping the newspaper") {
        Paragraph("The clerk tells you in writing (or by email). Enter it here when you hear.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Choice("Granted", c.waiverResult == "granted", Modifier.weight(1f)) { vm.update { it.copy(waiverResult = "granted") } }
            Choice("Denied", c.waiverResult == "denied", Modifier.weight(1f)) { vm.update { it.copy(waiverResult = "denied") } }
        }
        when (c.waiverResult) {
            "granted" -> Note("You can skip the newspaper step.", Tone.GOOD)
            "denied" -> Note("You'll need to publish the notice in the newspaper.", Tone.WARN)
            else -> Note("Still waiting? Keep going: file your petition. You'll be asked again before setting your hearing.")
        }
    }
}

@Composable
private fun FileStep(c: CaseData, vm: CaseViewModel) {
    val ctx = LocalContext.current
    val clerk = Court.clerkFor(c.county)
    Section("1 · Make your forms") {
        FormCard(CourtForm.PETITION, c, note = "Print it, then sign and date page 2.")
        FormCard(CourtForm.CONFIDENTIAL, c, note = "Not public. The court may also ask for your Social Security number; write it by hand if the clerk asks.")
        if (Guide.missing(StepId.DETAILS, c).isNotEmpty()) Note("Some details are missing (step 2). The forms will have blanks.", Tone.WARN)
        Note("Print shows your printers, or \"Save as PDF\". No printer? Tap the share icon to email it to a library or print shop.")
    }
    Section("2 · Take them to the clerk") {
        val bring = buildList {
            add("Signed petition")
            add("Confidential form")
            add(if (c.askFeeWaiver == true) "Fee waiver forms (DC 6:7.1 and 6:7.2)" else "Filing fee (about ${Court.FILING_FEE})")
            if (c.needsSafetyWaiver == true) add("Waiver request and copies of your proof")
            add("Photo ID")
        }
        Note("Bring: " + bring.joinToString(" · "))
        Note("Mailing instead? No cash. Send a check or money order payable to \"Clerk of the District Court\". The clerk can't give legal advice but can explain local rules.")
        if (clerk != null) {
            ActionRow(Icons.Filled.LocationOn, clerk.name, clerk.address) { openMap(ctx, clerk.mapQuery) }
            ActionRow(Icons.Filled.Phone, "Call the clerk", clerk.phone + " · ask about hours and filing by mail") { dial(ctx, clerk.phone) }
        } else {
            LinkRow(Link("Find your county's district court clerk", Court.CLERK_DIRECTORY))
        }
    }
    Section("3 · Write down what the clerk gives you") {
        DateField("Date you filed", c.filedDate, { v -> vm.update { it.copy(filedDate = v) } })
        Field("Case number", c.caseNumber, { v -> vm.update { it.copy(caseNumber = v.trim()) } }, caps = KeyboardCapitalization.Characters,
            hint = "Printed on your filed petition, e.g. CI 26-1234")
    }
}

@Composable
private fun HearingStep(c: CaseData, vm: CaseViewModel) {
    val ctx = LocalContext.current
    val clerk = Court.clerkFor(c.county)
    if (c.needsSafetyWaiver == true) WaiverDecision(c, vm)
    Dates.parse(c.filedDate)?.let { filed ->
        if (!c.publicationSkipped) Note("Ask for ${Dates.pretty(Deadlines.earliestHearing(filed))} or later.")
    }
    if (clerk != null) Section("Call") { ActionRow(Icons.Filled.Phone, "Call the clerk", clerk.phone + " · \"Who schedules a name change hearing?\"") { dial(ctx, clerk.phone) } }
    Section("Your hearing") {
        DateField("Date", c.hearingDate, { v -> vm.update { it.copy(hearingDate = v) } })
        CheckNote(Deadlines.hearingCheck(c))
        TimeField("Time", c.hearingTime, { v -> vm.update { it.copy(hearingTime = v) } })
        Field("Judge's name", c.judge, { v -> vm.update { it.copy(judge = v) } }, hint = "Needed for the newspaper notice.")
        Field("Courtroom", c.courtroom, { v -> vm.update { it.copy(courtroom = v) } })
        Field("Courthouse address", c.courthouseAddress, { v -> vm.update { it.copy(courthouseAddress = v) } })
        LaunchedEffect(clerk) { if (c.courthouseAddress.isBlank() && clerk != null) vm.update { it.copy(courthouseAddress = clerk.address) } }
    }
    Dates.parse(c.hearingDate)?.let { d ->
        Section {
            ActionRow(Icons.Filled.DateRange, "Add hearing to my calendar", Dates.pretty(d)) {
                addToCalendar(ctx, "Name change hearing", d, Dates.parseTime(c.hearingTime), c.courthouseAddress,
                    listOf(c.judge.let { if (it.isBlank()) "" else "Judge $it" }, c.courtroom.let { if (it.isBlank()) "" else "Courtroom $it" }, "Case ${c.caseNumber}").filter { it.isNotBlank() }.joinToString("\n"))
            }
        }
    }
}

@Composable
private fun Publish(c: CaseData, vm: CaseViewModel) {
    val ctx = LocalContext.current
    val hearing = Dates.parse(c.hearingDate)
    if (hearing != null) Note("Get the 1st run in by ${Dates.pretty(Deadlines.latestFirstRun(hearing))} so both runs and the newspaper's proof are done before your hearing.")
    Section("1 · Make the notice") {
        FormCard(CourtForm.NOTICE, c, note = "Send this to the newspaper.")
        if (c.judge.isBlank() || c.hearingDate.isBlank()) Note("Add your hearing details first (step 6) so the notice is complete.", Tone.WARN)
    }
    Section("2 · Pick a newspaper") {
        ActionRow(Icons.Filled.Phone, "Nebraska Press Association", "${Court.NEWSPAPERS_PHONE} · they'll tell you which papers can run it in ${c.county} County") { dial(ctx, Court.NEWSPAPERS_PHONE) }
        LinkRow(Link("Newspaper list", Court.NEWSPAPERS))
        Note("Tell the paper: \"Legal notice for an adult name change, once a week for 2 weeks in a row. Please file the Affidavit of Publication with the ${c.county} County District Court, case ${c.caseNumber.ifBlank { "[number]" }}.\"")
        Field("Newspaper name", c.newspaper, { v -> vm.update { it.copy(newspaper = v) } }, hint = "Also goes on your decree.")
    }
    Section("3 · Run dates") {
        DateField("1st run", c.pub1Date, { v ->
            vm.update { s -> s.copy(pub1Date = v, pub2Date = if (s.pub2Date.isBlank()) Dates.parse(v)?.plusDays(7)?.toString() ?: "" else s.pub2Date) }
        })
        DateField("2nd run", c.pub2Date, { v -> vm.update { it.copy(pub2Date = v) } }, hint = "Usually exactly one week later.")
        CheckNote(Deadlines.publicationCheck(c))
    }
    Section("4 · Anyone with a legal interest?") {
        YesNo("Do you owe anyone money, or have children under 19, or know someone with a legal reason to be told?", c.hasInterestedParties,
            { v -> vm.update { it.copy(hasInterestedParties = v) } },
            help = "The court guide says an interested party may include a creditor or the parent of your children under 19. Many adults have none.")
        if (c.hasInterestedParties == true) {
            val p1 = Dates.parse(c.pub1Date)
            Note(if (p1 != null) "Mail each of them a copy of the first published notice by ${Dates.pretty(Deadlines.mailBy(p1))} (within 5 days of the 1st run)." else "Within 5 days of the 1st run, mail each of them a copy of the published notice.")
            DateField("Date you mailed them", c.mailedDate, { v -> vm.update { it.copy(mailedDate = v) } })
            Dates.parse(c.mailedDate)?.let { m ->
                Note("Sign the Affidavit of Mailing in front of a notary and file it by ${Dates.pretty(Deadlines.affidavitBy(m, hearing))}.")
            }
            FormCard(CourtForm.AFFIDAVIT_MAILING, c, note = "Partly filled. Add who you mailed and when, then sign in front of a notary (banks often do this free for customers).")
            DateField("Date you filed the affidavit", c.mailingAffidavitFiledDate, { v -> vm.update { it.copy(mailingAffidavitFiledDate = v) } })
            LinkRow(Link("Copies of published notices", Court.PUBLIC_NOTICES))
        }
    }
}

@Composable
private fun Prepare(c: CaseData, vm: CaseViewModel, onScript: () -> Unit) {
    val ctx = LocalContext.current
    Section("Checklist") {
        Guide.prepareChecklist(c).forEach { item -> CheckRow(item.text, item.id in c.checked) { vm.toggleCheck(item.id) } }
    }
    Section("Your papers") {
        FormCard(CourtForm.DECREE, c, note = "Print 2 copies. Leave the date and signature for the judge.")
        ActionRow(Icons.Filled.Edit, "Your hearing script", "What to say, word for word") { onScript() }
    }
    Court.clerkFor(c.county)?.let { clerk ->
        Section { ActionRow(Icons.Filled.Phone, "Call the clerk", clerk.phone + " · confirm proof of publication is filed") { dial(ctx, clerk.phone) } }
    }
}

@Composable
private fun HearingDay(c: CaseData, vm: CaseViewModel, onScript: () -> Unit) {
    val ctx = LocalContext.current
    Dates.parse(c.hearingDate)?.let { d ->
        Section("When and where") {
            Note(listOf(Dates.pretty(d), Dates.parseTime(c.hearingTime)?.let(Dates::time), c.courtroom.takeIf { it.isNotBlank() }?.let { "Courtroom $it" }, c.judge.takeIf { it.isNotBlank() }?.let { "Judge $it" })
                .filterNotNull().joinToString(" · "))
            if (c.courthouseAddress.isNotBlank()) ActionRow(Icons.Filled.LocationOn, "Directions", c.courthouseAddress) { openMap(ctx, c.courthouseAddress) }
        }
    }
    Section {
        Button(onClick = onScript, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Icon(Icons.Filled.Edit, null); Spacer(Modifier.width(8.dp)); Text("Open my script")
        }
    }
    Section("After the judge signs") {
        DateField("Date the decree was signed", c.decreeDate, { v -> vm.update { it.copy(decreeDate = v) } })
        if (c.decreeDate.isNotBlank()) Note("Congratulations. Your name is now ${c.newName.ifBlank { "changed" }}.", Tone.GOOD)
        Note("If the judge doesn't sign, write down what they said needs fixing and ask the clerk about next steps.")
    }
}


@Composable
private fun Update(c: CaseData, vm: CaseViewModel) {
    val ctx = LocalContext.current
    Section("First: certified copies") {
        CheckRow("Get ${Guide.recommendedCopies(c)} or more certified copies of your signed decree from the clerk", "got_copies" in c.checked) { vm.toggleCheck("got_copies") }
        Court.clerkFor(c.county)?.let { clerk -> ActionRow(Icons.Filled.LocationOn, clerk.name, clerk.address) { openMap(ctx, clerk.mapQuery) } }
    }
    val list = Guide.updateChecklist()
    val n = list.count { it.id in c.checked }
    Dates.parse(c.decreeDate)?.let { d ->
        Note("Driver's license or ID deadline: ${Dates.pretty(d.plusDays(60))} (60 days).", Tone.WARN)
    }
    Section("Then: $n of ${list.size} updated") {
        list.forEach { item -> CheckRow(item.text, item.id in c.checked, item.link) { vm.toggleCheck(item.id) } }
    }
}

// ---------------- Form card ----------------

@Composable
fun FormCard(form: CourtForm, c: CaseData, note: String? = null) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Create, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(form.title, style = MaterialTheme.typography.titleMedium)
                    Text("Official form ${form.code}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (note != null) Text(note, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            if (error != null) Text(error!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fun make(action: (java.io.File) -> Unit) {
                    busy = true; error = null
                    scope.launch {
                        val f = runCatching { withContext(Dispatchers.IO) { PdfFiller.fill(ctx, form, c) } }
                        busy = false
                        f.onSuccess { runCatching { action(it) }.onFailure { e -> error = "Couldn't open that (${e.message}). Try Send instead." } }
                            .onFailure { error = "Couldn't fill the form (${it.message}). Opening the blank one instead."; runCatching { PdfFiller.open(ctx, PdfFiller.blank(ctx, form)) } }
                    }
                }
                Button(onClick = { make { PdfFiller.print(ctx, it, "Charger ${form.code}") } }, enabled = !busy, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Print")
                }
                OutlinedButton(onClick = { make { PdfFiller.open(ctx, it) } }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("View") }
                OutlinedButton(onClick = { make { PdfFiller.share(ctx, it) } }, enabled = !busy, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Filled.Share, "Send", Modifier.size(18.dp))
                }
            }
        }
    }
}
