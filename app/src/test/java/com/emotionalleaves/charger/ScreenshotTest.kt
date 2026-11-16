package com.emotionalleaves.charger

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.data.CaseViewModel
import com.emotionalleaves.charger.data.Store
import com.emotionalleaves.charger.logic.StepId
import com.emotionalleaves.charger.ui.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders each screen to build/shots for visual review. Run with -Pscreenshots=true. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h1600dp-xhdpi")
class ScreenshotTest {
    @get:Rule val rule = createComposeRule()

    private val app get() = ApplicationProvider.getApplicationContext<Application>()

    private val midway = CaseData(
        isAdult = true, livedOneYear = true, honestReason = true,
        curFirst = "Avery", curMiddle = "Quinn", curLast = "Sample", newFirst = "Rowan", newLast = "Sample",
        street = "123 Example St", zip = "68508", phone = "402-555-0100", email = "avery@example.com",
        birthDate = "2006-05-04", reason = "I have gone by Rowan for years.", askFeeWaiver = false, needsSafetyWaiver = false,
        filedDate = "2026-11-16", caseNumber = "CI 26-1234", hearingDate = "2027-01-04", hearingTime = "13:30",
        judge = "Example Judge", courtroom = "21", courthouseAddress = "575 S. 10th Street, Lincoln, NE 68508",
        newspaper = "Example Daily News", pub1Date = "2026-12-03", pub2Date = "2026-12-10", hasInterestedParties = false,
        done = setOf("ELIGIBLE", "DETAILS", "MONEY", "SAFETY", "FILE", "HEARING", "PUBLISH"),
    )

    @Before fun gate() { assumeTrue(System.getProperty("screenshots") == "true") }

    private fun shot(name: String, content: @Composable () -> Unit) {
        rule.setContent { ChargerTheme { content() } }
        rule.onRoot().captureRoboImage("build/shots/$name.png")
    }

    private fun vm(c: CaseData): CaseViewModel { Store.save(app, c); return CaseViewModel(app) }

    @Test fun homeEmpty() = shot("00-home-empty") { HomeScreen(CaseData(), {}, {}) }
    @Test fun homeMidway() = shot("01-home-midway") { HomeScreen(midway, {}, {}) }


    @Test fun stepEligible() = step(StepId.ELIGIBLE, CaseData())
    @Test fun stepDetails() = step(StepId.DETAILS, CaseData())
    @Test fun stepMoney() = step(StepId.MONEY, midway)
    @Test fun stepSafety() = step(StepId.SAFETY, midway)
    @Test fun stepFile() = step(StepId.FILE, midway)
    @Test fun stepHearing() = step(StepId.HEARING, midway)
    @Test fun stepPublish() = step(StepId.PUBLISH, midway.copy(hasInterestedParties = true, mailedDate = "2026-12-05"))
    @Test fun stepPrepare() = step(StepId.PREPARE, midway)
    @Test fun stepHearingDay() = step(StepId.HEARING_DAY, midway)
    @Test fun stepCopies() = step(StepId.COPIES, midway)
    @Test fun stepUpdate() = step(StepId.UPDATE, midway.copy(decreeDate = "2027-01-04"))
    @Test fun script() = shot("20-script") { ScriptScreen(midway) {} }
    @Test fun about() { val v = vm(midway); shot("21-about") { AboutScreen(v) {} } }

    private fun step(s: StepId, c: CaseData) {
        val v = vm(c)
        shot("1%02d-step-%s".format(s.ordinal, s.name.lowercase())) { StepScreen(s, c, v, {}, {}, {}) }
    }
}
