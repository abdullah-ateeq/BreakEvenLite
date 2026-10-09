package com.example.breakevenlite.domain.calculator

import com.example.breakevenlite.data.model.BreakEvenInput
import com.example.breakevenlite.data.model.BreakEvenResult
import com.example.breakevenlite.data.model.BreakEvenStatus
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object BreakEvenCalculator {

    val MAX_INPUT_VALUE: BigDecimal = BigDecimal("999999999999.99")

    fun calculateContributionMargin(
        sellingPricePerUnit: BigDecimal,
        variableCostPerUnit: BigDecimal
    ): BigDecimal {
        return sellingPricePerUnit.subtract(variableCostPerUnit)
    }

    fun calculateContributionMarginPercent(
        contributionMargin: BigDecimal,
        sellingPricePerUnit: BigDecimal
    ): BigDecimal {
        if (sellingPricePerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        return contributionMargin
            .divide(sellingPricePerUnit, 6, RoundingMode.HALF_UP)
            .multiply(BigDecimal("100"))
    }

    fun calculateBreakEvenUnits(
        fixedCosts: BigDecimal,
        contributionMarginPerUnit: BigDecimal
    ): BigInteger? {
        if (contributionMarginPerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            return null
        }
        if (fixedCosts.compareTo(BigDecimal.ZERO) == 0) {
            return BigInteger.ZERO
        }
        if (fixedCosts.compareTo(BigDecimal.ZERO) < 0) {
            return null
        }

        val rawCeil = fixedCosts.divide(contributionMarginPerUnit, 0, RoundingMode.CEILING).toBigInteger()
        var units = rawCeil
        while (BigDecimal(units).multiply(contributionMarginPerUnit) < fixedCosts) {
            units += BigInteger.ONE
        }
        while (units > BigInteger.ZERO && BigDecimal(units - BigInteger.ONE).multiply(contributionMarginPerUnit) >= fixedCosts) {
            units -= BigInteger.ONE
        }
        return units
    }

    fun calculateExactBreakEvenUnits(
        fixedCosts: BigDecimal,
        contributionMarginPerUnit: BigDecimal
    ): BigDecimal? {
        if (contributionMarginPerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            return null
        }
        if (fixedCosts.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        val divided = fixedCosts.divide(contributionMarginPerUnit, 6, RoundingMode.HALF_UP)
        val stripped = divided.stripTrailingZeros()
        return if (stripped.scale() < 0) stripped.setScale(0) else stripped
    }

    fun calculateBreakEvenSales(
        units: BigInteger,
        sellingPricePerUnit: BigDecimal
    ): BigDecimal {
        return BigDecimal(units).multiply(sellingPricePerUnit)
    }

    fun calculateVariableCostAtUnits(
        units: BigInteger,
        variableCostPerUnit: BigDecimal
    ): BigDecimal {
        return BigDecimal(units).multiply(variableCostPerUnit)
    }

    fun calculateTotalCostAtUnits(
        fixedCosts: BigDecimal,
        variableCostPerUnit: BigDecimal,
        units: BigInteger
    ): BigDecimal {
        return fixedCosts.add(calculateVariableCostAtUnits(units, variableCostPerUnit))
    }

    fun calculateSalesAtUnits(
        sellingPricePerUnit: BigDecimal,
        units: BigInteger
    ): BigDecimal {
        return BigDecimal(units).multiply(sellingPricePerUnit)
    }

    fun calculateProfitAtUnits(
        sellingPricePerUnit: BigDecimal,
        variableCostPerUnit: BigDecimal,
        fixedCosts: BigDecimal,
        units: BigInteger
    ): BigDecimal {
        val sales = calculateSalesAtUnits(sellingPricePerUnit, units)
        val totalCost = calculateTotalCostAtUnits(fixedCosts, variableCostPerUnit, units)
        return sales.subtract(totalCost)
    }

    fun calculateTargetProfitUnits(
        fixedCosts: BigDecimal,
        targetProfit: BigDecimal,
        contributionMarginPerUnit: BigDecimal
    ): BigInteger? {
        if (contributionMarginPerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            return null
        }
        if (targetProfit.compareTo(BigDecimal.ZERO) < 0 || fixedCosts.compareTo(BigDecimal.ZERO) < 0) {
            return null
        }
        val totalRequired = fixedCosts.add(targetProfit)
        if (totalRequired.compareTo(BigDecimal.ZERO) == 0) {
            return BigInteger.ZERO
        }
        val rawCeil = totalRequired.divide(contributionMarginPerUnit, 0, RoundingMode.CEILING).toBigInteger()
        var units = rawCeil
        while (BigDecimal(units).multiply(contributionMarginPerUnit) < totalRequired) {
            units += BigInteger.ONE
        }
        while (units > BigInteger.ZERO && BigDecimal(units - BigInteger.ONE).multiply(contributionMarginPerUnit) >= totalRequired) {
            units -= BigInteger.ONE
        }
        return units
    }

    fun calculateTargetProfitSales(
        units: BigInteger,
        sellingPricePerUnit: BigDecimal
    ): BigDecimal {
        return BigDecimal(units).multiply(sellingPricePerUnit)
    }

    fun determineStatus(contributionMargin: BigDecimal): BreakEvenStatus {
        return when {
            contributionMargin.compareTo(BigDecimal.ZERO) > 0 -> BreakEvenStatus.BREAK_EVEN_AVAILABLE
            contributionMargin.compareTo(BigDecimal.ZERO) == 0 -> BreakEvenStatus.NO_BREAK_EVEN
            else -> BreakEvenStatus.NEGATIVE_CONTRIBUTION
        }
    }

    fun calculate(input: BreakEvenInput): BreakEvenResult {
        val contributionMargin = calculateContributionMargin(
            sellingPricePerUnit = input.sellingPricePerUnit,
            variableCostPerUnit = input.variableCostPerUnit
        )

        val contributionPercent = calculateContributionMarginPercent(
            contributionMargin = contributionMargin,
            sellingPricePerUnit = input.sellingPricePerUnit
        )

        val status = determineStatus(contributionMargin)

        val statusMessage = when (status) {
            BreakEvenStatus.BREAK_EVEN_AVAILABLE -> {
                if (input.fixedCosts.compareTo(BigDecimal.ZERO) == 0) {
                    "All fixed costs are already covered."
                } else {
                    "Break-even threshold achievable."
                }
            }
            BreakEvenStatus.NO_BREAK_EVEN -> {
                "Variable cost must be lower than selling price to cover fixed costs."
            }
            BreakEvenStatus.NEGATIVE_CONTRIBUTION -> {
                "Each additional unit increases the loss by considering fixed costs."
            }
        }

        val exactUnits = calculateExactBreakEvenUnits(input.fixedCosts, contributionMargin)
        val breakEvenUnits = calculateBreakEvenUnits(input.fixedCosts, contributionMargin)

        val breakEvenSales = breakEvenUnits?.let {
            calculateBreakEvenSales(it, input.sellingPricePerUnit)
        }
        val variableCostAtBreakEven = breakEvenUnits?.let {
            calculateVariableCostAtUnits(it, input.variableCostPerUnit)
        }
        val totalCostAtBreakEven = breakEvenUnits?.let {
            calculateTotalCostAtUnits(input.fixedCosts, input.variableCostPerUnit, it)
        }
        val profitAtBreakEven = breakEvenUnits?.let {
            calculateProfitAtUnits(
                sellingPricePerUnit = input.sellingPricePerUnit,
                variableCostPerUnit = input.variableCostPerUnit,
                fixedCosts = input.fixedCosts,
                units = it
            )
        }

        var targetUnits: BigInteger? = null
        var targetSales: BigDecimal? = null
        var actualProfitAtTarget: BigDecimal? = null
        var additionalUnitsForTarget: BigInteger? = null

        if (input.targetProfitEnabled && input.targetProfit != null && input.targetProfit >= BigDecimal.ZERO) {
            targetUnits = calculateTargetProfitUnits(
                fixedCosts = input.fixedCosts,
                targetProfit = input.targetProfit,
                contributionMarginPerUnit = contributionMargin
            )
            targetSales = targetUnits?.let {
                calculateTargetProfitSales(it, input.sellingPricePerUnit)
            }
            actualProfitAtTarget = targetUnits?.let {
                calculateProfitAtUnits(
                    sellingPricePerUnit = input.sellingPricePerUnit,
                    variableCostPerUnit = input.variableCostPerUnit,
                    fixedCosts = input.fixedCosts,
                    units = it
                )
            }
            if (targetUnits != null && breakEvenUnits != null) {
                additionalUnitsForTarget = (targetUnits - breakEvenUnits).max(BigInteger.ZERO)
            }
        }

        return BreakEvenResult(
            status = status,
            contributionMarginPerUnit = contributionMargin,
            contributionMarginPercent = contributionPercent,
            exactBreakEvenUnits = exactUnits,
            breakEvenUnits = breakEvenUnits,
            breakEvenSales = breakEvenSales,
            variableCostAtBreakEven = variableCostAtBreakEven,
            totalCostAtBreakEven = totalCostAtBreakEven,
            profitAtBreakEven = profitAtBreakEven,
            targetProfitUnits = targetUnits,
            targetProfitSales = targetSales,
            actualProfitAtTarget = actualProfitAtTarget,
            additionalUnitsForTarget = additionalUnitsForTarget,
            statusMessage = statusMessage
        )
    }
}
