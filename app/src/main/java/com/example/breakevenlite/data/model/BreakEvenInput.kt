package com.example.breakevenlite.data.model

import java.math.BigDecimal

data class BreakEvenInput(
    val scenarioName: String = "",
    val notes: String = "",
    val currency: String = "PKR",
    val fixedCosts: BigDecimal = BigDecimal.ZERO,
    val sellingPricePerUnit: BigDecimal = BigDecimal.ZERO,
    val variableCostPerUnit: BigDecimal = BigDecimal.ZERO,
    val targetProfitEnabled: Boolean = false,
    val targetProfit: BigDecimal? = null
)
