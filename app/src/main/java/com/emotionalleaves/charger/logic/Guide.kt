package com.emotionalleaves.charger.logic

import com.emotionalleaves.charger.data.CaseData

enum class StepId(val title: String, val short: String) {
    ELIGIBLE("Check you can file", "Eligibility"),
    DETAILS("Your names and details", "Details"),
    MONEY("Plan the cost", "Cost"),
    SAFETY("Newspaper safety check", "Safety"),
    FILE("File your petition", "File"),
    HEARING("Get a hearing date", "Hearing date"),
    PUBLISH("Put the notice in the newspaper", "Newspaper"),
    PREPARE("Get ready for the hearing", "Prepare"),
    HEARING_DAY("Hearing day", "Hearing day"),
    UPDATE("Update your records", "Records"),
}

data class Link(val label: String, val url: String)
data class ChecklistItem(val id: String, val text: String, val link: Link? = null)

object Guide {

    fun visibleSteps(c: CaseData): List<StepId> = StepId.entries.filter { it != StepId.PUBLISH || !c.publicationSkipped }

    /** Whether a step's required answers are filled in. Used to enable "Mark done". */
    fun missing(step: StepId, c: CaseData): List<String> = when (step) {
        StepId.ELIGIBLE -> if (c.isAdult == true && c.livedOneYear == true && c.honestReason == true) emptyList() else listOf("Yes to all three questions")
        StepId.DETAILS -> buildList {
            if (c.curFirst.isBlank() || c.curLast.isBlank()) add("Current first and last name")
            if (c.newFirst.isBlank() || c.newLast.isBlank()) add("New first and last name")
            if (c.street.isBlank() || c.city.isBlank() || c.zip.isBlank()) add("Address")
            if (c.phone.isBlank()) add("Phone")
            if (!c.noEmail && c.email.isBlank()) add("Email (or tick \"I can't get email\")")
            if (c.noEmail && c.noEmailReason.isBlank()) add("Why you can't get email")
            if (Dates.parse(c.birthDate) == null) add("Birth date")
            if (c.reason.isBlank()) add("Your reason")
        }
        StepId.MONEY -> if (c.askFeeWaiver == null) listOf("Choose how you'll pay the filing fee") else emptyList()
        StepId.SAFETY -> if (c.needsSafetyWaiver == null) listOf("Answer the safety question") else emptyList()
        StepId.FILE -> buildList {
            if (Dates.parse(c.filedDate) == null) add("The date you filed")
            if (c.caseNumber.isBlank()) add("Your case number")
        }
        StepId.HEARING -> buildList {
            if (c.waiverPending) add("The judge's decision on skipping the newspaper")
            if (Dates.parse(c.hearingDate) == null) add("Hearing date")
            if (Dates.parseTime(c.hearingTime) == null) add("Hearing time")
        }
        StepId.PUBLISH -> buildList {
            if (c.newspaper.isBlank()) add("Newspaper name")
            if (Dates.parse(c.pub1Date) == null) add("1st run date")
            if (Dates.parse(c.pub2Date) == null) add("2nd run date")
            if (c.hasInterestedParties == null) add("Answer the interested-parties question")
            if (c.hasInterestedParties == true && c.mailingAffidavitFiledDate.isBlank()) add("Mail the notice and file the Affidavit of Mailing")
        }
        StepId.PREPARE -> prepareChecklist(c).filter { it.id !in c.checked }.map { it.text }
        StepId.HEARING_DAY -> if (Dates.parse(c.decreeDate) == null) listOf("The date the judge signed your decree") else emptyList()
        StepId.UPDATE -> emptyList()
    }

    fun nextStep(c: CaseData): StepId? = visibleSteps(c).firstOrNull { it.name !in c.done }

    fun progress(c: CaseData): Pair<Int, Int> {
        val steps = visibleSteps(c)
        return steps.count { it.name in c.done } to steps.size
    }

    // ---- What each step says. Kept short on purpose: one idea per line. ----

    fun intro(step: StepId, c: CaseData): List<String> = when (step) {
        StepId.ELIGIBLE -> listOf(
            "In Nebraska, an adult changes their name by asking the district court in the county where they live.",
            "Answer three questions. If all are yes, you can file.",
        )
        StepId.DETAILS -> listOf(
            "These answers go straight onto the official court forms, so type them exactly how they should appear.",
            "Your current name is your legal name as it is now. Leave middle blank if you don't have one.",
        )
        StepId.MONEY -> listOf(
            "Three costs: the court filing fee (about ${Court.FILING_FEE}), the newspaper notice (the paper sets its price), and certified copies at the end (a few dollars each).",
            "If you can't afford the court costs, you can ask the judge to waive them.",
        )
        StepId.SAFETY -> listOf(
            "Normally your name change is printed in a newspaper so the public is notified.",
            "If printing it would put you in danger, you can ask the judge to skip it. You'll need proof, like a protection order or police reports. Most people don't qualify.",
        )
        StepId.FILE -> listOf(
            "Print two forms, sign the petition, and hand them to the clerk of the district court with the fee. The clerk gives you a case number. Clerks can't give legal advice, but can explain local rules.",
        )
        StepId.HEARING -> listOf(
            "Ask the clerk who schedules name change hearings (often the judge's bailiff), then call them.",
            if (c.publicationSkipped) "Your newspaper notice was waived, so ask for the next available hearing."
            else "Ask for a date at least 6 weeks after you filed, so the newspaper notice has time to run.",
            "Write down the date, time, judge, courtroom and building address.",
        )
        StepId.PUBLISH -> listOf(
            "Your notice runs in a newspaper sold in your county, once a week for 2 weeks in a row, and both runs finish before your hearing. The newspaper then sends the court its proof.",
        )
        StepId.PREPARE -> listOf(
            "Do these at least 2 days before your hearing.",
        )
        StepId.HEARING_DAY -> listOf(
            "Arrive 15 minutes early. Ask the clerk how to let the judge know you're there.",
            "When your case is called, say you're ready and would like to testify. You'll take an oath, then read your script.",
            "It usually takes under 10 minutes. You can bring people for support.",
        )
        StepId.UPDATE -> listOf(
            "Agencies only accept certified copies of your decree, not photocopies, and some keep the one you give them. Get copies from the clerk first.",
            "Then do Social Security. Most other places check against it. Your Nebraska driver's license or ID must be updated within 60 days.",
        )
    }

    fun recommendedCopies(c: CaseData) = 3

    /** Short "at a glance" facts for someone just starting. */
    val glance = listOf(
        "Time" to "Usually 6 to 10 weeks from filing to your hearing",
        "Cost" to "About ${Court.FILING_FEE} to the court, plus the newspaper. Fee waivers exist.",
        "Where" to "The district court in the county you've lived in for a year",
        "You'll need" to "A printer (or print shop), and one short court hearing",
    )

    /** Starting points for the reason box. Plain, honest, and short is what the court wants. */
    val exampleReasons = listOf(
        "I have used this name for years and want my legal name to match.",
        "This name reflects who I am.",
        "I want to take a family name.",
        "My current name is hard to spell and pronounce, and I want a simpler one.",
        "I want to go back to a name I used before.",
    )

    /** Gentle checks on how names are typed, since they print exactly as entered. */
    fun nameWarnings(c: CaseData): List<String> = buildList {
        val parts = listOf(c.curFirst, c.curMiddle, c.curLast, c.newFirst, c.newMiddle, c.newLast).map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.any { it.first().isLowerCase() }) add("A name starts with a lowercase letter. Forms print exactly what you type.")
        if (parts.any { p -> p.length > 2 && p.all { !it.isLetter() || it.isUpperCase() } }) add("A name is in ALL CAPS. Type it the way it should look on your ID.")
        if (parts.any { p -> p.any { it.isDigit() } }) add("A name contains a number. Double-check it.")
        if (c.newName.isNotBlank() && c.newName.equals(c.currentName, ignoreCase = true)) add("Your new name is the same as your current name.")
    }

    fun stillNeeded(missing: List<String>): String? = when (missing.size) {
        0 -> null
        1 -> "Still needed: ${missing[0]}"
        else -> "Still needed: ${missing[0]}, and ${missing.size - 1} more"
    }

    fun prepareChecklist(c: CaseData): List<ChecklistItem> = buildList {
        add(ChecklistItem("make_decree", "Create and print 2 copies of the Decree of Name Change (below)"))
        add(ChecklistItem("read_script", "Read your hearing script out loud once (below)"))
        if (!c.publicationSkipped) add(ChecklistItem("confirm_affidavit", "Call the clerk: confirm the newspaper filed its Affidavit of Publication"))
        if (c.hasInterestedParties == true) add(ChecklistItem("confirm_mailing", "Confirm your Affidavit of Mailing is filed"))
        add(ChecklistItem("plan_trip", "Plan how you'll get there and take time off work"))
        add(ChecklistItem("pack", "Pack: photo ID, 2 decree copies, your script, money for certified copies"))
    }

    fun updateChecklist(): List<ChecklistItem> = listOf(
        ChecklistItem("u_ssa", "Social Security card (free). Do this first.", Link("SSA: change your name", "https://www.ssa.gov/personal-record/change-name")),
        ChecklistItem("u_dmv", "Driver's license or state ID, within 60 days", Link("Nebraska DMV: name change", "https://dmv.nebraska.gov/dl/name-change")),
        ChecklistItem("u_passport", "Passport, if you have one. Issued less than 1 year ago: Form DS-5504. Older: renew by mail with DS-82. Send a certified decree.", Link("State Dept: change your name", "https://travel.state.gov/namechange")),
        ChecklistItem("u_birth", "Birth certificate. Born in Nebraska: mail DHHS Vital Records a certified decree and an amendment request (402-471-2871). Born elsewhere: that state's office.", Link("Nebraska DHHS: Vital Records changes", "https://dhhs.ne.gov/Pages/Vital-Records-Changes.aspx")),
        ChecklistItem("u_vote", "Voter registration. Nebraska requires you to re-register when your name changes, online or with a paper form.", Link("Nebraska Secretary of State", "https://sos.nebraska.gov")),
        ChecklistItem("u_work", "Employer HR and payroll (and your W-4)"),
        ChecklistItem("u_bank", "Bank, cards, and anyone you owe money to"),
        ChecklistItem("u_insurance", "Health, car, and renter's insurance"),
        ChecklistItem("u_health", "Doctor, dentist, pharmacy"),
        ChecklistItem("u_lease", "Lease or landlord, utilities, phone plan"),
        ChecklistItem("u_vehicle", "Car title and registration (county treasurer)"),
        ChecklistItem("u_school", "School records, if any"),
        ChecklistItem("u_accounts", "Email, apps, and online accounts"),
    )

    fun hearingScript(c: CaseData): List<String> {
        val cur = c.currentName.ifBlank { "[your current name]" }
        val new = c.newName.ifBlank { "[your new name]" }
        val city = c.city.ifBlank { "[your city]" }
        val county = c.county.ifBlank { "[county]" }
        val reason = c.reason.ifBlank { "[your reason]" }
        return buildList {
            add("My name is $cur.")
            add("I have lived in $city, $county County, Nebraska for at least one year before I filed the petition to change my name.")
            add("I wish to change my name from $cur, my current name, to my preferred new name of $new.")
            add("I am changing my name for the following reason: $reason")
            if (!c.publicationSkipped) add("I ask the court to take judicial notice of the Affidavit of Publication.")
            add("I have nothing further, Your Honor.")
        }
    }

    val sources = listOf(
        Link("Nebraska Judicial Branch: Adult Name Change", Court.OFFICIAL_GUIDE),
        Link("Adult Name Change packet and guides", Court.FORMS_PAGE),
        Link("Neb. Rev. Stat. § 25-21,271", Court.STATUTE),
        Link("Filing fees and court costs", Court.FEE_SCHEDULE),
        Link("Legal Aid of Nebraska guide", Court.LEGAL_AID),
        Link("District court clerk contacts", Court.CLERK_DIRECTORY),
    )

    val feeWaiverLinks = listOf(
        Link("Fee waiver guide (Appendix B)", "https://supremecourt.nebraska.gov/sites/default/files/DC-6-9-A2.pdf"),
        Link("Affidavit and Application (DC 6:7.1)", "https://supremecourt.nebraska.gov/sites/default/files/DC-6-7-1.pdf"),
        Link("Order to Proceed (DC 6:7.2)", "https://supremecourt.nebraska.gov/sites/default/files/DC-6-7-2.pdf"),
    )
}
