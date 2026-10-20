package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AmortizationSummary
import com.example.data.model.CurrencyRate
import com.example.data.model.FunnelSummary
import com.example.data.model.LtvMetrics
import com.example.data.model.MarginMarkupState
import com.example.data.model.PaymentFrequency
import com.example.data.model.PnlModel
import com.example.data.model.SavedDeal
import com.example.data.repository.AuthRepository
import com.example.data.repository.DealRepository
import com.example.domain.calculator.AmortizationEngine
import com.example.domain.calculator.CurrencyEngine
import com.example.domain.calculator.FunnelCalculator
import com.example.domain.calculator.LtvCalculator
import com.example.domain.calculator.MarginMarkupSolver
import com.example.domain.calculator.PnlRoiEngine
import com.example.service.PdfExportService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BigDealViewModel(
    private val dealRepository: DealRepository = DealRepository(),
    private val authRepository: AuthRepository
) : ViewModel() {

    // Auth & Cloud Sync
    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    val savedDeals: StateFlow<List<SavedDeal>> = dealRepository.deals
    val isSyncing: StateFlow<Boolean> = dealRepository.isSyncing

    // Toast / Event Bus
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // 1. Active Deal Metadata
    val dealTitle = MutableStateFlow("Enterprise Platform & Support Agreement")
    val clientName = MutableStateFlow("Globex International")
    val selectedCurrency = MutableStateFlow("USD")

    // 2. Financing & Amortization Modeler
    val principalAmount = MutableStateFlow("120000")
    val downPayment = MutableStateFlow("15000")
    val annualInterestRate = MutableStateFlow("6.5")
    val tenureMonths = MutableStateFlow("24")
    val paymentFrequency = MutableStateFlow(PaymentFrequency.MONTHLY)

    private val _amortization = MutableStateFlow(
        AmortizationEngine.calculate(
            principal = 120000.0,
            annualInterestRatePct = 6.5,
            tenureMonths = 24,
            downPayment = 15000.0,
            frequency = PaymentFrequency.MONTHLY
        )
    )
    val amortization: StateFlow<AmortizationSummary> = _amortization.asStateFlow()

    // Currency Cross-Rates
    val currencyRates = MutableStateFlow(CurrencyEngine.DEFAULT_CURRENCIES)
    val targetCurrency = MutableStateFlow("EUR")
    val convertedDealValue = MutableStateFlow(
        CurrencyEngine.convert(120000.0, "USD", "EUR")
    )
    val currentCrossRate = MutableStateFlow(
        CurrencyEngine.getCrossRate("USD", "EUR")
    )

    // 3. SaaS & Pipeline Economics (LTV, Churn, Funnel)
    val arpu = MutableStateFlow("2500")
    val grossMarginPct = MutableStateFlow("75")
    val churnRatePct = MutableStateFlow("2.5")
    val cac = MutableStateFlow("4000")

    private val _ltvMetrics = MutableStateFlow(
        LtvCalculator.calculate(2500.0, 75.0, 2.5, 4000.0)
    )
    val ltvMetrics: StateFlow<LtvMetrics> = _ltvMetrics.asStateFlow()

    // Funnel Conversions
    val funnelLeads = MutableStateFlow("1200")
    val funnelMqls = MutableStateFlow("450")
    val funnelSqls = MutableStateFlow("180")
    val funnelOpps = MutableStateFlow("60")
    val funnelClosedWon = MutableStateFlow("18")
    val avgDealValue = MutableStateFlow("35000")

    private val _funnelSummary = MutableStateFlow(
        FunnelCalculator.calculate(1200, 450, 180, 60, 18, 35000.0)
    )
    val funnelSummary: StateFlow<FunnelSummary> = _funnelSummary.asStateFlow()

    // 4. Margin & Markup Dynamics
    val marginCostInput = MutableStateFlow("1000")
    val marginPriceInput = MutableStateFlow("1600")
    private val _marginState = MutableStateFlow(
        MarginMarkupSolver.fromCostAndPrice(1000.0, 1600.0)
    )
    val marginState: StateFlow<MarginMarkupState> = _marginState.asStateFlow()

    // 5. Analytics (ROI & PnL)
    val pnlRevenue = MutableStateFlow("150000")
    val pnlCogs = MutableStateFlow("45000")
    val pnlOpex = MutableStateFlow("35000")
    val pnlInvestment = MutableStateFlow("50000")
    val pnlDurationMonths = MutableStateFlow("12")
    val fixedCosts = MutableStateFlow("25000")
    val unitPrice = MutableStateFlow("500")
    val unitCost = MutableStateFlow("150")

    private val _pnlModel = MutableStateFlow(
        PnlRoiEngine.calculate(
            revenue = 150000.0,
            cogs = 45000.0,
            opex = 35000.0,
            initialInvestment = 50000.0,
            fixedCosts = 25000.0,
            pricePerUnit = 500.0,
            variableCostPerUnit = 150.0,
            dealDurationMonths = 12.0
        )
    )
    val pnlModel: StateFlow<PnlModel> = _pnlModel.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                if (user != null) {
                    dealRepository.startFirestoreSync(viewModelScope)
                } else {
                    dealRepository.stopFirestoreSync()
                }
            }
        }
    }

    // --- Financing Calculation Triggers ---
    fun updateFinancing() {
        val p = principalAmount.value.toDoubleOrNull() ?: 0.0
        val down = downPayment.value.toDoubleOrNull() ?: 0.0
        val r = annualInterestRate.value.toDoubleOrNull() ?: 0.0
        val tenure = tenureMonths.value.toIntOrNull() ?: 12
        _amortization.value = AmortizationEngine.calculate(
            principal = p,
            annualInterestRatePct = r,
            tenureMonths = tenure,
            downPayment = down,
            frequency = paymentFrequency.value
        )
        updateCurrencyConversion()
    }

    fun setPaymentFrequency(freq: PaymentFrequency) {
        paymentFrequency.value = freq
        updateFinancing()
    }

    fun setTargetCurrency(code: String) {
        targetCurrency.value = code
        updateCurrencyConversion()
    }

    fun updateCurrencyConversion() {
        val p = principalAmount.value.toDoubleOrNull() ?: 0.0
        convertedDealValue.value = CurrencyEngine.convert(
            amount = p,
            sourceCode = selectedCurrency.value,
            targetCode = targetCurrency.value,
            rates = currencyRates.value
        )
        currentCrossRate.value = CurrencyEngine.getCrossRate(
            sourceCode = selectedCurrency.value,
            targetCode = targetCurrency.value,
            rates = currencyRates.value
        )
    }

    // --- SaaS & Funnel Triggers ---
    fun updateLtvCalculations() {
        val a = arpu.value.toDoubleOrNull() ?: 0.0
        val m = grossMarginPct.value.toDoubleOrNull() ?: 70.0
        val ch = churnRatePct.value.toDoubleOrNull() ?: 2.0
        val c = cac.value.toDoubleOrNull() ?: 1000.0
        _ltvMetrics.value = LtvCalculator.calculate(
            arpu = a,
            grossMarginPct = m,
            churnRatePct = ch,
            cac = c
        )
    }

    fun updateFunnelCalculations() {
        val leads = funnelLeads.value.toIntOrNull() ?: 0
        val mqls = funnelMqls.value.toIntOrNull() ?: 0
        val sqls = funnelSqls.value.toIntOrNull() ?: 0
        val opps = funnelOpps.value.toIntOrNull() ?: 0
        val won = funnelClosedWon.value.toIntOrNull() ?: 0
        val dealVal = avgDealValue.value.toDoubleOrNull() ?: 0.0
        _funnelSummary.value = FunnelCalculator.calculate(
            leads = leads,
            mqls = mqls,
            sqls = sqls,
            opportunities = opps,
            closedWon = won,
            avgDealValue = dealVal
        )
    }

    // --- Margin & Markup Solver Triggers ---
    fun onMarginCostOrPriceChanged(newCost: String? = null, newPrice: String? = null) {
        newCost?.let { marginCostInput.value = it }
        newPrice?.let { marginPriceInput.value = it }
        val cost = marginCostInput.value.toDoubleOrNull() ?: 0.0
        val price = marginPriceInput.value.toDoubleOrNull() ?: 0.0
        _marginState.value = MarginMarkupSolver.fromCostAndPrice(cost, price)
    }

    fun applyMarginPercentPreset(marginPct: Double) {
        val cost = marginCostInput.value.toDoubleOrNull() ?: 0.0
        _marginState.value = MarginMarkupSolver.fromCostAndMargin(cost, marginPct)
        marginPriceInput.value = _marginState.value.sellingPrice.toString()
    }

    fun applyMarkupPercentPreset(markupPct: Double) {
        val cost = marginCostInput.value.toDoubleOrNull() ?: 0.0
        _marginState.value = MarginMarkupSolver.fromCostAndMarkup(cost, markupPct)
        marginPriceInput.value = _marginState.value.sellingPrice.toString()
    }

    // --- PnL and ROI Triggers ---
    fun updatePnlCalculations() {
        val rev = pnlRevenue.value.toDoubleOrNull() ?: 0.0
        val cogs = pnlCogs.value.toDoubleOrNull() ?: 0.0
        val opex = pnlOpex.value.toDoubleOrNull() ?: 0.0
        val inv = pnlInvestment.value.toDoubleOrNull() ?: 0.0
        val dur = pnlDurationMonths.value.toDoubleOrNull() ?: 12.0
        val fixed = fixedCosts.value.toDoubleOrNull() ?: (opex * 0.7)
        val pUnit = unitPrice.value.toDoubleOrNull() ?: 500.0
        val cUnit = unitCost.value.toDoubleOrNull() ?: 150.0

        _pnlModel.value = PnlRoiEngine.calculate(
            revenue = rev,
            cogs = cogs,
            opex = opex,
            initialInvestment = inv,
            fixedCosts = fixed,
            pricePerUnit = pUnit,
            variableCostPerUnit = cUnit,
            dealDurationMonths = dur
        )
    }

    // --- Deal Saving & PDF Generation ---
    fun saveCurrentDeal() {
        viewModelScope.launch {
            val p = principalAmount.value.toDoubleOrNull() ?: 120000.0
            val r = annualInterestRate.value.toDoubleOrNull() ?: 6.5
            val tenure = tenureMonths.value.toIntOrNull() ?: 24

            val deal = SavedDeal(
                title = dealTitle.value,
                clientName = clientName.value,
                currencyCode = selectedCurrency.value,
                dealValue = p,
                interestRatePct = r,
                tenureMonths = tenure,
                frequency = paymentFrequency.value,
                periodicPayment = _amortization.value.periodicPayment,
                ltv = _ltvMetrics.value.ltv,
                ltvCacRatio = _ltvMetrics.value.ltvCacRatio,
                marginPct = _marginState.value.marginPct,
                markupPct = _marginState.value.markupPct,
                roiPct = _pnlModel.value.roiPct,
                paybackMonths = _pnlModel.value.paybackMonths,
                updatedAt = System.currentTimeMillis()
            )
            dealRepository.saveDeal(deal)
            _userMessage.emit("Deal proposal saved & synced: ${deal.title}")
        }
    }

    fun loadDeal(deal: SavedDeal) {
        dealTitle.value = deal.title
        clientName.value = deal.clientName
        selectedCurrency.value = deal.currencyCode
        principalAmount.value = deal.dealValue.toInt().toString()
        annualInterestRate.value = deal.interestRatePct.toString()
        tenureMonths.value = deal.tenureMonths.toString()
        paymentFrequency.value = deal.frequency

        updateFinancing()
        onMarginCostOrPriceChanged(
            newCost = (deal.dealValue * (1.0 - deal.marginPct / 100.0)).toInt().toString(),
            newPrice = deal.dealValue.toInt().toString()
        )
        pnlRevenue.value = deal.dealValue.toInt().toString()
        updatePnlCalculations()

        viewModelScope.launch {
            _userMessage.emit("Loaded scenario: ${deal.title}")
        }
    }

    fun deleteDeal(dealId: String) {
        viewModelScope.launch {
            dealRepository.deleteDeal(dealId)
            _userMessage.emit("Deal scenario deleted")
        }
    }

    fun exportPdf(context: Context) {
        viewModelScope.launch {
            val deal = SavedDeal(
                title = dealTitle.value,
                clientName = clientName.value,
                currencyCode = selectedCurrency.value,
                dealValue = principalAmount.value.toDoubleOrNull() ?: 120000.0,
                interestRatePct = annualInterestRate.value.toDoubleOrNull() ?: 6.5,
                tenureMonths = tenureMonths.value.toIntOrNull() ?: 24,
                frequency = paymentFrequency.value,
                periodicPayment = _amortization.value.periodicPayment,
                ltv = _ltvMetrics.value.ltv,
                ltvCacRatio = _ltvMetrics.value.ltvCacRatio,
                marginPct = _marginState.value.marginPct,
                markupPct = _marginState.value.markupPct,
                roiPct = _pnlModel.value.roiPct,
                paybackMonths = _pnlModel.value.paybackMonths,
                updatedAt = System.currentTimeMillis()
            )

            val result = PdfExportService.generateAndShareDealPdf(
                context = context,
                deal = deal,
                amortization = _amortization.value,
                pnl = _pnlModel.value,
                ltv = _ltvMetrics.value,
                margin = _marginState.value
            )

            result.onSuccess {
                _userMessage.emit("Executive PDF proposal generated successfully!")
            }.onFailure { e ->
                _userMessage.emit("Error generating PDF: ${e.localizedMessage}")
            }
        }
    }

    // --- Google Sign-In via Credential Manager ---
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activityContext)
            result.onSuccess { user ->
                _userMessage.emit("Connected as ${user.displayName ?: user.email}")
                dealRepository.startFirestoreSync(viewModelScope)
            }.onFailure { e ->
                _userMessage.emit("Sign-in cancelled or failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        viewModelScope.launch {
            _userMessage.emit("Signed out of Cloud Sync")
        }
    }
}
