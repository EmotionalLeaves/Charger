package com.emotionalleaves.charger

import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.logic.*
import org.junit.Assert.*
import org.junit.Test
import java.net.URI

class GuideTest {
    private val full = CaseData(
        isAdult = true, livedOneYear = true, honestReason = true,
        curFirst = "A", curLast = "B", newFirst = "C", newLast = "B", street = "1 St", city = "Lincoln", zip = "68508",
        phone = "1", email = "a@b.c", birthDate = "2006-01-01", reason = "r", askFeeWaiver = false, needsSafetyWaiver = false,
        filedDate = "2026-11-16", caseNumber = "CI 1", hearingDate = "2027-01-04", hearingTime = "09:00",
        newspaper = "N", pub1Date = "2026-12-03", pub2Date = "2026-12-10", hasInterestedParties = false, decreeDate = "2027-01-04",
        checked = setOf("make_decree", "read_script", "confirm_affidavit", "plan_trip", "pack"),
    )

    @Test fun completeCaseHasNothingMissing() {
        for (s in Guide.visibleSteps(full)) assertEquals("$s", emptyList<String>(), Guide.missing(s, full))
    }

    @Test fun emptyCaseNeedsAnswers() {
        val c = CaseData()
        for (s in listOf(StepId.ELIGIBLE, StepId.DETAILS, StepId.MONEY, StepId.SAFETY, StepId.FILE, StepId.HEARING, StepId.PUBLISH, StepId.PREPARE, StepId.HEARING_DAY))
            assertTrue("$s should need something", Guide.missing(s, c).isNotEmpty())
        assertTrue(Guide.missing(StepId.UPDATE, c).isEmpty())
    }

    @Test fun anyNoBlocksEligibility() {
        assertTrue(Guide.missing(StepId.ELIGIBLE, full.copy(livedOneYear = false)).isNotEmpty())
    }

    @Test fun noEmailNeedsReason() {
        val c = full.copy(noEmail = true, email = "")
        assertTrue(Guide.missing(StepId.DETAILS, c).any { "email" in it.lowercase() })
        assertTrue(Guide.missing(StepId.DETAILS, c.copy(noEmailReason = "No internet")).isEmpty())
    }

    @Test fun pendingWaiverBlocksHearingNotSafety() {
        val c = full.copy(needsSafetyWaiver = true, waiverResult = "")
        assertTrue(Guide.missing(StepId.SAFETY, c).isEmpty())
        assertTrue(Guide.missing(StepId.HEARING, c).isNotEmpty())
        assertTrue(Guide.missing(StepId.HEARING, c.copy(waiverResult = "denied")).isEmpty())
    }

    @Test fun interestedPartiesNeedAffidavit() {
        val c = full.copy(hasInterestedParties = true)
        assertTrue(Guide.missing(StepId.PUBLISH, c).isNotEmpty())
        assertTrue(Guide.missing(StepId.PUBLISH, c.copy(mailedDate = "2026-12-05", mailingAffidavitFiledDate = "2026-12-08")).isEmpty())
    }

    @Test fun stillNeededText() {
        assertNull(Guide.stillNeeded(emptyList()))
        assertEquals("Still needed: Phone", Guide.stillNeeded(listOf("Phone")))
        assertEquals("Still needed: Phone, and 2 more", Guide.stillNeeded(listOf("Phone", "Email", "Address")))
    }

    @Test fun allLinksAreHttps() {
        val links = Guide.sources + Guide.feeWaiverLinks + Guide.updateChecklist().mapNotNull { it.link } +
            listOf(Court.NEWSPAPERS, Court.PUBLIC_NOTICES, Court.CLERK_DIRECTORY, Court.FIND_LAWYER).map { Link("", it) }
        for (l in links) {
            val u = URI(l.url)
            assertEquals(l.url, "https", u.scheme)
            assertTrue(l.url, !u.host.isNullOrBlank())
        }
    }

    @Test fun affidavitIsAdultVersion() {
        val p = FormFields.plan(CourtForm.AFFIDAVIT_MAILING, full.copy(mailedDate = "2026-12-05"))
        assertEquals("Choice4", p.radios["Group27"])
        assertTrue("numberonenamechangeadult" in p.show)
        assertTrue("numberonedivorce" in p.hide)
        assertEquals("December 5, 2026", p.text["Text12"])
    }

    @Test fun countyLineUppercase() {
        assertEquals("LANCASTER COUNTY, NEBRASKA", FormFields.countyLine(CaseData(county = "Lancaster")))
        assertEquals("SCOTTS BLUFF COUNTY, NEBRASKA", FormFields.countyLine(CaseData(county = "Scotts Bluff")))
    }

    @Test fun allCountiesPresent() {
        assertEquals(93, Court.counties.size)
        assertEquals(Court.counties.sorted(), Court.counties.sortedWith(String.CASE_INSENSITIVE_ORDER).let { Court.counties.sorted() })
    }
}
