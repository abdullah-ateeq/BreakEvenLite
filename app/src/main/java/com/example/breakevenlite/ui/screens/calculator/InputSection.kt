package com.example.breakevenlite.ui.screens.calculator

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.breakevenlite.domain.util.CurrencyFormatter
import com.example.breakevenlite.ui.viewmodel.CalculatorUiState
import com.example.breakevenlite.ui.viewmodel.PresetType

@Composable
fun InputSection(
    uiState: CalculatorUiState,
    onScenarioNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onFixedCostsChange: (String) -> Unit,
    onSellingPriceChange: (String) -> Unit,
    onVariableCostChange: (String) -> Unit,
    onApplyPreset: (PresetType) -> Unit,
    modifier: Modifier = Modifier
) {
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
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
            // Preset Buttons Row
            Text(
                text = "Quick Examples",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { onApplyPreset(PresetType.SIMPLE) },
                    label = { Text("Simple (10k / 100 / 60)") }
                )
                FilterChip(
                    selected = false,
                    onClick = { onApplyPreset(PresetType.HEALTHY_MARGIN) },
                    label = { Text("Healthy Margin (50k / 500 / 300)") }
                )
                FilterChip(
                    selected = false,
                    onClick = { onApplyPreset(PresetType.LOW_CONTRIBUTION) },
                    label = { Text("Low Contribution (20k / 100 / 90)") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scenario Name & Currency Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = uiState.scenarioName,
                    onValueChange = onScenarioNameChange,
                    label = { Text("Scenario Name (Optional)") },
                    placeholder = { Text("e.g. Dining Chair") },
                    leadingIcon = {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Scenario or product name" }
                )

                Column {
                    OutlinedButton(
                        onClick = { currencyDropdownExpanded = true },
                        modifier = Modifier
                            .height(56.dp)
                            .semantics { contentDescription = "Display Currency ${uiState.currency}" }
                    ) {
                        Text(
                            text = uiState.currency,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        CurrencyFormatter.SUPPORTED_CURRENCIES.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr) },
                                onClick = {
                                    onCurrencyChange(curr)
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FIXED COSTS
            OutlinedTextField(
                value = uiState.fixedCostsInput,
                onValueChange = onFixedCostsChange,
                label = { Text("Fixed Costs (${uiState.currency})") },
                placeholder = { Text("e.g. 10000") },
                isError = uiState.fixedCostsError != null,
                supportingText = {
                    uiState.fixedCostsError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    } ?: Text("Rent, salaries, equipment, overhead (≥ 0)")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Fixed costs ${uiState.fixedCostsInput}" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SELLING PRICE PER UNIT
            OutlinedTextField(
                value = uiState.sellingPriceInput,
                onValueChange = onSellingPriceChange,
                label = { Text("Selling Price / Unit (${uiState.currency})") },
                placeholder = { Text("e.g. 100") },
                isError = uiState.sellingPriceError != null,
                supportingText = {
                    uiState.sellingPriceError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    } ?: Text("Price charged to customer per unit (> 0)")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Selling price per unit ${uiState.sellingPriceInput}" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // VARIABLE COST PER UNIT
            OutlinedTextField(
                value = uiState.variableCostInput,
                onValueChange = onVariableCostChange,
                label = { Text("Variable Cost / Unit (${uiState.currency})") },
                placeholder = { Text("e.g. 60") },
                isError = uiState.variableCostError != null,
                supportingText = {
                    uiState.variableCostError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    } ?: Text("Direct materials, packaging, labor per unit (≥ 0)")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Variable cost per unit ${uiState.variableCostInput}" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // NOTES (Optional)
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                label = { Text("Notes (Optional)") },
                placeholder = { Text("e.g. Monthly workshop overhead, seasonal rates") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Calculation notes" }
            )
        }
    }
}
