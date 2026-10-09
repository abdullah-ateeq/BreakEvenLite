package com.example.breakevenlite.domain.util

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    val SUPPORTED_CURRENCIES = listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR", "Custom")

    fun formatMoney(
        amount: BigDecimal?,
        currency: String = "PKR",
        decimalPlaces: Int = 2
    ): String {
        if (amount == null) return "—"

        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append("#,##0")
            if (decimalPlaces > 0) {
                append(".")
                repeat(decimalPlaces) { append("0") }
            }
        }
        val df = DecimalFormat(pattern, symbols)
        val roundedAmount = amount.setScale(decimalPlaces, RoundingMode.HALF_UP)
        val formattedNumber = df.format(roundedAmount)

        return when (currency.trim().uppercase(Locale.US)) {
            "USD" -> "$formattedNumber USD"
            "EUR" -> "€$formattedNumber"
            "GBP" -> "£$formattedNumber"
            "AED" -> "AED $formattedNumber"
            "SAR" -> "SAR $formattedNumber"
            "INR" -> "₹$formattedNumber"
            "PKR" -> "PKR $formattedNumber"
            else -> {
                val cleanCurr = currency.trim()
                if (cleanCurr.isEmpty() || cleanCurr.equals("Custom", ignoreCase = true)) {
                    formattedNumber
                } else {
                    "$cleanCurr $formattedNumber"
                }
            }
        }
    }

    fun formatUnits(units: BigInteger?): String {
        if (units == null) return "—"
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0", symbols)
        return df.format(units)
    }

    fun formatUnitsDecimal(units: BigDecimal?, decimalPlaces: Int = 2): String {
        if (units == null) return "—"
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append("#,##0")
            if (decimalPlaces > 0) {
                append(".")
                repeat(decimalPlaces) { append("#") }
            }
        }
        val df = DecimalFormat(pattern, symbols)
        return df.format(units)
    }

    fun formatPercent(percent: BigDecimal?, decimalPlaces: Int = 2): String {
        if (percent == null) return "—"
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append("#,##0")
            if (decimalPlaces > 0) {
                append(".")
                repeat(decimalPlaces) { append("0") }
            }
        }
        val df = DecimalFormat(pattern, symbols)
        val rounded = percent.setScale(decimalPlaces, RoundingMode.HALF_UP)
        return "${df.format(rounded)}%"
    }

    fun parseCleanBigDecimal(text: String): BigDecimal? {
        val clean = text.trim().replace(",", "")
        return clean.toBigDecimalOrNull()
    }
}
