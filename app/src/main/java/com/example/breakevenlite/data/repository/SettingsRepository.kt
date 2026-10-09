package com.example.breakevenlite.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.breakevenlite.data.local.PreferenceKeys
import com.example.breakevenlite.data.local.dataStore
import com.example.breakevenlite.data.model.AppSettings
import com.example.breakevenlite.data.model.AppThemeMode
import com.example.breakevenlite.data.model.HistorySortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsRepository(private val context: Context) {

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeStr = preferences[PreferenceKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val themeMode = runCatching { AppThemeMode.valueOf(themeModeStr) }.getOrDefault(AppThemeMode.SYSTEM)

            val sortStr = preferences[PreferenceKeys.DEFAULT_HISTORY_SORT] ?: HistorySortOption.RECENTLY_UPDATED.name
            val defaultHistorySort = runCatching { HistorySortOption.valueOf(sortStr) }.getOrDefault(HistorySortOption.RECENTLY_UPDATED)

            AppSettings(
                themeMode = themeMode,
                dynamicColor = preferences[PreferenceKeys.DYNAMIC_COLOR] ?: true,
                defaultCurrency = preferences[PreferenceKeys.DEFAULT_CURRENCY] ?: "PKR",
                defaultFixedCosts = preferences[PreferenceKeys.DEFAULT_FIXED_COSTS] ?: "10000",
                defaultSellingPrice = preferences[PreferenceKeys.DEFAULT_SELLING_PRICE] ?: "100",
                defaultVariableCost = preferences[PreferenceKeys.DEFAULT_VARIABLE_COST] ?: "60",
                defaultTargetProfitEnabled = preferences[PreferenceKeys.DEFAULT_TARGET_PROFIT_ENABLED] ?: false,
                defaultTargetProfitAmount = preferences[PreferenceKeys.DEFAULT_TARGET_PROFIT_AMOUNT] ?: "2000",
                decimalPlaces = preferences[PreferenceKeys.DECIMAL_PLACES] ?: 2,
                showContribution = preferences[PreferenceKeys.SHOW_CONTRIBUTION] ?: true,
                showContributionPercentage = preferences[PreferenceKeys.SHOW_CONTRIBUTION_PERCENT] ?: true,
                showCostStructure = preferences[PreferenceKeys.SHOW_COST_STRUCTURE] ?: true,
                showFormula = preferences[PreferenceKeys.SHOW_FORMULA] ?: true,
                showTargetProfit = preferences[PreferenceKeys.SHOW_TARGET_PROFIT] ?: true,
                showBreakEvenSales = preferences[PreferenceKeys.SHOW_BREAK_EVEN_SALES] ?: true,
                hapticFeedback = preferences[PreferenceKeys.HAPTIC_FEEDBACK] ?: true,
                confirmDelete = preferences[PreferenceKeys.CONFIRM_DELETE] ?: true,
                defaultHistorySort = defaultHistorySort
            )
        }

    suspend fun updateTheme(themeMode: AppThemeMode) {
        context.dataStore.edit { it[PreferenceKeys.THEME_MODE] = themeMode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateDefaultCurrency(currency: String) {
        context.dataStore.edit { it[PreferenceKeys.DEFAULT_CURRENCY] = currency }
    }

    suspend fun updateDefaultFixedCosts(value: String) {
        context.dataStore.edit { it[PreferenceKeys.DEFAULT_FIXED_COSTS] = value }
    }

    suspend fun updateDefaultSellingPrice(value: String) {
        context.dataStore.edit { it[PreferenceKeys.DEFAULT_SELLING_PRICE] = value }
    }

    suspend fun updateDefaultVariableCost(value: String) {
        context.dataStore.edit { it[PreferenceKeys.DEFAULT_VARIABLE_COST] = value }
    }

    suspend fun updateDefaultTargetProfitEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.DEFAULT_TARGET_PROFIT_ENABLED] = enabled }
    }

    suspend fun updateDefaultTargetProfitAmount(value: String) {
        context.dataStore.edit { it[PreferenceKeys.DEFAULT_TARGET_PROFIT_AMOUNT] = value }
    }

    suspend fun updateDecimalPlaces(places: Int) {
        context.dataStore.edit { it[PreferenceKeys.DECIMAL_PLACES] = places }
    }

    suspend fun updateShowContribution(show: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SHOW_CONTRIBUTION] = show }
    }

    suspend fun updateShowContributionPercentage(show: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SHOW_CONTRIBUTION_PERCENT] = show }
    }

    suspend fun updateShowCostStructure(show: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SHOW_COST_STRUCTURE] = show }
    }

    suspend fun updateShowFormula(show: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SHOW_FORMULA] = show }
    }

    suspend fun updateShowTargetProfit(show: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SHOW_TARGET_PROFIT] = show }
    }

    suspend fun updateShowBreakEvenSales(show: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SHOW_BREAK_EVEN_SALES] = show }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun updateConfirmDelete(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.CONFIRM_DELETE] = enabled }
    }

    suspend fun updateDefaultHistorySort(sort: HistorySortOption) {
        context.dataStore.edit { it[PreferenceKeys.DEFAULT_HISTORY_SORT] = sort.name }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferenceKeys.THEME_MODE)
            preferences.remove(PreferenceKeys.DYNAMIC_COLOR)
            preferences.remove(PreferenceKeys.DEFAULT_CURRENCY)
            preferences.remove(PreferenceKeys.DEFAULT_FIXED_COSTS)
            preferences.remove(PreferenceKeys.DEFAULT_SELLING_PRICE)
            preferences.remove(PreferenceKeys.DEFAULT_VARIABLE_COST)
            preferences.remove(PreferenceKeys.DEFAULT_TARGET_PROFIT_ENABLED)
            preferences.remove(PreferenceKeys.DEFAULT_TARGET_PROFIT_AMOUNT)
            preferences.remove(PreferenceKeys.DECIMAL_PLACES)
            preferences.remove(PreferenceKeys.SHOW_CONTRIBUTION)
            preferences.remove(PreferenceKeys.SHOW_CONTRIBUTION_PERCENT)
            preferences.remove(PreferenceKeys.SHOW_COST_STRUCTURE)
            preferences.remove(PreferenceKeys.SHOW_FORMULA)
            preferences.remove(PreferenceKeys.SHOW_TARGET_PROFIT)
            preferences.remove(PreferenceKeys.SHOW_BREAK_EVEN_SALES)
            preferences.remove(PreferenceKeys.HAPTIC_FEEDBACK)
            preferences.remove(PreferenceKeys.CONFIRM_DELETE)
            preferences.remove(PreferenceKeys.DEFAULT_HISTORY_SORT)
        }
    }
}
