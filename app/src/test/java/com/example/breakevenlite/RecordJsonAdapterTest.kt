package com.example.breakevenlite

import com.example.breakevenlite.data.local.RecordJsonAdapter
import com.example.breakevenlite.data.model.AppSettings
import com.example.breakevenlite.data.model.CalculationRecord
import com.example.breakevenlite.data.model.HistoryFilterOption
import com.example.breakevenlite.data.model.HistorySortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class RecordJsonAdapterTest {

    // 23. History persistence (Serialization / Deserialization of CalculationRecord)
    @Test
    fun testRecordJsonRoundTrip() {
        val record = CalculationRecord(
            id = "test-123",
            scenarioName = "Dining Chair",
            notes = "Special wood \"mahogany\" & nails\nSecond line",
            currency = "PKR",
            fixedCosts = "10000",
            sellingPricePerUnit = "100",
            variableCostPerUnit = "60",
            targetProfitEnabled = true,
            targetProfit = "2000",
            breakEvenUnits = "250",
            breakEvenSales = "25000",
            contributionMargin = "40",
            timestamp = 1700000000000L,
            isFavorite = true
        )

        val json = RecordJsonAdapter.recordToJson(record)
        val parsed = RecordJsonAdapter.jsonToRecord(json)

        assertNotNull(parsed)
        assertEquals(record.id, parsed!!.id)
        assertEquals(record.scenarioName, parsed.scenarioName)
        assertEquals(record.notes, parsed.notes)
        assertEquals(record.currency, parsed.currency)
        assertEquals(record.fixedCosts, parsed.fixedCosts)
        assertEquals(record.sellingPricePerUnit, parsed.sellingPricePerUnit)
        assertEquals(record.variableCostPerUnit, parsed.variableCostPerUnit)
        assertEquals(record.targetProfitEnabled, parsed.targetProfitEnabled)
        assertEquals(record.targetProfit, parsed.targetProfit)
        assertEquals(record.breakEvenUnits, parsed.breakEvenUnits)
        assertEquals(record.breakEvenSales, parsed.breakEvenSales)
        assertEquals(record.contributionMargin, parsed.contributionMargin)
        assertEquals(record.timestamp, parsed.timestamp)
        assertTrue(parsed.isFavorite)
    }

    @Test
    fun testListJsonRoundTrip() {
        val records = listOf(
            CalculationRecord(id = "1", scenarioName = "Project Alpha", isFavorite = true),
            CalculationRecord(id = "2", scenarioName = "Project Beta", isFavorite = false)
        )

        val json = RecordJsonAdapter.listToJson(records)
        val parsedList = RecordJsonAdapter.jsonToList(json)

        assertEquals(2, parsedList.size)
        assertEquals("Project Alpha", parsedList[0].scenarioName)
        assertTrue(parsedList[0].isFavorite)
        assertEquals("Project Beta", parsedList[1].scenarioName)
        assertFalse(parsedList[1].isFavorite)
    }

    // 24. Settings defaults verification
    @Test
    fun testSettingsDefaults() {
        val settings = AppSettings()
        assertEquals("PKR", settings.defaultCurrency)
        assertEquals("10000", settings.defaultFixedCosts)
        assertEquals("100", settings.defaultSellingPrice)
        assertEquals("60", settings.defaultVariableCost)
        assertEquals(2, settings.decimalPlaces)
        assertTrue(settings.showContribution)
        assertTrue(settings.showContributionPercentage)
        assertTrue(settings.showCostStructure)
        assertTrue(settings.showFormula)
        assertTrue(settings.showBreakEvenSales)
        assertTrue(settings.hapticFeedback)
        assertTrue(settings.confirmDelete)
        assertEquals(HistorySortOption.RECENTLY_UPDATED, settings.defaultHistorySort)
    }

    // 25. Favorites priority sorting
    @Test
    fun testFavoritesPrioritySorting() {
        val r1 = CalculationRecord(id = "1", scenarioName = "A", timestamp = 100, isFavorite = false)
        val r2 = CalculationRecord(id = "2", scenarioName = "B", timestamp = 200, isFavorite = true)
        val r3 = CalculationRecord(id = "3", scenarioName = "C", timestamp = 300, isFavorite = false)

        val list = listOf(r1, r2, r3)
        val (favs, nonFavs) = list.partition { it.isFavorite }
        val sorted = favs.sortedByDescending { it.timestamp } + nonFavs.sortedByDescending { it.timestamp }

        assertEquals("2", sorted[0].id) // Favorite first despite lower timestamp than r3
        assertEquals("3", sorted[1].id)
        assertEquals("1", sorted[2].id)
    }
}
