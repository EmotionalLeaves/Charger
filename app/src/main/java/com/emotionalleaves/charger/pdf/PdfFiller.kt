package com.emotionalleaves.charger.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.emotionalleaves.charger.data.CaseData
import com.emotionalleaves.charger.logic.CourtForm
import com.emotionalleaves.charger.logic.FormFields
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckBox
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDRadioButton
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDTerminalField
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDChoice
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField
import java.io.File

/** Fills the bundled official court PDFs with your answers and saves a copy you can print or share. */
object PdfFiller {
    @Volatile private var ready = false

    private fun init(ctx: Context) {
        if (!ready) { PDFBoxResourceLoader.init(ctx.applicationContext); ready = true }
    }

    fun fill(ctx: Context, form: CourtForm, c: CaseData): File {
        init(ctx)
        val plan = FormFields.plan(form, c)
        val outDir = File(ctx.cacheDir, "forms").apply { mkdirs() }
        val out = File(outDir, form.fileName)
        ctx.assets.open(form.asset).use { input ->
            PDDocument.load(input).use { doc ->
                val acro = doc.documentCatalog.acroForm
                if (acro != null) {
                    acro.needAppearances = true
                    // Choice lists first: the court PDFs use them to drive the header text.
                    (acro.getField(FormFields.COURT_DROPDOWN) as? PDChoice)?.let { runCatching { it.setValue(FormFields.courtLine()) } }
                    (acro.getField(FormFields.COUNTY_DROPDOWN) as? PDChoice)?.let { runCatching { it.setValue(FormFields.countyLine(c)) } }
                    for ((name, value) in plan.text) {
                        val f = acro.getField(name) as? PDTextField ?: continue
                        runCatching { f.setValue(value) }
                    }
                    for (name in plan.clear) {
                        (acro.getField(name) as? PDTextField)?.let { runCatching { it.setValue("") } }
                    }
                    for ((name, v) in plan.radios) {
                        (acro.getField(name) as? PDRadioButton)?.let { runCatching { it.setValue(v) } }
                    }
                    for (name in plan.show) (acro.getField(name) as? PDTerminalField)?.widgets?.forEach { it.isHidden = false; it.isPrinted = true }
                    for (name in plan.hide) (acro.getField(name) as? PDTerminalField)?.widgets?.forEach { it.isHidden = true }
                    for ((name, on) in plan.checks) {
                        val f = acro.getField(name) as? PDCheckBox ?: continue
                        runCatching { if (on) f.check() else f.unCheck() }
                    }
                }
                doc.save(out)
            }
        }
        return out
    }

    fun blank(ctx: Context, form: CourtForm): File {
        val outDir = File(ctx.cacheDir, "forms").apply { mkdirs() }
        val out = File(outDir, "blank-" + form.fileName)
        ctx.assets.open(form.asset).use { i -> out.outputStream().use { o -> i.copyTo(o) } }
        return out
    }

    fun uri(ctx: Context, file: File): Uri =
        FileProvider.getUriForFile(ctx, ctx.packageName + ".files", file)

    /** Opens the PDF in the phone's PDF viewer (where you can print). */
    fun open(ctx: Context, file: File) {
        val u = uri(ctx, file)
        val view = Intent(Intent.ACTION_VIEW).setDataAndType(u, "application/pdf")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        try { ctx.startActivity(view) } catch (e: Exception) { share(ctx, file) }
    }

    fun share(ctx: Context, file: File) {
        val u = uri(ctx, file)
        val send = Intent(Intent.ACTION_SEND).setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, u)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ctx.startActivity(Intent.createChooser(send, "Send or save ${file.name}").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
