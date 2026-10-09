package com.example.breakevenlite.domain.calculator

import java.math.BigDecimal

object BreakEvenValidator {

    val MAX_VALUE: BigDecimal = BigDecimal("999999999999.99")

    fun validateFixedCosts(input: String): String? {
        val trimmed = input.trim().replace(",", "")
        if (trimmed.isEmpty()) return "Fixed costs cannot be empty."
        val value = trimmed.toBigDecimalOrNull() ?: return "Enter a valid number."
        if (value < BigDecimal.ZERO) return "Fixed costs cannot be negative."
        if (value > MAX_VALUE) return "Value exceeds maximum allowed (999,999,999,999.99)."
        return null
    }

    fun validateSellingPrice(input: String): String? {
        val trimmed = input.trim().replace(",", "")
        if (trimmed.isEmpty()) return "Selling price cannot be empty."
        val value = trimmed.toBigDecimalOrNull() ?: return "Enter a valid number."
        if (value <= BigDecimal.ZERO) return "Selling price must be greater than zero."
        if (value > MAX_VALUE) return "Value exceeds maximum allowed (999,999,999,999.99)."
        return null
    }

    fun validateVariableCost(input: String): String? {
        val trimmed = input.trim().replace(",", "")
        if (trimmed.isEmpty()) return "Variable cost cannot be empty."
        val value = trimmed.toBigDecimalOrNull() ?: return "Enter a valid number."
        if (value < BigDecimal.ZERO) return "Variable cost cannot be negative."
        if (value > MAX_VALUE) return "Value exceeds maximum allowed (999,999,999,999.99)."
        return null
    }

    fun validateTargetProfit(input: String): String? {
        val trimmed = input.trim().replace(",", "")
        if (trimmed.isEmpty()) return null // allowed empty if disabled or optional
        val value = trimmed.toBigDecimalOrNull() ?: return "Enter a valid number."
        if (value < BigDecimal.ZERO) return "Target profit cannot be negative."
        if (value > MAX_VALUE) return "Value exceeds maximum allowed (999,999,999,999.99)."
        return null
    }
}
