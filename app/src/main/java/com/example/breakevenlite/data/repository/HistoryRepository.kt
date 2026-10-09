package com.example.breakevenlite.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.breakevenlite.data.local.PreferenceKeys
import com.example.breakevenlite.data.local.RecordJsonAdapter
import com.example.breakevenlite.data.local.dataStore
import com.example.breakevenlite.data.model.CalculationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

class HistoryRepository(private val context: Context) {

    val historyFlow: Flow<List<CalculationRecord>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[PreferenceKeys.SAVED_HISTORY] ?: "[]"
            RecordJsonAdapter.jsonToList(json)
        }

    suspend fun saveCalculation(record: CalculationRecord) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferenceKeys.SAVED_HISTORY] ?: "[]"
            val currentList = RecordJsonAdapter.jsonToList(json).toMutableList()

            // Check if record exists
            val existingIndex = currentList.indexOfFirst { it.id == record.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = record.copy(timestamp = System.currentTimeMillis())
            } else {
                // Insert at beginning
                currentList.add(0, record)
            }

            // Enforce maximum 50 records. If > 50, remove oldest from end
            val trimmedList = if (currentList.size > 50) {
                currentList.take(50)
            } else {
                currentList
            }

            preferences[PreferenceKeys.SAVED_HISTORY] = RecordJsonAdapter.listToJson(trimmedList)
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferenceKeys.SAVED_HISTORY] ?: "[]"
            val currentList = RecordJsonAdapter.jsonToList(json).map { record ->
                if (record.id == id) {
                    record.copy(isFavorite = !record.isFavorite)
                } else {
                    record
                }
            }
            preferences[PreferenceKeys.SAVED_HISTORY] = RecordJsonAdapter.listToJson(currentList)
        }
    }

    suspend fun duplicateCalculation(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferenceKeys.SAVED_HISTORY] ?: "[]"
            val currentList = RecordJsonAdapter.jsonToList(json).toMutableList()
            val existing = currentList.find { it.id == id }
            if (existing != null) {
                val copyName = if (existing.scenarioName.isNotBlank()) {
                    "Copy of ${existing.scenarioName}"
                } else {
                    "Copy of Untitled Break-Even"
                }
                val duplicate = existing.copy(
                    id = UUID.randomUUID().toString(),
                    scenarioName = copyName.take(80),
                    timestamp = System.currentTimeMillis()
                )
                currentList.add(0, duplicate)
                val trimmedList = if (currentList.size > 50) currentList.take(50) else currentList
                preferences[PreferenceKeys.SAVED_HISTORY] = RecordJsonAdapter.listToJson(trimmedList)
            }
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferenceKeys.SAVED_HISTORY] ?: "[]"
            val currentList = RecordJsonAdapter.jsonToList(json).filterNot { it.id == id }
            preferences[PreferenceKeys.SAVED_HISTORY] = RecordJsonAdapter.listToJson(currentList)
        }
    }

    suspend fun clearAllHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SAVED_HISTORY] = "[]"
        }
    }
}
