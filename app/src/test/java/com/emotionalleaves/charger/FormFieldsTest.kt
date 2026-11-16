package com.emotionalleaves.charger

import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.logic.CourtForm
import com.emotionalleaves.charger.logic.FormFields
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox
import org.apache.pdfbox.pdmodel.interactive.form.PDChoice
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Fills every bundled court form with sample answers using desktop PDFBox (same API as pdfbox-android),
 * checks every field the app writes really exists, and saves the result to build/test-forms for review.
 */
class FormFieldsTest {
    private val sample = CaseData(
        county = "Lancaster", curFirst = "Avery", curMiddle = "Quinn", curLast = "Sample",
        newFirst = "Rowan", newMiddle = "", newLast = "Sample",
        street = "123 Example St", city = "Lincoln", zip = "68508", phone = "402-555-0100", email = "avery@example.com",
        birthDate = "2006-05-04", reason = "I have gone by Rowan for years and want my legal name to match.",
        filedDate = "2026-11-16", caseNumber = "CI 26-1234", hearingDate = "2027-01-04", hearingTime = "13:30",
        judge = "Example Judge", courtroom = "21", courthouseAddress = "575 S. 10th Street, Lincoln, NE 68508",
        newspaper = "Example Daily News", mailedDate = "2026-12-05", pub1Date = "2026-12-03",
    )

    private fun assetFile(f: CourtForm): File {
        val candidates = listOf(File("src/main/assets/${f.asset}"), File("app/src/main/assets/${f.asset}"))
        return candidates.first { it.exists() }
    }

    @Test fun allFieldsExistAndFill() {
        val outDir = File("build/test-forms").apply { mkdirs() }
        for (form in CourtForm.entries) {
            PDDocument.load(assetFile(form)).use { doc ->
                val acro = doc.documentCatalog.acroForm
                assertNotNull("${form.code} has no form fields", acro)
                for (name in FormFields.expectedFieldNames(form)) {
                    assertNotNull("${form.code} is missing field '$name' (court may have revised the form)", acro.getField(name))
                }
                val plan = FormFields.plan(form, sample)
                (acro.getField(FormFields.COURT_DROPDOWN) as? PDChoice)?.setValue(FormFields.courtLine())
                (acro.getField(FormFields.COUNTY_DROPDOWN) as? PDChoice)?.setValue(FormFields.countyLine(sample))
                for ((k, v) in plan.text) (acro.getField(k) as PDTextField).setValue(v)
                for (k in plan.clear) (acro.getField(k) as PDTextField).setValue("")
                for ((k, v) in plan.radios) (acro.getField(k) as PDRadioButton).setValue(v)
                for (k in plan.show) (acro.getField(k) as PDTerminalField).widgets.forEach { it.isHidden = false; it.isPrinted = true }
                for (k in plan.hide) (acro.getField(k) as PDTerminalField).widgets.forEach { it.isHidden = true }
                for ((k, on) in plan.checks) (acro.getField(k) as PDCheckBox).let { if (on) it.check() else it.unCheck() }
                acro.needAppearances = true
                doc.save(File(outDir, form.fileName))
            }
        }
    }

    @Test fun noticeDateParts() {
        val p = FormFields.plan(CourtForm.NOTICE, sample).text
        assertEquals("16th", p["date"]); assertEquals("November", p["month"]); assertEquals("26", p["Text3"])
        assertEquals("4th", p["date_2"]); assertEquals("January", p["month_2"]); assertEquals("27", p["Text1"])
        assertEquals("1:30", p["time of hearing"]); assertEquals("p", p["Text2"])
    }
}
