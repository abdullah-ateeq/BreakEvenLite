package com.example.breakevenlite.data.model

import java.util.UUID

data class CalculationRecord(
    val id: String = UUID.randomUUID().toString(),
    val scenarioName: String = "",
    val notes: String = "",
    val currency: String = "PKR",
    val fixedCosts: String = "0",
    val sellingPricePerUnit: String = "0",
    val variableCostPerUnit: String = "0",
    val targetProfitEnabled: Boolean = false,
    val targetProfit: String? = null,
    val breakEvenUnits: String? = null,
    val breakEvenSales: String? = null,
    val contributionMargin: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
