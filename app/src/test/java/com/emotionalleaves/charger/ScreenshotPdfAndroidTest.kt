package com.emotionalleaves.charger

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.logic.CourtForm
import com.emotionalleaves.charger.pdf.PdfFiller
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Runs the app's real Android PDF filler (pdfbox-android) on every form. Runs with -Pscreenshots=true. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScreenshotPdfAndroidTest {
    @Test fun fillsEveryFormOnAndroid() {
        assumeTrue(System.getProperty("screenshots") == "true")
        val app = ApplicationProvider.getApplicationContext<Application>()
        val c = CaseData(
            county = "Lancaster", curFirst = "Avery", curMiddle = "Quinn", curLast = "Sample", newFirst = "Rowan", newLast = "Sample",
            street = "123 Example St", city = "Lincoln", zip = "68508", phone = "402-555-0100", email = "avery@example.com",
            birthDate = "2006-05-04", reason = "I have used this name for years and want my legal name to match.",
            filedDate = "2026-11-16", caseNumber = "CI 26-1234", hearingDate = "2027-01-04", hearingTime = "13:30",
            judge = "Example Judge", courtroom = "21", courthouseAddress = "575 S. 10th Street, Lincoln, NE 68508",
            newspaper = "Example Daily News", pub1Date = "2026-12-03", mailedDate = "2026-12-05",
        )
        val out = File("build/android-forms").apply { mkdirs() }
        for (f in CourtForm.entries) {
            val file = PdfFiller.fill(app, f, c)
            assertTrue(file.length() > 10_000)
            file.copyTo(File(out, f.fileName), overwrite = true)
        }
    }
}
