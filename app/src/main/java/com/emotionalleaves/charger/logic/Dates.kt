package com.emotionalleaves.charger.logic

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object Dates {
    fun parse(s: String): LocalDate? = try { if (s.isBlank()) null else LocalDate.parse(s) } catch (e: Exception) { null }
    fun parseTime(s: String): LocalTime? = try { if (s.isBlank()) null else LocalTime.parse(s) } catch (e: Exception) { null }

    private val pretty = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)
    private val long = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
    private val time = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    fun pretty(d: LocalDate) = pretty.format(d)
    fun long(d: LocalDate) = long.format(d)
    fun pretty(s: String) = parse(s)?.let { pretty(it) } ?: ""
    fun time(t: LocalTime) = time.format(t)

    fun monthName(d: LocalDate) = d.month.getDisplayName(TextStyle.FULL, Locale.US)

    /** "1st", "2nd", "16th" — the court notice reads "on the 16th day of November". */
    fun ordinal(n: Int): String {
        val suffix = if (n % 100 in 11..13) "th" else when (n % 10) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }
        return "$n$suffix"
    }

    fun daysBetween(a: LocalDate, b: LocalDate) = ChronoUnit.DAYS.between(a, b)

    fun relative(d: LocalDate, today: LocalDate): String {
        val n = daysBetween(today, d)
        return when {
            n == 0L -> "today"
            n == 1L -> "tomorrow"
            n == -1L -> "yesterday"
            n > 1 -> "in $n days"
            else -> "${-n} days ago"
        }
    }
}
