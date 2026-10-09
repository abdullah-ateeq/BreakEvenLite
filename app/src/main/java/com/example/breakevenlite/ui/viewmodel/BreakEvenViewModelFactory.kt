package com.example.breakevenlite.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.breakevenlite.data.repository.HistoryRepository
import com.example.breakevenlite.data.repository.SettingsRepository

class BreakEvenViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BreakEvenViewModel::class.java)) {
            val historyRepo = HistoryRepository(context.applicationContext)
            val settingsRepo = SettingsRepository(context.applicationContext)
            return BreakEvenViewModel(historyRepo, settingsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
