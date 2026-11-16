package com.emotionalleaves.charger.logic

/** Court and contact facts. Checked against official Nebraska Judicial Branch pages, Nov 2026. */
object Court {
    const val FILING_FEE = "\$144"
    const val FILING_FEE_NOTE = "Nebraska district court civil filing fee, effective July 1, 2026. Confirm with the clerk."

    data class Clerk(val name: String, val address: String, val phone: String, val mapQuery: String)

    val lancaster = Clerk(
        name = "Clerk of the District Court, Lancaster County",
        address = "575 S. 10th Street, Lincoln, NE 68508",
        phone = "402-441-7328",
        mapQuery = "575 S 10th St, Lincoln, NE 68508",
    )

    fun clerkFor(county: String): Clerk? = if (county.trim().equals("Lancaster", true)) lancaster else null

    const val CLERK_DIRECTORY = "https://nebraskajudicial.gov/directories/district-court-contacts"
    const val OFFICIAL_GUIDE = "https://nebraskajudicial.gov/self-help/name-change/adult-name-change"
    const val FORMS_PAGE = "https://supremecourt.nebraska.gov/administration/self-represented-litigant-resources/name-change-adult-updated-format"
    const val FEE_SCHEDULE = "https://nebraskajudicial.gov/rules/administrative-policies-schedules/filing-fees-and-court-costs"
    const val FEE_WAIVER_FORMS = "https://nebraskajudicial.gov/self-help/fee-waiver"
    const val STATUTE = "https://nebraskalegislature.gov/laws/statutes.php?statute=25-21,271"
    const val LEGAL_AID = "https://legalaidofnebraska.org/online-resources/a-guide-to-name-changes-in-nebraska-a-step-by-step-process.html"
    const val FIND_LAWYER = "https://nefindalawyer.com/"
    const val NEWSPAPERS = "https://www.nebpress.com"
    const val NEWSPAPERS_PHONE = "402-476-2851"
    const val PUBLIC_NOTICES = "https://www.nepublicnotices.com"

    val counties = listOf(
        "Adams", "Antelope", "Arthur", "Banner", "Blaine", "Boone", "Box Butte", "Boyd", "Brown", "Buffalo",
        "Burt", "Butler", "Cass", "Cedar", "Chase", "Cherry", "Cheyenne", "Clay", "Colfax", "Cuming", "Custer",
        "Dakota", "Dawes", "Dawson", "Deuel", "Dixon", "Dodge", "Douglas", "Dundy", "Fillmore", "Franklin",
        "Frontier", "Furnas", "Gage", "Garden", "Garfield", "Gosper", "Grant", "Greeley", "Hall", "Hamilton",
        "Harlan", "Hayes", "Hitchcock", "Holt", "Hooker", "Howard", "Jefferson", "Johnson", "Kearney", "Keith",
        "Keya Paha", "Kimball", "Knox", "Lancaster", "Lincoln", "Logan", "Loup", "Madison", "McPherson",
        "Merrick", "Morrill", "Nance", "Nemaha", "Nuckolls", "Otoe", "Pawnee", "Perkins", "Phelps", "Pierce",
        "Platte", "Polk", "Red Willow", "Richardson", "Rock", "Saline", "Sarpy", "Saunders", "Scotts Bluff",
        "Seward", "Sheridan", "Sherman", "Sioux", "Stanton", "Thayer", "Thomas", "Thurston", "Valley",
        "Washington", "Wayne", "Webster", "Wheeler", "York",
    )
}
