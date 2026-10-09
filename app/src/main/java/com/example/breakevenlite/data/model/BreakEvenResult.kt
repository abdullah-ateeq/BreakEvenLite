package com.example.breakevenlite.data.model

import java.math.BigDecimal
import java.math.BigInteger

data class BreakEvenResult(
    val status: BreakEvenStatus,
    val contributionMarginPerUnit: BigDecimal,
    val contributionMarginPercent: BigDecimal,
    val exactBreakEvenUnits: BigDecimal?,
    val breakEvenUnits: BigInteger?,
    val breakEvenSales: BigDecimal?,
    val variableCostAtBreakEven: BigDecimal?,
    val totalCostAtBreakEven: BigDecimal?,
    val profitAtBreakEven: BigDecimal?,
    val targetProfitUnits: BigInteger? = null,
    val targetProfitSales: BigDecimal? = null,
    val actualProfitAtTarget: BigDecimal? = null,
    val additionalUnitsForTarget: BigInteger? = null,
    val statusMessage: String
)
