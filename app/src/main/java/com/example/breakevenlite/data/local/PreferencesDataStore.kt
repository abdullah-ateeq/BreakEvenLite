package com.example.breakevenlite.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "breakeven_lite_preferences")

object PreferenceKeys {
    val SAVED_HISTORY = stringPreferencesKey("saved_history")

    // Settings
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
    val DEFAULT_FIXED_COSTS = stringPreferencesKey("default_fixed_costs")
    val DEFAULT_SELLING_PRICE = stringPreferencesKey("default_selling_price")
    val DEFAULT_VARIABLE_COST = stringPreferencesKey("default_variable_cost")
    val DEFAULT_TARGET_PROFIT_ENABLED = booleanPreferencesKey("default_target_profit_enabled")
    val DEFAULT_TARGET_PROFIT_AMOUNT = stringPreferencesKey("default_target_profit_amount")
    val DECIMAL_PLACES = intPreferencesKey("decimal_places")

    val SHOW_CONTRIBUTION = booleanPreferencesKey("show_contribution")
    val SHOW_CONTRIBUTION_PERCENT = booleanPreferencesKey("show_contribution_percent")
    val SHOW_COST_STRUCTURE = booleanPreferencesKey("show_cost_structure")
    val SHOW_FORMULA = booleanPreferencesKey("show_formula")
    val SHOW_TARGET_PROFIT = booleanPreferencesKey("show_target_profit")
    val SHOW_BREAK_EVEN_SALES = booleanPreferencesKey("show_break_even_sales")

    val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
    val DEFAULT_HISTORY_SORT = stringPreferencesKey("default_history_sort")
}
