package com.example.breakevenlite.ui.screens.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breakevenlite.domain.util.CurrencyFormatter
import com.example.breakevenlite.ui.viewmodel.BreakEvenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(
    viewModel: BreakEvenViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.calculatorState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val fixedBigDecimal = CurrencyFormatter.parseCleanBigDecimal(uiState.fixedCostsInput)
    val sellingBigDecimal = CurrencyFormatter.parseCleanBigDecimal(uiState.sellingPriceInput)
    val variableBigDecimal = CurrencyFormatter.parseCleanBigDecimal(uiState.variableCostInput)

    fun performHaptic() {
        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        performHaptic()
    }

    fun shareResult(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share BreakEven Calculation")
        context.startActivity(shareIntent)
        performHaptic()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth > 680.dp

        if (isWide) {
            // Two-column layout for tablets / wide landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Inputs & Scenarios
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    CalculatorHeader()

                    Spacer(modifier = Modifier.height(16.dp))

                    InputSection(
                        uiState = uiState,
                        onScenarioNameChange = viewModel::onScenarioNameChange,
                        onNotesChange = viewModel::onNotesChange,
                        onCurrencyChange = viewModel::onCurrencyChange,
                        onFixedCostsChange = viewModel::onFixedCostsChange,
                        onSellingPriceChange = viewModel::onSellingPriceChange,
                        onVariableCostChange = viewModel::onVariableCostChange,
                        onApplyPreset = { preset ->
                            performHaptic()
                            viewModel.applyPreset(preset)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WhatIfPriceCard(
                        isExpanded = uiState.isWhatIfExpanded,
                        onToggleExpanded = viewModel::toggleWhatIfExpanded,
                        whatIfPriceInput = uiState.whatIfPriceInput,
                        onWhatIfPriceChange = viewModel::onWhatIfPriceChange,
                        whatIfResult = uiState.whatIfPriceResult,
                        currency = uiState.currency,
                        decimalPlaces = settings.decimalPlaces
                    )
                }

                // Right Column: Results, Costs, Target Profit, Actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    PrimaryResultCard(
                        result = uiState.calculationResult,
                        currency = uiState.currency,
                        decimalPlaces = settings.decimalPlaces,
                        onCopyBreakEven = {
                            val copyText = viewModel.getCopyBreakEvenString()
                            copyToClipboard(copyText, "Break-Even Quantity")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Break-even quantity copied")
                            }
                        }
                    )

                    if (settings.showContribution) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ContributionCard(
                            result = uiState.calculationResult,
                            sellingPrice = sellingBigDecimal,
                            variableCost = variableBigDecimal,
                            currency = uiState.currency,
                            decimalPlaces = settings.decimalPlaces,
                            showPercent = settings.showContributionPercentage
                        )
                    }

                    if (settings.showCostStructure) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CostStructureCard(
                            result = uiState.calculationResult,
                            fixedCosts = fixedBigDecimal,
                            currency = uiState.currency,
                            decimalPlaces = settings.decimalPlaces
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    BreakEvenFlowCard(
                        result = uiState.calculationResult,
                        fixedCosts = fixedBigDecimal,
                        currency = uiState.currency,
                        decimalPlaces = settings.decimalPlaces
                    )

                    if (settings.showTargetProfit) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TargetProfitCard(
                            enabled = uiState.targetProfitEnabled,
                            onToggleEnabled = viewModel::onTargetProfitToggle,
                            targetProfitInput = uiState.targetProfitInput,
                            onTargetProfitChange = viewModel::onTargetProfitAmountChange,
                            error = uiState.targetProfitError,
                            result = uiState.calculationResult,
                            currency = uiState.currency,
                            decimalPlaces = settings.decimalPlaces
                        )
                    }

                    if (settings.showFormula) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FormulaCard(
                            isExpanded = uiState.isFormulaExpanded,
                            onToggleExpanded = viewModel::toggleFormulaExpanded,
                            result = uiState.calculationResult,
                            fixedCosts = fixedBigDecimal,
                            sellingPrice = sellingBigDecimal,
                            variableCost = variableBigDecimal,
                            currency = uiState.currency,
                            decimalPlaces = settings.decimalPlaces
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ActionButtonsSection(
                        onCopyBreakEven = {
                            val copyText = viewModel.getCopyBreakEvenString()
                            copyToClipboard(copyText, "Break-Even Quantity")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Break-even quantity copied")
                            }
                        },
                        onCopyResult = {
                            val copyText = viewModel.getFormattedShareString(settings.decimalPlaces)
                            copyToClipboard(copyText, "Break-Even Result")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Break-even result copied")
                            }
                        },
                        onShare = {
                            val shareText = viewModel.getFormattedShareString(settings.decimalPlaces)
                            shareResult(shareText)
                        },
                        onSave = {
                            performHaptic()
                            viewModel.saveCalculation()
                        },
                        onReset = {
                            performHaptic()
                            viewModel.resetCalculator()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    DisclaimerFooter()
                }
            }
        } else {
            // Single-column layout for standard phone portrait
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                CalculatorHeader()

                Spacer(modifier = Modifier.height(16.dp))

                InputSection(
                    uiState = uiState,
                    onScenarioNameChange = viewModel::onScenarioNameChange,
                    onNotesChange = viewModel::onNotesChange,
                    onCurrencyChange = viewModel::onCurrencyChange,
                    onFixedCostsChange = viewModel::onFixedCostsChange,
                    onSellingPriceChange = viewModel::onSellingPriceChange,
                    onVariableCostChange = viewModel::onVariableCostChange,
                    onApplyPreset = { preset ->
                        performHaptic()
                        viewModel.applyPreset(preset)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryResultCard(
                    result = uiState.calculationResult,
                    currency = uiState.currency,
                    decimalPlaces = settings.decimalPlaces,
                    onCopyBreakEven = {
                        val copyText = viewModel.getCopyBreakEvenString()
                        copyToClipboard(copyText, "Break-Even Quantity")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Break-even quantity copied")
                        }
                    }
                )

                if (settings.showContribution) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ContributionCard(
                        result = uiState.calculationResult,
                        sellingPrice = sellingBigDecimal,
                        variableCost = variableBigDecimal,
                        currency = uiState.currency,
                        decimalPlaces = settings.decimalPlaces,
                        showPercent = settings.showContributionPercentage
                    )
                }

                if (settings.showCostStructure) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CostStructureCard(
                        result = uiState.calculationResult,
                        fixedCosts = fixedBigDecimal,
                        currency = uiState.currency,
                        decimalPlaces = settings.decimalPlaces
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BreakEvenFlowCard(
                    result = uiState.calculationResult,
                    fixedCosts = fixedBigDecimal,
                    currency = uiState.currency,
                    decimalPlaces = settings.decimalPlaces
                )

                if (settings.showTargetProfit) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TargetProfitCard(
                        enabled = uiState.targetProfitEnabled,
                        onToggleEnabled = viewModel::onTargetProfitToggle,
                        targetProfitInput = uiState.targetProfitInput,
                        onTargetProfitChange = viewModel::onTargetProfitAmountChange,
                        error = uiState.targetProfitError,
                        result = uiState.calculationResult,
                        currency = uiState.currency,
                        decimalPlaces = settings.decimalPlaces
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                WhatIfPriceCard(
                    isExpanded = uiState.isWhatIfExpanded,
                    onToggleExpanded = viewModel::toggleWhatIfExpanded,
                    whatIfPriceInput = uiState.whatIfPriceInput,
                    onWhatIfPriceChange = viewModel::onWhatIfPriceChange,
                    whatIfResult = uiState.whatIfPriceResult,
                    currency = uiState.currency,
                    decimalPlaces = settings.decimalPlaces
                )

                if (settings.showFormula) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FormulaCard(
                        isExpanded = uiState.isFormulaExpanded,
                        onToggleExpanded = viewModel::toggleFormulaExpanded,
                        result = uiState.calculationResult,
                        fixedCosts = fixedBigDecimal,
                        sellingPrice = sellingBigDecimal,
                        variableCost = variableBigDecimal,
                        currency = uiState.currency,
                        decimalPlaces = settings.decimalPlaces
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                ActionButtonsSection(
                    onCopyBreakEven = {
                        val copyText = viewModel.getCopyBreakEvenString()
                        copyToClipboard(copyText, "Break-Even Quantity")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Break-even quantity copied")
                        }
                    },
                    onCopyResult = {
                        val copyText = viewModel.getFormattedShareString(settings.decimalPlaces)
                        copyToClipboard(copyText, "Break-Even Result")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Break-even result copied")
                        }
                    },
                    onShare = {
                        val shareText = viewModel.getFormattedShareString(settings.decimalPlaces)
                        shareResult(shareText)
                    },
                    onSave = {
                        performHaptic()
                        viewModel.saveCalculation()
                    },
                    onReset = {
                        performHaptic()
                        viewModel.resetCalculator()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                DisclaimerFooter()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CalculatorHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "BREAKEVENLITE",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Calculate units required to cover fixed costs.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtonsSection(
    onCopyBreakEven: () -> Unit,
    onCopyResult: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main Primary Actions Row: Save & Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Save calculation" },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save")
            }

            FilledTonalButton(
                onClick = onShare,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Share result" },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }
        }

        // Secondary Copy Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCopyBreakEven,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .semantics { contentDescription = "Copy break-even quantity" },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Units", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = onCopyResult,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .semantics { contentDescription = "Copy full result" },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy All", fontSize = 13.sp)
            }
        }

        // Reset Button
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .semantics { contentDescription = "Reset calculator" },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Calculator", fontSize = 13.sp)
        }
    }
}

@Composable
private fun DisclaimerFooter() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "BreakEvenLite performs mathematical break-even calculations only. It does not provide accounting, financial forecasting, or business advice.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp)
        )
    }
}
