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
    COPIES("Get certified copies", "Copies"),
    UPDATE("Update your records", "Records"),
}

data class Link(val label: String, val url: String)
data class ChecklistItem(val id: String, val text: String, val link: Link? = null)

object Guide {

    fun visibleSteps(c: CaseData): List<StepId> = StepId.entries.filter { it != StepId.PUBLISH || !c.publicationSkipped }

    /** Whether a step's required answers are filled in. Used to enable "Mark done". */
    fun missing(step: StepId, c: CaseData): List<String> = when (step) {
        StepId.ELIGIBLE -> buildList {
            if (c.isAdult != true) add("You must be an adult under Nebraska law")
            if (c.livedOneYear != true) add("You must have lived in the county for 1 year")
            if (c.honestReason != true) add("Your reason must be an honest one")
        }
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
        StepId.SAFETY -> buildList {
            if (c.needsSafetyWaiver == null) add("Answer the safety question")
            if (c.needsSafetyWaiver == true && c.waiverResult.isBlank()) add("Enter the judge's decision once you hear back")
        }
        StepId.FILE -> buildList {
            if (Dates.parse(c.filedDate) == null) add("The date you filed")
            if (c.caseNumber.isBlank()) add("Your case number")
        }
        StepId.HEARING -> buildList {
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
        StepId.COPIES -> if ("got_copies" !in c.checked) listOf("Pick up your certified copies") else emptyList()
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
            "Filing fee: about ${Court.FILING_FEE}. ${Court.FILING_FEE_NOTE}",
            "Newspaper notice: the newspaper sets its own price. Call and ask before you file.",
            "Certified copies after your hearing: a few dollars each.",
            "If you can't afford the court costs, you can ask the judge to waive them (called \"in forma pauperis\").",
        )
        StepId.SAFETY -> listOf(
            "Normally your name change is printed in a newspaper so the public is notified.",
            "If printing it would put you in danger, you can ask the judge to skip it. You'll need proof, like a protection order or police reports. Most people don't qualify.",
        )
        StepId.FILE -> buildList {
            add("Create your two filing forms below, print them, and sign the petition.")
            add("Then take them, with the fee or your fee-waiver forms, to the clerk of the district court. You can also mail them (no cash by mail; check or money order payable to \"Clerk of the District Court\").")
            add("The clerk gives you a case number. Every later form needs it.")
            add("The clerk can't give legal advice, but can tell you local rules.")
        }
        StepId.HEARING -> listOf(
            "Ask the clerk who schedules name change hearings (often the judge's bailiff), then call them.",
            if (c.publicationSkipped) "Your newspaper notice was waived, so ask for the next available hearing."
            else "Ask for a date at least 6 weeks after you filed, so the newspaper notice has time to run.",
            "Write down the date, time, judge, courtroom and building address.",
        )
        StepId.PUBLISH -> listOf(
            "Your Legal Notice must be printed once a week, 2 weeks in a row, in a newspaper sold in your county. Both runs must finish before your hearing.",
            "Create the notice below, then send it to the newspaper and ask them to file their Affidavit of Publication with the court.",
            "Not sure which paper? The Nebraska Press Association keeps a list and will help (${Court.NEWSPAPERS_PHONE}).",
        )
        StepId.PREPARE -> listOf(
            "Do these at least 2 days before your hearing.",
        )
        StepId.HEARING_DAY -> listOf(
            "Arrive 15 minutes early. Ask the clerk how to let the judge know you're there.",
            "When your case is called, say you're ready and would like to testify. You'll take an oath, then read your script.",
            "It usually takes under 10 minutes. You can bring people for support.",
        )
        StepId.COPIES -> listOf(
            "After the judge signs, get certified copies of your Decree from the clerk of the district court.",
            "Agencies like Social Security and the DMV only accept certified copies, not photocopies.",
            "Get one for each place that might keep it. ${recommendedCopies(c)} is a good number for most people.",
        )
        StepId.UPDATE -> listOf(
            "Do Social Security first. Most other places check against it.",
            "Your Nebraska driver's license or state ID must be updated within 60 days.",
        )
    }

    fun recommendedCopies(c: CaseData) = 3

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
        ChecklistItem("u_dmv", "Driver's license or state ID, in person, within 60 days", Link("Nebraska DMV: name change", "https://dmv.nebraska.gov/dl/name-change")),
        ChecklistItem("u_passport", "Passport (if you have one)", Link("State Dept: change your name", "https://travel.state.gov/content/travel/en/passports/have-passport/change-correct.html")),
        ChecklistItem("u_birth", "Birth certificate. Born in Nebraska: send a certified decree to DHHS Vital Records (402-471-2872). Born elsewhere: that state's office.", Link("Nebraska DHHS Vital Records", "https://dhhs.ne.gov/Pages/Vital-Records.aspx")),
        ChecklistItem("u_vote", "Voter registration", Link("Nebraska voter registration", "https://www.nebraska.gov/apps-sos-voter-registration/")),
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
