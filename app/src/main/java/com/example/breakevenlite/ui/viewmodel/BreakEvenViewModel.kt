package com.example.breakevenlite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breakevenlite.data.model.AppSettings
import com.example.breakevenlite.data.model.AppThemeMode
import com.example.breakevenlite.data.model.BreakEvenInput
import com.example.breakevenlite.data.model.BreakEvenResult
import com.example.breakevenlite.data.model.BreakEvenStatus
import com.example.breakevenlite.data.model.CalculationRecord
import com.example.breakevenlite.data.model.HistoryFilterOption
import com.example.breakevenlite.data.model.HistorySortOption
import com.example.breakevenlite.data.repository.HistoryRepository
import com.example.breakevenlite.data.repository.SettingsRepository
import com.example.breakevenlite.domain.calculator.BreakEvenCalculator
import com.example.breakevenlite.domain.calculator.BreakEvenValidator
import com.example.breakevenlite.domain.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger

data class CalculatorUiState(
    val scenarioName: String = "",
    val notes: String = "",
    val currency: String = "PKR",
    val fixedCostsInput: String = "10000",
    val sellingPriceInput: String = "100",
    val variableCostInput: String = "60",
    val targetProfitEnabled: Boolean = false,
    val targetProfitInput: String = "2000",
    val fixedCostsError: String? = null,
    val sellingPriceError: String? = null,
    val variableCostError: String? = null,
    val targetProfitError: String? = null,
    val calculationResult: BreakEvenResult? = null,
    val whatIfPriceInput: String = "",
    val whatIfPriceResult: BreakEvenResult? = null,
    val isWhatIfExpanded: Boolean = false,
    val isFormulaExpanded: Boolean = false,
    val snackbarMessage: String? = null
)

class BreakEvenViewModel(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _calculatorState = MutableStateFlow(CalculatorUiState())
    val calculatorState: StateFlow<CalculatorUiState> = _calculatorState.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilterOption.ALL)
    val historyFilter: StateFlow<HistoryFilterOption> = _historyFilter.asStateFlow()

    private val _historySort = MutableStateFlow(HistorySortOption.RECENTLY_UPDATED)
    val historySort: StateFlow<HistorySortOption> = _historySort.asStateFlow()

    private val _allHistory = historyRepository.historyFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredHistory: StateFlow<List<CalculationRecord>> = combine(
        _allHistory,
        _historySearchQuery,
        _historyFilter,
        _historySort
    ) { history, query, filter, sort ->
        var list = history

        // 1. Search Query Filter (Case-insensitive scenario name & notes)
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.scenarioName.lowercase().contains(q) || it.notes.lowercase().contains(q)
            }
        }

        // 2. Status / Feature Filter
        list = when (filter) {
            HistoryFilterOption.ALL -> list
            HistoryFilterOption.BREAK_EVEN_AVAILABLE -> list.filter {
                val sp = it.sellingPricePerUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val vc = it.variableCostPerUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
                sp > vc
            }
            HistoryFilterOption.NO_BREAK_EVEN -> list.filter {
                val sp = it.sellingPricePerUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val vc = it.variableCostPerUnit.toBigDecimalOrNull() ?: BigDecimal.ZERO
                sp <= vc
            }
            HistoryFilterOption.TARGET_PROFIT -> list.filter { it.targetProfitEnabled }
        }

        // 3. Sort: Favorites first, then requested order
        val (favorites, nonFavorites) = list.partition { it.isFavorite }

        fun sortSublist(sub: List<CalculationRecord>): List<CalculationRecord> {
            return when (sort) {
                HistorySortOption.RECENTLY_UPDATED -> sub.sortedByDescending { it.timestamp }
                HistorySortOption.NAME_ASC -> sub.sortedWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER) {
                        if (it.scenarioName.isNotBlank()) it.scenarioName else "Untitled Break-Even"
                    }
                )
                HistorySortOption.LOWEST_BREAK_EVEN -> sub.sortedBy {
                    it.breakEvenUnits?.toBigIntegerOrNull() ?: BigInteger.valueOf(Long.MAX_VALUE)
                }
                HistorySortOption.HIGHEST_CONTRIBUTION -> sub.sortedByDescending {
                    it.contributionMargin?.toBigDecimalOrNull() ?: BigDecimal("-999999999999")
                }
            }
        }

        sortSublist(favorites) + sortSublist(nonFavorites)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Initial recalculation with default inputs
        recalculate()
    }

    fun onScenarioNameChange(name: String) {
        _calculatorState.update { it.copy(scenarioName = name.take(80)) }
    }

    fun onNotesChange(notes: String) {
        _calculatorState.update { it.copy(notes = notes.take(500)) }
    }

    fun onCurrencyChange(currency: String) {
        _calculatorState.update { it.copy(currency = currency) }
        recalculate()
    }

    fun onFixedCostsChange(value: String) {
        val error = BreakEvenValidator.validateFixedCosts(value)
        _calculatorState.update { it.copy(fixedCostsInput = value, fixedCostsError = error) }
        recalculate()
    }

    fun onSellingPriceChange(value: String) {
        val error = BreakEvenValidator.validateSellingPrice(value)
        _calculatorState.update { it.copy(sellingPriceInput = value, sellingPriceError = error) }
        recalculate()
    }

    fun onVariableCostChange(value: String) {
        val error = BreakEvenValidator.validateVariableCost(value)
        _calculatorState.update { it.copy(variableCostInput = value, variableCostError = error) }
        recalculate()
    }

    fun onTargetProfitToggle(enabled: Boolean) {
        _calculatorState.update { it.copy(targetProfitEnabled = enabled) }
        recalculate()
    }

    fun onTargetProfitAmountChange(value: String) {
        val error = BreakEvenValidator.validateTargetProfit(value)
        _calculatorState.update { it.copy(targetProfitInput = value, targetProfitError = error) }
        recalculate()
    }

    fun onWhatIfPriceChange(value: String) {
        _calculatorState.update { it.copy(whatIfPriceInput = value) }
        recalculateWhatIf()
    }

    fun toggleWhatIfExpanded() {
        _calculatorState.update { it.copy(isWhatIfExpanded = !it.isWhatIfExpanded) }
    }

    fun toggleFormulaExpanded() {
        _calculatorState.update { it.copy(isFormulaExpanded = !it.isFormulaExpanded) }
    }

    fun clearSnackbar() {
        _calculatorState.update { it.copy(snackbarMessage = null) }
    }

    fun applyPreset(presetType: PresetType) {
        when (presetType) {
            PresetType.SIMPLE -> {
                _calculatorState.update {
                    it.copy(
                        fixedCostsInput = "10000",
                        sellingPriceInput = "100",
                        variableCostInput = "60",
                        fixedCostsError = null,
                        sellingPriceError = null,
                        variableCostError = null
                    )
                }
            }
            PresetType.HEALTHY_MARGIN -> {
                _calculatorState.update {
                    it.copy(
                        fixedCostsInput = "50000",
                        sellingPriceInput = "500",
                        variableCostInput = "300",
                        fixedCostsError = null,
                        sellingPriceError = null,
                        variableCostError = null
                    )
                }
            }
            PresetType.LOW_CONTRIBUTION -> {
                _calculatorState.update {
                    it.copy(
                        fixedCostsInput = "20000",
                        sellingPriceInput = "100",
                        variableCostInput = "90",
                        fixedCostsError = null,
                        sellingPriceError = null,
                        variableCostError = null
                    )
                }
            }
        }
        recalculate()
    }

    fun resetCalculator() {
        val currentSettings = settings.value
        _calculatorState.update {
            it.copy(
                scenarioName = "",
                notes = "",
                currency = currentSettings.defaultCurrency,
                fixedCostsInput = currentSettings.defaultFixedCosts,
                sellingPriceInput = currentSettings.defaultSellingPrice,
                variableCostInput = currentSettings.defaultVariableCost,
                targetProfitEnabled = currentSettings.defaultTargetProfitEnabled,
                targetProfitInput = currentSettings.defaultTargetProfitAmount,
                fixedCostsError = null,
                sellingPriceError = null,
                variableCostError = null,
                targetProfitError = null,
                whatIfPriceInput = "",
                whatIfPriceResult = null,
                isWhatIfExpanded = false,
                snackbarMessage = "Calculator reset to defaults"
            )
        }
        recalculate()
    }

    private fun recalculate() {
        val state = _calculatorState.value
        val fixedCosts = CurrencyFormatter.parseCleanBigDecimal(state.fixedCostsInput)
        val sellingPrice = CurrencyFormatter.parseCleanBigDecimal(state.sellingPriceInput)
        val variableCost = CurrencyFormatter.parseCleanBigDecimal(state.variableCostInput)
        val targetProfit = if (state.targetProfitEnabled) {
            CurrencyFormatter.parseCleanBigDecimal(state.targetProfitInput)
        } else {
            null
        }

        if (fixedCosts != null && sellingPrice != null && variableCost != null &&
            state.fixedCostsError == null && state.sellingPriceError == null && state.variableCostError == null
        ) {
            val input = BreakEvenInput(
                scenarioName = state.scenarioName,
                notes = state.notes,
                currency = state.currency,
                fixedCosts = fixedCosts,
                sellingPricePerUnit = sellingPrice,
                variableCostPerUnit = variableCost,
                targetProfitEnabled = state.targetProfitEnabled,
                targetProfit = targetProfit
            )
            val result = BreakEvenCalculator.calculate(input)
            _calculatorState.update { it.copy(calculationResult = result) }
        } else {
            _calculatorState.update { it.copy(calculationResult = null) }
        }

        recalculateWhatIf()
    }

    private fun recalculateWhatIf() {
        val state = _calculatorState.value
        val altPrice = CurrencyFormatter.parseCleanBigDecimal(state.whatIfPriceInput)
        val fixedCosts = CurrencyFormatter.parseCleanBigDecimal(state.fixedCostsInput)
        val variableCost = CurrencyFormatter.parseCleanBigDecimal(state.variableCostInput)

        if (altPrice != null && altPrice > BigDecimal.ZERO && fixedCosts != null && variableCost != null) {
            val whatIfInput = BreakEvenInput(
                currency = state.currency,
                fixedCosts = fixedCosts,
                sellingPricePerUnit = altPrice,
                variableCostPerUnit = variableCost,
                targetProfitEnabled = false
            )
            val result = BreakEvenCalculator.calculate(whatIfInput)
            _calculatorState.update { it.copy(whatIfPriceResult = result) }
        } else {
            _calculatorState.update { it.copy(whatIfPriceResult = null) }
        }
    }

    // Save calculation to history
    fun saveCalculation() {
        val state = _calculatorState.value
        val result = state.calculationResult ?: return

        val scenarioName = if (state.scenarioName.isNotBlank()) state.scenarioName else "Untitled Break-Even"

        val record = CalculationRecord(
            scenarioName = scenarioName,
            notes = state.notes,
            currency = state.currency,
            fixedCosts = state.fixedCostsInput,
            sellingPricePerUnit = state.sellingPriceInput,
            variableCostPerUnit = state.variableCostInput,
            targetProfitEnabled = state.targetProfitEnabled,
            targetProfit = if (state.targetProfitEnabled) state.targetProfitInput else null,
            breakEvenUnits = result.breakEvenUnits?.toString(),
            breakEvenSales = result.breakEvenSales?.toPlainString(),
            contributionMargin = result.contributionMarginPerUnit.toPlainString(),
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            historyRepository.saveCalculation(record)
            _calculatorState.update { it.copy(snackbarMessage = "Calculation saved to history") }
        }
    }

    // Restore calculation from history
    fun restoreCalculation(record: CalculationRecord) {
        _calculatorState.update {
            it.copy(
                scenarioName = record.scenarioName,
                notes = record.notes,
                currency = record.currency,
                fixedCostsInput = record.fixedCosts,
                sellingPriceInput = record.sellingPricePerUnit,
                variableCostInput = record.variableCostPerUnit,
                targetProfitEnabled = record.targetProfitEnabled,
                targetProfitInput = record.targetProfit ?: "2000",
                fixedCostsError = null,
                sellingPriceError = null,
                variableCostError = null,
                targetProfitError = null,
                snackbarMessage = "Loaded: ${if (record.scenarioName.isNotBlank()) record.scenarioName else "Untitled Break-Even"}"
            )
        }
        recalculate()
    }

    fun duplicateCalculation(id: String) {
        viewModelScope.launch {
            historyRepository.duplicateCalculation(id)
            _calculatorState.update { it.copy(snackbarMessage = "Duplicate created") }
        }
    }

    fun deleteCalculation(id: String) {
        viewModelScope.launch {
            historyRepository.deleteCalculation(id)
            _calculatorState.update { it.copy(snackbarMessage = "Calculation deleted") }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            _calculatorState.update { it.copy(snackbarMessage = "All history cleared") }
        }
    }

    fun onHistorySearchChange(query: String) {
        _historySearchQuery.value = query
    }

    fun onHistoryFilterChange(filter: HistoryFilterOption) {
        _historyFilter.value = filter
    }

    fun onHistorySortChange(sort: HistorySortOption) {
        _historySort.value = sort
    }

    // Settings actions
    fun updateTheme(themeMode: AppThemeMode) = viewModelScope.launch {
        settingsRepository.updateTheme(themeMode)
    }

    fun updateDynamicColor(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateDynamicColor(enabled)
    }

    fun updateDefaultCurrency(currency: String) = viewModelScope.launch {
        settingsRepository.updateDefaultCurrency(currency)
    }

    fun updateDefaultFixedCosts(value: String) = viewModelScope.launch {
        settingsRepository.updateDefaultFixedCosts(value)
    }

    fun updateDefaultSellingPrice(value: String) = viewModelScope.launch {
        settingsRepository.updateDefaultSellingPrice(value)
    }

    fun updateDefaultVariableCost(value: String) = viewModelScope.launch {
        settingsRepository.updateDefaultVariableCost(value)
    }

    fun updateDefaultTargetProfitEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateDefaultTargetProfitEnabled(enabled)
    }

    fun updateDefaultTargetProfitAmount(value: String) = viewModelScope.launch {
        settingsRepository.updateDefaultTargetProfitAmount(value)
    }

    fun updateDecimalPlaces(places: Int) = viewModelScope.launch {
        settingsRepository.updateDecimalPlaces(places)
    }

    fun updateShowContribution(show: Boolean) = viewModelScope.launch {
        settingsRepository.updateShowContribution(show)
    }

    fun updateShowContributionPercentage(show: Boolean) = viewModelScope.launch {
        settingsRepository.updateShowContributionPercentage(show)
    }

    fun updateShowCostStructure(show: Boolean) = viewModelScope.launch {
        settingsRepository.updateShowCostStructure(show)
    }

    fun updateShowFormula(show: Boolean) = viewModelScope.launch {
        settingsRepository.updateShowFormula(show)
    }

    fun updateShowTargetProfit(show: Boolean) = viewModelScope.launch {
        settingsRepository.updateShowTargetProfit(show)
    }

    fun updateShowBreakEvenSales(show: Boolean) = viewModelScope.launch {
        settingsRepository.updateShowBreakEvenSales(show)
    }

    fun updateHapticFeedback(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateHapticFeedback(enabled)
    }

    fun updateConfirmDelete(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateConfirmDelete(enabled)
    }

    fun updateDefaultHistorySort(sort: HistorySortOption) = viewModelScope.launch {
        settingsRepository.updateDefaultHistorySort(sort)
    }

    fun resetSettings() = viewModelScope.launch {
        settingsRepository.resetSettings()
        _calculatorState.update { it.copy(snackbarMessage = "Settings reset to defaults") }
    }

    fun getCopyBreakEvenString(): String {
        val result = _calculatorState.value.calculationResult ?: return ""
        return if (result.breakEvenUnits != null) {
            "${CurrencyFormatter.formatUnits(result.breakEvenUnits)} units"
        } else {
            "No break-even"
        }
    }

    fun getFormattedShareString(decimalPlaces: Int = 2): String {
        val state = _calculatorState.value
        val result = state.calculationResult ?: return ""
        val currency = state.currency

        val nameHeader = if (state.scenarioName.isNotBlank()) "${state.scenarioName}\n" else ""
        val fixedStr = CurrencyFormatter.formatMoney(CurrencyFormatter.parseCleanBigDecimal(state.fixedCostsInput), currency, decimalPlaces)
        val sellingStr = CurrencyFormatter.formatMoney(CurrencyFormatter.parseCleanBigDecimal(state.sellingPriceInput), currency, decimalPlaces)
        val varStr = CurrencyFormatter.formatMoney(CurrencyFormatter.parseCleanBigDecimal(state.variableCostInput), currency, decimalPlaces)
        val contribStr = CurrencyFormatter.formatMoney(result.contributionMarginPerUnit, currency, decimalPlaces)
        val contribPctStr = CurrencyFormatter.formatPercent(result.contributionMarginPercent, decimalPlaces)

        val breakEvenUnitsStr = if (result.breakEvenUnits != null) "${CurrencyFormatter.formatUnits(result.breakEvenUnits)} units" else "No break-even"
        val breakEvenSalesStr = CurrencyFormatter.formatMoney(result.breakEvenSales, currency, decimalPlaces)

        return buildString {
            append("BreakEvenLite\n")
            if (nameHeader.isNotBlank()) append(nameHeader)
            append("\nFixed Costs: $fixedStr")
            append("\nSelling Price / Unit: $sellingStr")
            append("\nVariable Cost / Unit: $varStr\n")
            append("\nContribution / Unit: $contribStr")
            append("\nContribution Margin: $contribPctStr\n")
            append("\nBreak-Even: $breakEvenUnitsStr")
            if (result.breakEvenSales != null) {
                append("\nBreak-Even Sales: $breakEvenSalesStr")
            }
            if (state.targetProfitEnabled && result.targetProfitUnits != null) {
                val tpUnitsStr = "${CurrencyFormatter.formatUnits(result.targetProfitUnits)} units"
                val tpSalesStr = CurrencyFormatter.formatMoney(result.targetProfitSales, currency, decimalPlaces)
                append("\nTarget Profit Units: $tpUnitsStr")
                append("\nTarget Sales: $tpSalesStr")
            }
            if (state.notes.isNotBlank()) {
                append("\n\nNotes: ${state.notes}")
            }
        }
    }
}

enum class PresetType {
    SIMPLE,
    HEALTHY_MARGIN,
    LOW_CONTRIBUTION
}
