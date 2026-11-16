package com.emotionalleaves.charger.logic

import com.emotionalleaves.charger.data.CaseData

/** The official Nebraska fillable court forms bundled in assets/forms, and how your answers fill them. */
enum class CourtForm(val asset: String, val code: String, val title: String, val fileName: String) {
    PETITION("forms/petition.pdf", "DC 6:9.1", "Petition for Name Change", "1-Petition-for-Name-Change.pdf"),
    CONFIDENTIAL("forms/confidential.pdf", "DC 6:9.4", "Confidential Party Information", "2-Confidential-Party-Information.pdf"),
    WAIVER("forms/waiver.pdf", "DC 6:9.5", "Request for Waiver of Publication", "Request-for-Waiver-of-Publication.pdf"),
    NOTICE("forms/notice.pdf", "DC 6:9.2", "Legal Notice for Publication", "3-Legal-Notice.pdf"),
    AFFIDAVIT_MAILING("forms/affidavit_mailing.pdf", "DC 6:6.8", "Affidavit of Mailing Published Notice", "Affidavit-of-Mailing.pdf"),
    DECREE("forms/decree.pdf", "DC 6:9.3", "Decree of Name Change", "4-Decree-of-Name-Change.pdf"),
}

data class FillPlan(
    val text: Map<String, String>,
    val checks: Map<String, Boolean> = emptyMap(),
    /** Fields whose built-in placeholder text should be cleared. */
    val clear: List<String> = emptyList(),
    /** Radio groups: field name to export value (e.g. which case type). */
    val radios: Map<String, String> = emptyMap(),
    /** Fields the PDF normally shows/hides with scripts, which don't run when filled by the app. */
    val show: List<String> = emptyList(),
    val hide: List<String> = emptyList(),
)

object FormFields {
    private const val COURT_FIELD = "TYPEOFCOURTRESULTS"
    private const val COUNTY_FIELD = "fullcountystatementRIGHT"
    private const val COUNTY_HELPER = "enter the county"
    const val COURT_DROPDOWN = "TYPEOFCOURTDROPDOWN"
    const val COUNTY_DROPDOWN = "DROPDOWNCOUNTY2"

    fun countyLine(c: CaseData) = "${c.county.trim().uppercase()} COUNTY, NEBRASKA"
    fun courtLine() = "IN THE DISTRICT COURT OF"

    private fun header(c: CaseData) = mapOf(
        COURT_FIELD to courtLine(),
        COUNTY_FIELD to countyLine(c),
    )

    private fun currentName(c: CaseData) = mapOf(
        "current first name" to c.curFirst.trim(),
        "current middle names" to c.curMiddle.trim(),
        "current last names" to c.curLast.trim(),
    )

    private fun newName(c: CaseData) = mapOf(
        "new first name" to c.newFirst.trim(),
        "new middle names" to c.newMiddle.trim(),
        "new last names" to c.newLast.trim(),
    )

    private fun contactBlock(c: CaseData) = mapOf(
        "printedname" to c.currentName,
        "streetaddress" to c.street.trim(),
        "citystatezip" to c.cityStateZip,
        "telephone number" to c.phone.trim(),
        "emailaddress" to if (c.noEmail) "" else c.email.trim(),
    )

    fun plan(form: CourtForm, c: CaseData): FillPlan = when (form) {
        CourtForm.PETITION -> {
            val reason = c.reason.trim()
            FillPlan(
                text = header(c) + currentName(c) + newName(c) + contactBlock(c) + mapOf(
                    "case number assigned by Clerk of Court" to c.caseNumber.trim(),
                    "your full name" to c.currentName,
                    "your street address" to c.street.trim(),
                    "city where you live" to c.city.trim(),
                    "county where you live" to c.county.trim(),
                    "county where you live_2" to c.county.trim(),
                    "list here why you desire to change your name.0" to reason,
                    // Signature date is left blank: date it when you sign it.
                    "noemailreason" to if (c.noEmail) c.noEmailReason.trim() else "",
                ),
                checks = mapOf("Check Box7" to c.noEmail),
                clear = listOf(COUNTY_HELPER),
            )
        }
        CourtForm.CONFIDENTIAL -> FillPlan(
            text = header(c) + currentName(c) + mapOf(
                "case number assigned by Clerk of Court" to c.caseNumber.trim(),
                "your full name" to c.currentName,
                "undefined_3" to (Dates.parse(c.birthDate)?.let { Dates.long(it) } ?: c.birthDate),
            ),
            clear = listOf(COUNTY_HELPER),
        )
        CourtForm.NOTICE -> {
            val filed = Dates.parse(c.filedDate)
            val hearing = Dates.parse(c.hearingDate)
            val t = Dates.parseTime(c.hearingTime)
            val hour12 = t?.let { val h = it.hour % 12; if (h == 0) 12 else h }
            FillPlan(
                text = header(c) + currentName(c) + newName(c) + mapOf(
                    "case number assigned by Clerk of Court" to c.caseNumber.trim(),
                    "date" to (filed?.let { Dates.ordinal(it.dayOfMonth) } ?: ""),
                    "month" to (filed?.let { Dates.monthName(it) } ?: ""),
                    "Text3" to (filed?.let { "%02d".format(it.year % 100) } ?: ""),
                    "county" to c.county.trim(),
                    "name of judge hearing your case" to c.judge.trim(),
                    "courtroom number" to c.courtroom.trim(),
                    "courthouse address where" to c.courthouseAddress.trim(),
                    "date_2" to (hearing?.let { Dates.ordinal(it.dayOfMonth) } ?: ""),
                    "month_2" to (hearing?.let { Dates.monthName(it) } ?: ""),
                    "Text1" to (hearing?.let { "%02d".format(it.year % 100) } ?: ""),
                    "time of hearing" to (t?.let { "$hour12:%02d".format(it.minute) } ?: ""),
                    "Text2" to (t?.let { if (it.hour < 12) "a" else "p" } ?: ""),
                    "Petitioner print name" to c.currentName,
                    "Street address" to c.street.trim(),
                    "City, State, ZIP Code" to c.cityStateZip,
                    "Telephone number" to c.phone.trim(),
                ),
                clear = listOf(COUNTY_HELPER),
            )
        }
        CourtForm.DECREE -> FillPlan(
            text = header(c) + currentName(c) + newName(c) + mapOf(
                "Text5" to c.caseNumber.trim(),
                "your full name" to c.currentName,
                "Text7" to (Dates.parse(c.hearingDate)?.let { Dates.long(it) } ?: ""),
                "county1" to c.county.trim(),
                "Text17" to c.newspaper.trim(),
                // "Dated" and the judge's signature are filled in by the court.
            ),
            clear = listOf(COUNTY_HELPER),
        )
        CourtForm.WAIVER -> FillPlan(
            text = header(c) + contactBlock(c) + mapOf(
                "namechangepetitionersname" to c.currentName,
                "Text12" to c.caseNumber.trim(),
            ),
            checks = mapOf("checkboxadultname" to true, "checkboxminorname" to false),
            clear = listOf(COUNTY_HELPER),
        )
        CourtForm.AFFIDAVIT_MAILING -> FillPlan(
            text = mapOf(
                COUNTY_FIELD to countyLine(c),
                COURT_FIELD to courtLine(),
                "Text29" to c.caseNumber.trim(),
                // Name goes into the caption block itself (the separate name box overlaps its label).
                "namechangeadult" to "IN RE NAME CHANGE OF:\r\r${c.currentName.ifBlank { "__________________________________" }},\r         (your full name)                   Petitioner.",
                "Text8" to c.currentName,
                "Text10" to c.newspaper.trim(),
                "Text11" to (Dates.parse(c.pub1Date)?.let { Dates.long(it) } ?: ""),
                "datenamechange.0" to (Dates.parse(c.filedDate)?.let { Dates.long(it) } ?: ""),
                "Text12" to (Dates.parse(c.mailedDate)?.let { Dates.long(it) } ?: ""),
            ) + contactBlock(c),
            // Choice4 = "Name change for an Adult" on this multi-purpose form.
            radios = mapOf("Group27" to "Choice4"),
            show = listOf("namechangeadult", "numberonenamechangeadult", "datenamechange.0"),
            hide = listOf("adultnamechange", "plaintiffdefendant", "plaintiff", "defendant", "numberonedivorce", "datedivorce.0"),
        )
    }

    /** Fields every plan expects to exist in its PDF (used by tests to catch a revised court form). */
    fun expectedFieldNames(form: CourtForm): Set<String> {
        val p = plan(form, CaseData())
        return p.text.keys + p.checks.keys + p.clear + p.radios.keys + p.show + p.hide
    }
}
