package com.emotionalleaves.charger.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

object Store {
    private const val PREFS = "charger"
    private const val KEY = "case_v1"
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    fun load(ctx: Context): CaseData {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return CaseData()
        return try { json.decodeFromString(CaseData.serializer(), raw) } catch (e: Exception) { CaseData() }
    }

    fun save(ctx: Context, c: CaseData) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, json.encodeToString(CaseData.serializer(), c)).apply()
    }
}

class CaseViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(Store.load(app))
    val state: StateFlow<CaseData> = _state.asStateFlow()

    fun update(f: (CaseData) -> CaseData) {
        val next = f(_state.value)
        _state.value = next
        Store.save(getApplication(), next)
    }

    fun setDone(step: String, done: Boolean) = update { it.copy(done = if (done) it.done + step else it.done - step) }
    fun toggleCheck(id: String) = update { it.copy(checked = if (id in it.checked) it.checked - id else it.checked + id) }
    fun reset() = update { CaseData() }
}
