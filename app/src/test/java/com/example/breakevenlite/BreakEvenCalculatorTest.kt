package com.example.breakevenlite

import com.example.breakevenlite.data.model.BreakEvenInput
import com.example.breakevenlite.data.model.BreakEvenStatus
import com.example.breakevenlite.domain.calculator.BreakEvenCalculator
import com.example.breakevenlite.domain.calculator.BreakEvenValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class BreakEvenCalculatorTest {

    // 1. Basic break-even
    @Test
    fun testBasicBreakEven() {
        val input = BreakEvenInput(
            fixedCosts = BigDecimal("10000"),
            sellingPricePerUnit = BigDecimal("100"),
            variableCostPerUnit = BigDecimal("60")
        )
        val result = BreakEvenCalculator.calculate(input)

        assertEquals(BreakEvenStatus.BREAK_EVEN_AVAILABLE, result.status)
        assertEquals(BigDecimal("40"), result.contributionMarginPerUnit)
        assertEquals(BigInteger.valueOf(250), result.breakEvenUnits)
        assertEquals(BigDecimal("25000"), result.breakEvenSales)
        assertEquals(BigDecimal("15000"), result.variableCostAtBreakEven)
        assertEquals(BigDecimal("25000"), result.totalCostAtBreakEven)
        assertEquals(BigDecimal("0"), result.profitAtBreakEven)
    }

    // 2. Exact break-even calculation
    @Test
    fun testExactBreakEven() {
        val fixed = BigDecimal("10000")
        val contrib = BigDecimal("40")
        val exact = BreakEvenCalculator.calculateExactBreakEvenUnits(fixed, contrib)
        assertEquals(BigDecimal("250"), exact)
    }

    // 3. Non-whole break-even (ceiling check)
    @Test
    fun testNonWholeBreakEvenCeiling() {
        val input = BreakEvenInput(
            fixedCosts = BigDecimal("10001"),
            sellingPricePerUnit = BigDecimal("100"),
            variableCostPerUnit = BigDecimal("60")
        )
        val result = BreakEvenCalculator.calculate(input)

        assertEquals(BigInteger.valueOf(251), result.breakEvenUnits)
        assertEquals(BigDecimal("250.025"), result.exactBreakEvenUnits)
        // 251 * 40 = 10,040 contribution covers 10,001 fixed costs
        assertTrue(result.profitAtBreakEven!! > BigDecimal.ZERO)
        assertEquals(BigDecimal("39"), result.profitAtBreakEven) // (251*100) - 10001 - (251*60) = 25100 - 10001 - 15060 = 39
    }

    // 4. Ceiling behavior: 250.0001 -> 251
    @Test
    fun testCeilingEdgeCase() {
        val fixed = BigDecimal("10000.01")
        val contrib = BigDecimal("40")
        val units = BreakEvenCalculator.calculateBreakEvenUnits(fixed, contrib)
        assertEquals(BigInteger.valueOf(251), units)
    }

    // 5. Zero fixed costs
    @Test
    fun testZeroFixedCosts() {
        val input = BreakEvenInput(
            fixedCosts = BigDecimal.ZERO,
            sellingPricePerUnit = BigDecimal("100"),
            variableCostPerUnit = BigDecimal("60")
        )
        val result = BreakEvenCalculator.calculate(input)

        assertEquals(BreakEvenStatus.BREAK_EVEN_AVAILABLE, result.status)
        assertEquals(BigInteger.ZERO, result.breakEvenUnits)
        assertEquals(BigDecimal.ZERO, result.breakEvenSales)
        assertEquals("All fixed costs are already covered.", result.statusMessage)
    }

    // 6. Zero contribution (selling == variable)
    @Test
    fun testZeroContribution() {
        val input = BreakEvenInput(
            fixedCosts = BigDecimal("10000"),
            sellingPricePerUnit = BigDecimal("100"),
            variableCostPerUnit = BigDecimal("100")
        )
        val result = BreakEvenCalculator.calculate(input)

        assertEquals(BreakEvenStatus.NO_BREAK_EVEN, result.status)
        assertEquals(BigDecimal.ZERO, result.contributionMarginPerUnit)
        assertNull(result.breakEvenUnits)
        assertNull(result.breakEvenSales)
    }

    // 7. Negative contribution (variable > selling)
    @Test
    fun testNegativeContribution() {
        val input = BreakEvenInput(
            fixedCosts = BigDecimal("10000"),
            sellingPricePerUnit = BigDecimal("100"),
            variableCostPerUnit = BigDecimal("120")
        )
        val result = BreakEvenCalculator.calculate(input)

        assertEquals(BreakEvenStatus.NEGATIVE_CONTRIBUTION, result.status)
        assertEquals(BigDecimal("-20"), result.contributionMarginPerUnit)
        assertNull(result.breakEvenUnits)
        assertNull(result.breakEvenSales)
    }

    // 8. Contribution margin formula
    @Test
    fun testContributionMargin() {
        val cm = BreakEvenCalculator.calculateContributionMargin(
            sellingPricePerUnit = BigDecimal("250.50"),
            variableCostPerUnit = BigDecimal("150.25")
        )
        assertEquals(BigDecimal("100.25"), cm)
    }

    // 9. Contribution percentage
    @Test
    fun testContributionPercentage() {
        val margin = BigDecimal("40")
        val price = BigDecimal("100")
        val percent = BreakEvenCalculator.calculateContributionMarginPercent(margin, price)
        assertEquals(0, percent.compareTo(BigDecimal("40.000000")))
        val formatted = com.example.breakevenlite.domain.util.CurrencyFormatter.formatPercent(percent, 2)
        assertEquals("40.00%", formatted)
    }

    // 10. Break-even sales
    @Test
    fun testBreakEvenSales() {
        val sales = BreakEvenCalculator.calculateBreakEvenSales(
            units = BigInteger.valueOf(250),
            sellingPricePerUnit = BigDecimal("100")
        )
        assertEquals(BigDecimal("25000"), sales)
    }

    // 11. Variable cost at break-even
    @Test
    fun testVariableCostAtUnits() {
        val vc = BreakEvenCalculator.calculateVariableCostAtUnits(
            units = BigInteger.valueOf(250),
            variableCostPerUnit = BigDecimal("60")
        )
        assertEquals(BigDecimal("15000"), vc)
    }

    // 12. Total cost at break-even
    @Test
    fun testTotalCostAtUnits() {
        val total = BreakEvenCalculator.calculateTotalCostAtUnits(
            fixedCosts = BigDecimal("10000"),
            variableCostPerUnit = BigDecimal("60"),
            units = BigInteger.valueOf(250)
        )
        assertEquals(BigDecimal("25000"), total)
    }

    // 13. Profit at threshold
    @Test
    fun testProfitAtThreshold() {
        val profitExact = BreakEvenCalculator.calculateProfitAtUnits(
            sellingPricePerUnit = BigDecimal("100"),
            variableCostPerUnit = BigDecimal("60"),
            fixedCosts = BigDecimal("10000"),
            units = BigInteger.valueOf(250)
        )
        assertEquals(BigDecimal.ZERO, profitExact)
    }

    // 14. Target profit units
    @Test
    fun testTargetProfitUnits() {
        val units = BreakEvenCalculator.calculateTargetProfitUnits(
            fixedCosts = BigDecimal("10000"),
            targetProfit = BigDecimal("2000"),
            contributionMarginPerUnit = BigDecimal("40")
        )
        assertEquals(BigInteger.valueOf(300), units)
    }

    // 15. Target profit sales
    @Test
    fun testTargetProfitSales() {
        val sales = BreakEvenCalculator.calculateTargetProfitSales(
            units = BigInteger.valueOf(300),
            sellingPricePerUnit = BigDecimal("100")
        )
        assertEquals(BigDecimal("30000"), sales)
    }

    // 16. Zero target profit returns break-even units
    @Test
    fun testZeroTargetProfit() {
        val targetUnits = BreakEvenCalculator.calculateTargetProfitUnits(
            fixedCosts = BigDecimal("10000"),
            targetProfit = BigDecimal.ZERO,
            contributionMarginPerUnit = BigDecimal("40")
        )
        val beUnits = BreakEvenCalculator.calculateBreakEvenUnits(
            fixedCosts = BigDecimal("10000"),
            contributionMarginPerUnit = BigDecimal("40")
        )
        assertEquals(beUnits, targetUnits)
    }

    // 17. Decimal prices and fractional units
    @Test
    fun testDecimalPrices() {
        val input = BreakEvenInput(
            fixedCosts = BigDecimal("1500.50"),
            sellingPricePerUnit = BigDecimal("12.75"),
            variableCostPerUnit = BigDecimal("7.25")
        )
        val result = BreakEvenCalculator.calculate(input)
        assertEquals(BigDecimal("5.50"), result.contributionMarginPerUnit)
        // 1500.50 / 5.50 = 272.818... -> ceiling 273
        assertEquals(BigInteger.valueOf(273), result.breakEvenUnits)
        assertTrue(result.profitAtBreakEven!! >= BigDecimal.ZERO)
    }

    // 18. Large values
    @Test
    fun testLargeValues() {
        val input = BreakEvenInput(
            fixedCosts = BigDecimal("500000000000.00"),
            sellingPricePerUnit = BigDecimal("100000.00"),
            variableCostPerUnit = BigDecimal("50000.00")
        )
        val result = BreakEvenCalculator.calculate(input)
        assertEquals(BigInteger.valueOf(10000000), result.breakEvenUnits)
        assertNotNull(result.breakEvenSales)
    }

    // 19. BigDecimal precision (no floating point inaccuracy)
    @Test
    fun testBigDecimalPrecision() {
        val price = BigDecimal("0.3")
        val cost = BigDecimal("0.1")
        val margin = BreakEvenCalculator.calculateContributionMargin(price, cost)
        assertEquals(BigDecimal("0.2"), margin)
    }

    // 20. Invalid negative fixed cost validation
    @Test
    fun testValidateNegativeFixedCosts() {
        val err = BreakEvenValidator.validateFixedCosts("-500")
        assertEquals("Fixed costs cannot be negative.", err)
    }

    // 21. Invalid zero selling price validation
    @Test
    fun testValidateZeroSellingPrice() {
        val err = BreakEvenValidator.validateSellingPrice("0")
        assertEquals("Selling price must be greater than zero.", err)
    }

    // 22. Invalid negative variable cost validation
    @Test
    fun testValidateNegativeVariableCost() {
        val err = BreakEvenValidator.validateVariableCost("-10")
        assertEquals("Variable cost cannot be negative.", err)
    }
}
