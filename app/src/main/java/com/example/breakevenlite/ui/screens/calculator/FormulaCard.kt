package com.example.breakevenlite.ui.screens.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breakevenlite.data.model.BreakEvenResult
import com.example.breakevenlite.domain.util.CurrencyFormatter
import java.math.BigDecimal

@Composable
fun FormulaCard(
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    result: BreakEvenResult?,
    fixedCosts: BigDecimal?,
    sellingPrice: BigDecimal?,
    variableCost: BigDecimal?,
    currency: String,
    decimalPlaces: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "How it was calculated step by step formula" },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HOW IT WAS CALCULATED",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "View the step-by-step mathematical formulas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                if (result != null && fixedCosts != null && sellingPrice != null && variableCost != null) {
                    val fixedStr = CurrencyFormatter.formatMoney(fixedCosts, currency, decimalPlaces)
                    val sellingStr = CurrencyFormatter.formatMoney(sellingPrice, currency, decimalPlaces)
                    val varStr = CurrencyFormatter.formatMoney(variableCost, currency, decimalPlaces)
                    val contribStr = CurrencyFormatter.formatMoney(result.contributionMarginPerUnit, currency, decimalPlaces)
                    val contribPctStr = CurrencyFormatter.formatPercent(result.contributionMarginPercent, decimalPlaces)
                    val unitsStr = if (result.breakEvenUnits != null) CurrencyFormatter.formatUnits(result.breakEvenUnits) else "—"
                    val salesStr = CurrencyFormatter.formatMoney(result.breakEvenSales, currency, decimalPlaces)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {
                        FormulaStep(
                            stepNumber = "1",
                            title = "Contribution Margin per Unit",
                            formula = "Selling Price  −  Variable Cost  =  Contribution",
                            calculation = "$sellingStr  −  $varStr  =  $contribStr / unit"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FormulaStep(
                            stepNumber = "2",
                            title = "Contribution Margin Percentage",
                            formula = "(Contribution  ÷  Selling Price)  ×  100  =  Margin %",
                            calculation = "($contribStr  ÷  $sellingStr)  ×  100  =  $contribPctStr"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FormulaStep(
                            stepNumber = "3",
                            title = "Break-Even Required Units (Rounded Up)",
                            formula = "CEILING( Fixed Costs  ÷  Contribution Margin )",
                            calculation = if (result.breakEvenUnits != null) {
                                "CEILING( $fixedStr  ÷  $contribStr )  =  $unitsStr units"
                            } else {
                                "Variable cost exceeds or equals price; no finite break-even"
                            }
                        )

                        if (result.breakEvenSales != null) {
                            Spacer(modifier = Modifier.height(10.dp))

                            FormulaStep(
                                stepNumber = "4",
                                title = "Break-Even Sales Revenue",
                                formula = "Break-Even Units  ×  Selling Price  =  Sales",
                                calculation = "$unitsStr  ×  $sellingStr  =  $salesStr"
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Enter valid inputs above to see dynamic mathematical steps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaStep(
    stepNumber: String,
    title: String,
    formula: String,
    calculation: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Step $stepNumber: $title",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formula,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = calculation,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
