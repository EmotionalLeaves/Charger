package com.emotionalleaves.charger.data

import kotlinx.serialization.Serializable

/** Everything the app knows about your case. Stored only on this phone. */
@Serializable
data class CaseData(
    // Eligibility
    val isAdult: Boolean? = null,          // 19+, married, or emancipated
    val livedOneYear: Boolean? = null,     // lived in this county 1+ year
    val honestReason: Boolean? = null,     // not to avoid debt, crime or a lawsuit
    val county: String = "Lancaster",

    // About you
    val curFirst: String = "",
    val curMiddle: String = "",
    val curLast: String = "",
    val newFirst: String = "",
    val newMiddle: String = "",
    val newLast: String = "",
    val street: String = "",
    val city: String = "Lincoln",
    val zip: String = "",
    val phone: String = "",
    val email: String = "",
    val noEmail: Boolean = false,
    val noEmailReason: String = "",
    val birthDate: String = "",            // yyyy-MM-dd
    val reason: String = "",

    // Money
    val askFeeWaiver: Boolean? = null,

    // Safety / publication waiver
    val needsSafetyWaiver: Boolean? = null,
    val waiverResult: String = "",         // "", "granted", "denied"

    // Filing
    val filedDate: String = "",
    val caseNumber: String = "",

    // Hearing
    val hearingDate: String = "",
    val hearingTime: String = "",          // HH:mm (24h)
    val judge: String = "",
    val courtroom: String = "",
    val courthouseAddress: String = "",

    // Publication
    val newspaper: String = "",
    val pub1Date: String = "",
    val pub2Date: String = "",
    val hasInterestedParties: Boolean? = null,
    val mailedDate: String = "",
    val mailingAffidavitFiledDate: String = "",

    // Hearing result
    val decreeDate: String = "",

    // Progress
    val done: Set<String> = emptySet(),
    val checked: Set<String> = emptySet(),  // ticked checklist items
) {
    val currentName get() = listOf(curFirst, curMiddle, curLast).filter { it.isNotBlank() }.joinToString(" ")
    val newName get() = listOf(newFirst, newMiddle, newLast).filter { it.isNotBlank() }.joinToString(" ")
    val cityStateZip get() = listOf(city.trim(), "NE ${zip.trim()}".trim()).filter { it.isNotBlank() && it != "NE" }.joinToString(", ")
    val waiverPending get() = needsSafetyWaiver == true && waiverResult.isBlank()
    val publicationSkipped get() = needsSafetyWaiver == true && waiverResult == "granted"
}
