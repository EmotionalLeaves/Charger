package com.emotionalleaves.charger.logic

import com.emotionalleaves.charger.data.CaseData
import java.time.LocalDate

/**
 * Timing rules (Nebraska Judicial Branch adult name change guides, rev. 07/2026,
 * and Neb. Rev. Stat. § 25-21,271):
 *  - Hearing should be set about 6 weeks or more after filing.
 *  - Legal notice runs once a week for 2 weeks in a row, both before the hearing.
 *  - If anyone has a legal interest, mail them the first published notice within 5 days
 *    of the first run, then file the notarized Affidavit of Mailing within 10 days of mailing
 *    (and before the hearing).
 */
object Deadlines {
    const val HEARING_MIN_WEEKS = 6L
    const val MAIL_WITHIN_DAYS = 5L
    const val AFFIDAVIT_WITHIN_DAYS = 10L

    data class Item(val date: LocalDate, val title: String, val detail: String, val done: Boolean = false)

    enum class Level { OK, WARN, PROBLEM }
    data class Check(val level: Level, val message: String)

    fun earliestHearing(filed: LocalDate): LocalDate = filed.plusWeeks(HEARING_MIN_WEEKS)

    /** Latest sensible day for the first newspaper run: 2 runs a week apart, plus a week of slack
     *  for the newspaper's Affidavit of Publication to reach the court. */
    fun latestFirstRun(hearing: LocalDate): LocalDate = hearing.minusDays(21)

    fun mailBy(pub1: LocalDate): LocalDate = pub1.plusDays(MAIL_WITHIN_DAYS)
    fun affidavitBy(mailed: LocalDate, hearing: LocalDate?): LocalDate {
        val byRule = mailed.plusDays(AFFIDAVIT_WITHIN_DAYS)
        val dayBefore = hearing?.minusDays(1)
        return if (dayBefore != null && dayBefore.isBefore(byRule)) dayBefore else byRule
    }

    fun hearingCheck(c: CaseData): Check? {
        val filed = Dates.parse(c.filedDate) ?: return null
        val hearing = Dates.parse(c.hearingDate) ?: return null
        val days = Dates.daysBetween(filed, hearing)
        return when {
            days <= 0 -> Check(Level.PROBLEM, "The hearing date is not after your filing date. Check both dates.")
            days < 21 && !c.publicationSkipped -> Check(Level.PROBLEM, "Only $days days after filing. That isn't enough time for 2 weeks of newspaper notice. Ask the court for a later date.")
            days < HEARING_MIN_WEEKS * 7 -> Check(Level.WARN, "This is $days days after filing. The court suggests at least 6 weeks so everything fits. It may still work if you publish right away.")
            else -> Check(Level.OK, "Good: $days days after filing.")
        }
    }

    fun publicationCheck(c: CaseData): Check? {
        val p1 = Dates.parse(c.pub1Date) ?: return null
        val hearing = Dates.parse(c.hearingDate)
        val p2 = Dates.parse(c.pub2Date)
        if (p2 != null) {
            val gap = Dates.daysBetween(p1, p2)
            if (gap <= 0) return Check(Level.PROBLEM, "The 2nd run must be after the 1st run.")
            if (gap > 7) return Check(Level.PROBLEM, "The runs are $gap days apart. They must be in back-to-back weeks (2 weeks in a row).")
            if (gap < 5) return Check(Level.WARN, "The runs are only $gap days apart. It must be once a week, in 2 different weeks. Check with the newspaper.")
            if (hearing != null && !p2.isBefore(hearing)) return Check(Level.PROBLEM, "The 2nd run is not before your hearing. Both runs must finish before the hearing.")
        }
        if (hearing != null && p1.isAfter(latestFirstRun(hearing))) {
            return Check(Level.WARN, "This is close to your hearing. Make sure both runs finish, and the newspaper files its proof, before ${Dates.pretty(hearing)}.")
        }
        return Check(Level.OK, "Timing looks right.")
    }

    /** All upcoming / relevant dates, sorted. */
    fun timeline(c: CaseData): List<Item> {
        val out = mutableListOf<Item>()
        val filed = Dates.parse(c.filedDate)
        val hearing = Dates.parse(c.hearingDate)
        val p1 = Dates.parse(c.pub1Date)
        val p2 = Dates.parse(c.pub2Date)
        val mailed = Dates.parse(c.mailedDate)

        if (filed != null && hearing == null) {
            out += Item(earliestHearing(filed), "Earliest suggested hearing", "About 6 weeks after filing.")
        }
        if (hearing != null && !c.publicationSkipped) {
            if (p1 == null) out += Item(latestFirstRun(hearing), "Notice should be in the paper by now", "First of 2 weekly runs. Earlier is better.")
            else out += Item(p1, "Notice runs (week 1)", c.newspaper.ifBlank { "Newspaper" }, done = !p1.isAfter(LocalDate.now()))
            if (p2 != null) out += Item(p2, "Notice runs (week 2)", c.newspaper.ifBlank { "Newspaper" }, done = !p2.isAfter(LocalDate.now()))
        }
        if (p1 != null && c.hasInterestedParties == true) {
            out += Item(mailBy(p1), "Mail notice to interested parties", "Within 5 days of the first run.", done = mailed != null)
            if (mailed != null) {
                out += Item(affidavitBy(mailed, hearing), "File Affidavit of Mailing (notarized)", "Within 10 days of mailing, before the hearing.", done = c.mailingAffidavitFiledDate.isNotBlank())
            }
        }
        if (hearing != null) {
            if (!c.publicationSkipped) out += Item(hearing.minusDays(2), "Call the clerk", "Confirm the newspaper's Affidavit of Publication is filed.", done = "confirm_affidavit" in c.checked)
            out += Item(hearing, "Your hearing", listOf(c.hearingTime.let { Dates.parseTime(it)?.let(Dates::time) ?: "" }, c.courtroom.let { if (it.isBlank()) "" else "Courtroom $it" }).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "District Court" }, done = c.decreeDate.isNotBlank())
        }
        return out.sortedBy { it.date }
    }

    fun upcoming(c: CaseData, today: LocalDate = LocalDate.now()): List<Item> =
        timeline(c).filter { !it.done && !it.date.isBefore(today.minusDays(3)) }
}
