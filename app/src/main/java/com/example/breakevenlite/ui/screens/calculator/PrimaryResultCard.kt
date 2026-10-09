package com.example.breakevenlite.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breakevenlite.data.model.BreakEvenResult
import com.example.breakevenlite.data.model.BreakEvenStatus
import com.example.breakevenlite.domain.util.CurrencyFormatter
import com.example.breakevenlite.ui.theme.AmberContainerDark
import com.example.breakevenlite.ui.theme.AmberContainerLight
import com.example.breakevenlite.ui.theme.AmberWarning
import com.example.breakevenlite.ui.theme.NegativeContainerDark
import com.example.breakevenlite.ui.theme.NegativeContainerLight
import com.example.breakevenlite.ui.theme.NegativeRed
import java.math.BigDecimal
import java.math.BigInteger

@Composable
fun PrimaryResultCard(
    result: BreakEvenResult?,
    currency: String,
    decimalPlaces: Int,
    onCopyBreakEven: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (result == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter valid positive values to calculate break-even.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val isAvailable = result.status == BreakEvenStatus.BREAK_EVEN_AVAILABLE
    val isZeroContrib = result.status == BreakEvenStatus.NO_BREAK_EVEN
    val isNegativeContrib = result.status == BreakEvenStatus.NEGATIVE_CONTRIBUTION

    val statusContainerColor = when {
        isAvailable -> MaterialTheme.colorScheme.primaryContainer
        isZeroContrib -> AmberContainerLight
        else -> NegativeContainerLight
    }

    val statusContentColor = when {
        isAvailable -> MaterialTheme.colorScheme.onPrimaryContainer
        isZeroContrib -> AmberWarning
        else -> NegativeRed
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (result.breakEvenUnits != null) {
                    "Break-even requires ${result.breakEvenUnits} units"
                } else {
                    "No break-even available because variable cost is equal to or greater than selling price"
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // STATUS BADGE
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusContainerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isAvailable -> Icons.Default.CheckCircle
                            isZeroContrib -> Icons.Default.Warning
                            else -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = statusContentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isAvailable -> "BREAK-EVEN AVAILABLE"
                            isZeroContrib -> "NO BREAK-EVEN"
                            else -> "NEGATIVE CONTRIBUTION — NO BREAK-EVEN"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = statusContentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MAIN BREAK-EVEN QUANTITY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BREAK-EVEN",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isAvailable && result.breakEvenUnits != null) {
                        Text(
                            text = "${CurrencyFormatter.formatUnits(result.breakEvenUnits)} UNITS",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 34.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "NO BREAK-EVEN",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isNegativeContrib) NegativeRed else AmberWarning
                        )
                    }
                }

                if (isAvailable && result.breakEvenUnits != null) {
                    IconButton(
                        onClick = onCopyBreakEven,
                        modifier = Modifier.semantics { contentDescription = "Copy break-even quantity" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy break-even quantity",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Guidance
            val subtitleText = when {
                isAvailable && result.breakEvenUnits == BigInteger.ZERO ->
                    "All fixed costs are already covered."
                isAvailable && result.breakEvenUnits != null ->
                    "SELL ${CurrencyFormatter.formatUnits(result.breakEvenUnits)} UNITS TO COVER YOUR FIXED COSTS"
                isZeroContrib ->
                    "Variable cost must be lower than selling price to cover fixed costs."
                else -> {
                    val absLoss = result.contributionMarginPerUnit.abs()
                    val lossFormatted = CurrencyFormatter.formatMoney(absLoss, currency, decimalPlaces)
                    "Each additional unit increases the loss by $lossFormatted before considering fixed costs."
                }
            }

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )

            // EXACT VS REQUIRED WHOLE UNITS (if exact was a decimal fraction)
            if (isAvailable && result.exactBreakEvenUnits != null && result.breakEvenUnits != null) {
                val exactDecimal = result.exactBreakEvenUnits
                val wholeDecimal = BigDecimal(result.breakEvenUnits)

                if (exactDecimal.compareTo(wholeDecimal) != 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "EXACT CALCULATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${CurrencyFormatter.formatUnitsDecimal(exactDecimal, decimalPlaces)} units",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "REQUIRED WHOLE UNITS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${CurrencyFormatter.formatUnits(result.breakEvenUnits)} units (Rounded Up)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // BREAK-EVEN SALES SUMMARY ROW
            if (isAvailable && result.breakEvenSales != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BREAK-EVEN SALES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatMoney(result.breakEvenSales, currency, decimalPlaces),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
