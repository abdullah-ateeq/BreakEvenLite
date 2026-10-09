package com.example.breakevenlite.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.sp
import com.example.breakevenlite.data.model.AppThemeMode
import com.example.breakevenlite.data.model.HistorySortOption
import com.example.breakevenlite.domain.util.CurrencyFormatter
import com.example.breakevenlite.ui.viewmodel.BreakEvenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BreakEvenViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var historySortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Customize theme, defaults, display sections, and behavior",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // APPEARANCE SECTION
        SettingsSectionHeader(title = "Appearance", icon = Icons.Default.Palette)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val modes = listOf(AppThemeMode.SYSTEM to "System", AppThemeMode.LIGHT to "Light", AppThemeMode.DARK to "Dark")
                    modes.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                            onClick = { viewModel.updateTheme(mode) },
                            selected = settings.themeMode == mode
                        ) {
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Color Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Color",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Match system wallpaper palette on Android 12+",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.dynamicColor,
                        onCheckedChange = viewModel::updateDynamicColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DEFAULTS SECTION
        SettingsSectionHeader(title = "Calculation Defaults", icon = Icons.Default.AttachMoney)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Default Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Default Currency",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Box {
                        OutlinedButton(onClick = { currencyMenuExpanded = true }) {
                            Text(settings.defaultCurrency)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = currencyMenuExpanded,
                            onDismissRequest = { currencyMenuExpanded = false }
                        ) {
                            CurrencyFormatter.SUPPORTED_CURRENCIES.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr) },
                                    onClick = {
                                        viewModel.updateDefaultCurrency(curr)
                                        currencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Default Inputs
                OutlinedTextField(
                    value = settings.defaultFixedCosts,
                    onValueChange = viewModel::updateDefaultFixedCosts,
                    label = { Text("Default Fixed Costs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = settings.defaultSellingPrice,
                    onValueChange = viewModel::updateDefaultSellingPrice,
                    label = { Text("Default Selling Price / Unit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = settings.defaultVariableCost,
                    onValueChange = viewModel::updateDefaultVariableCost,
                    label = { Text("Default Variable Cost / Unit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Decimal Places (0, 1, 2, 3, 4)
                Text(
                    text = "Decimal Places: ${settings.decimalPlaces}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val places = listOf(0, 1, 2, 3, 4)
                    places.forEachIndexed { index, place ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = places.size),
                            onClick = { viewModel.updateDecimalPlaces(place) },
                            selected = settings.decimalPlaces == place
                        ) {
                            Text(place.toString())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DISPLAY SECTIONS TOGGLES
        SettingsSectionHeader(title = "Display Sections", icon = Icons.Default.Visibility)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Show Contribution Breakdown",
                    subtitle = "Display selling price − variable cost and margin per unit",
                    checked = settings.showContribution,
                    onCheckedChange = viewModel::updateShowContribution
                )

                SettingsSwitchRow(
                    title = "Show Contribution Percentage",
                    subtitle = "Display margin as percentage of selling price",
                    checked = settings.showContributionPercentage,
                    onCheckedChange = viewModel::updateShowContributionPercentage
                )

                SettingsSwitchRow(
                    title = "Show Cost Structure Card",
                    subtitle = "Display fixed, variable, total cost and profit at threshold",
                    checked = settings.showCostStructure,
                    onCheckedChange = viewModel::updateShowCostStructure
                )

                SettingsSwitchRow(
                    title = "Show Break-Even Sales",
                    subtitle = "Display calculated total sales at break-even quantity",
                    checked = settings.showBreakEvenSales,
                    onCheckedChange = viewModel::updateShowBreakEvenSales
                )

                SettingsSwitchRow(
                    title = "Show Target Profit Section",
                    subtitle = "Include target profit calculation card",
                    checked = settings.showTargetProfit,
                    onCheckedChange = viewModel::updateShowTargetProfit
                )

                SettingsSwitchRow(
                    title = "Show Step-by-Step Formula",
                    subtitle = "Include dynamic mathematical steps breakdown",
                    checked = settings.showFormula,
                    onCheckedChange = viewModel::updateShowFormula
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BEHAVIOR & INTERACTION
        SettingsSectionHeader(title = "Interaction & History", icon = Icons.Default.Vibration)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on button clicks, presets, and saves",
                    checked = settings.hapticFeedback,
                    onCheckedChange = viewModel::updateHapticFeedback
                )

                SettingsSwitchRow(
                    title = "Confirm Before Deleting",
                    subtitle = "Show confirmation dialog when deleting history items",
                    checked = settings.confirmDelete,
                    onCheckedChange = viewModel::updateConfirmDelete
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Default History Sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default History Sort",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = when (settings.defaultHistorySort) {
                                HistorySortOption.RECENTLY_UPDATED -> "Recently Updated"
                                HistorySortOption.NAME_ASC -> "Name A–Z"
                                HistorySortOption.LOWEST_BREAK_EVEN -> "Lowest Break-Even"
                                HistorySortOption.HIGHEST_CONTRIBUTION -> "Highest Contribution"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box {
                        OutlinedButton(onClick = { historySortMenuExpanded = true }) {
                            Text("Change")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = historySortMenuExpanded,
                            onDismissRequest = { historySortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently Updated") },
                                onClick = {
                                    viewModel.updateDefaultHistorySort(HistorySortOption.RECENTLY_UPDATED)
                                    historySortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Name A–Z") },
                                onClick = {
                                    viewModel.updateDefaultHistorySort(HistorySortOption.NAME_ASC)
                                    historySortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Lowest Break-Even") },
                                onClick = {
                                    viewModel.updateDefaultHistorySort(HistorySortOption.LOWEST_BREAK_EVEN)
                                    historySortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest Contribution") },
                                onClick = {
                                    viewModel.updateDefaultHistorySort(HistorySortOption.HIGHEST_CONTRIBUTION)
                                    historySortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ACTION BUTTONS: RESET & CLEAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Reset all settings to default" },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Settings")
            }

            Button(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Clear all calculation history" },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear History")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Reset Settings Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all preferences back to factory defaults?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetSettings()
                    showResetDialog = false
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Permanently delete all saved calculations?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
