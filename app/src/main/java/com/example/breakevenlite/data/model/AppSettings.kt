package com.example.breakevenlite.data.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class HistorySortOption {
    RECENTLY_UPDATED,
    NAME_ASC,
    LOWEST_BREAK_EVEN,
    HIGHEST_CONTRIBUTION
}

enum class HistoryFilterOption {
    ALL,
    BREAK_EVEN_AVAILABLE,
    NO_BREAK_EVEN,
    TARGET_PROFIT
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultCurrency: String = "PKR",
    val defaultFixedCosts: String = "10000",
    val defaultSellingPrice: String = "100",
    val defaultVariableCost: String = "60",
    val defaultTargetProfitEnabled: Boolean = false,
    val defaultTargetProfitAmount: String = "2000",
    val decimalPlaces: Int = 2,
    val showContribution: Boolean = true,
    val showContributionPercentage: Boolean = true,
    val showCostStructure: Boolean = true,
    val showFormula: Boolean = true,
    val showTargetProfit: Boolean = true,
    val showBreakEvenSales: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true,
    val defaultHistorySort: HistorySortOption = HistorySortOption.RECENTLY_UPDATED
)
