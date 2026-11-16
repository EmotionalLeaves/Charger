package com.emotionalleaves.charger

import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.logic.Dates
import com.emotionalleaves.charger.logic.Deadlines
import com.emotionalleaves.charger.logic.Guide
import com.emotionalleaves.charger.logic.StepId
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DeadlinesTest {
    private val filed = LocalDate.of(2026, 11, 16)

    @Test fun earliestHearingIsSixWeeks() {
        assertEquals(LocalDate.of(2026, 12, 28), Deadlines.earliestHearing(filed))
    }

    @Test fun hearingTooSoonIsAProblem() {
        val c = CaseData(filedDate = filed.toString(), hearingDate = filed.plusDays(14).toString())
        assertEquals(Deadlines.Level.PROBLEM, Deadlines.hearingCheck(c)!!.level)
    }

    @Test fun hearingSoonButWaivedIsOnlyWarn() {
        val c = CaseData(filedDate = filed.toString(), hearingDate = filed.plusDays(14).toString(), needsSafetyWaiver = true, waiverResult = "granted")
        assertEquals(Deadlines.Level.WARN, Deadlines.hearingCheck(c)!!.level)
    }

    @Test fun hearingSixWeeksOk() {
        val c = CaseData(filedDate = filed.toString(), hearingDate = filed.plusWeeks(7).toString())
        assertEquals(Deadlines.Level.OK, Deadlines.hearingCheck(c)!!.level)
    }

    @Test fun publicationMustBeConsecutiveWeeks() {
        val base = CaseData(hearingDate = "2027-01-20", pub1Date = "2026-12-01")
        assertEquals(Deadlines.Level.OK, Deadlines.publicationCheck(base.copy(pub2Date = "2026-12-08"))!!.level)
        assertEquals(Deadlines.Level.PROBLEM, Deadlines.publicationCheck(base.copy(pub2Date = "2026-12-15"))!!.level)
        assertEquals(Deadlines.Level.PROBLEM, Deadlines.publicationCheck(base.copy(pub2Date = "2026-11-30"))!!.level)
    }

    @Test fun secondRunAfterHearingIsAProblem() {
        val c = CaseData(hearingDate = "2026-12-05", pub1Date = "2026-12-01", pub2Date = "2026-12-08")
        assertEquals(Deadlines.Level.PROBLEM, Deadlines.publicationCheck(c)!!.level)
    }

    @Test fun mailingDeadlines() {
        assertEquals(LocalDate.of(2026, 12, 6), Deadlines.mailBy(LocalDate.of(2026, 12, 1)))
        assertEquals(LocalDate.of(2026, 12, 14), Deadlines.affidavitBy(LocalDate.of(2026, 12, 4), null))
        // Capped at the day before the hearing.
        assertEquals(LocalDate.of(2026, 12, 9), Deadlines.affidavitBy(LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 10)))
    }

    @Test fun ordinals() {
        assertEquals(listOf("1st", "2nd", "3rd", "4th", "11th", "12th", "13th", "21st", "22nd", "31st"),
            listOf(1, 2, 3, 4, 11, 12, 13, 21, 22, 31).map(Dates::ordinal))
    }

    @Test fun waiverGrantedSkipsPublishStep() {
        val c = CaseData(needsSafetyWaiver = true, waiverResult = "granted")
        assertFalse(StepId.PUBLISH in Guide.visibleSteps(c))
        assertTrue(StepId.PUBLISH in Guide.visibleSteps(CaseData()))
    }

    @Test fun nextStepFollowsDone() {
        assertEquals(StepId.ELIGIBLE, Guide.nextStep(CaseData()))
        assertEquals(StepId.DETAILS, Guide.nextStep(CaseData(done = setOf("ELIGIBLE"))))
    }

    @Test fun scriptUsesNames() {
        val c = CaseData(curFirst = "Ann", curLast = "Lee", newFirst = "Jo", newLast = "Lee", city = "Lincoln", county = "Lancaster", reason = "I go by Jo.")
        val s = Guide.hearingScript(c)
        assertTrue(s[0].contains("Ann Lee"))
        assertTrue(s[2].contains("Jo Lee"))
        assertTrue(s.any { it.contains("Affidavit of Publication") })
    }

    @Test fun timelineSorted() {
        val c = CaseData(filedDate = "2026-11-16", hearingDate = "2027-01-04", hearingTime = "09:00", pub1Date = "2026-12-03", pub2Date = "2026-12-10", hasInterestedParties = true)
        val t = Deadlines.timeline(c)
        assertEquals(t.map { it.date }.sorted(), t.map { it.date })
        assertTrue(t.any { it.title.startsWith("Mail") })
    }
}
