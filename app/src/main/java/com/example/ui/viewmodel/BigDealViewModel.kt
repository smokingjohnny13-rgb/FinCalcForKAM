package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AmortizationSummary
import com.example.data.model.CargoCategory
import com.example.data.model.CountryTradeProfile
import com.example.data.model.CurrencyRate
import com.example.data.model.FunnelSummary
import com.example.data.model.LandedCostResult
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
import com.example.domain.calculator.LandedCostCalculator
import com.example.domain.calculator.LtvCalculator
import com.example.domain.calculator.MarginMarkupSolver
import com.example.domain.calculator.PaymentTermsEngine
import com.example.domain.calculator.PaymentTermsResult
import com.example.domain.calculator.PnlRoiEngine
import com.example.domain.calculator.PriceWaterfallEngine
import com.example.domain.calculator.CurrencyCreditLegInput
import com.example.domain.calculator.CurrencyCreditLegResult
import com.example.domain.calculator.MultiCurrencyCreditEngine
import com.example.domain.calculator.MultiCurrencyCreditResult
import com.example.domain.calculator.PriceWaterfallResult
import com.example.domain.calculator.PromoBreakEvenEngine
import com.example.domain.calculator.PromoBreakEvenResult
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

    // 2b. Express Multiplier & Landed Cost Matrix
    val selectedCategory = MutableStateFlow(CargoCategory.INDUSTRIAL)
    val selectedCountry = MutableStateFlow(LandedCostCalculator.COUNTRY_PROFILES[0]) // Belarus (EAEU)
    val fobPriceInput = MutableStateFlow("10000")
    val targetLandedCostInput = MutableStateFlow("14800")
    val expressCoeffInput = MutableStateFlow("1.48")
    val isLandedReverseMode = MutableStateFlow(false)

    val customLogisticsPct = MutableStateFlow("15.0")
    val customDutyPct = MutableStateFlow("7.5")
    val customVatPct = MutableStateFlow("20.0")
    val customBrokerPct = MutableStateFlow("2.5")

    private val _landedCostResult = MutableStateFlow(
        LandedCostCalculator.calculateDirect(
            fobPrice = 10000.0,
            coefficient = 1.48,
            logisticsPct = 15.0,
            dutyPct = 7.5,
            vatPct = 20.0,
            brokerFeesPct = 2.5
        )
    )
    val landedCostResult: StateFlow<LandedCostResult> = _landedCostResult.asStateFlow()

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

    // Global Multi-Currency Support
    val activeCurrency = MutableStateFlow(CurrencyEngine.findByCode("USD"))
    val customCurrencies = MutableStateFlow(CurrencyEngine.DEFAULT_CURRENCIES)

    // 6. Price Waterfall (РРЦ -> Net-Net Price)
    val waterfallRsp = MutableStateFlow("100")
    val waterfallVat = MutableStateFlow("20")
    val waterfallRetailerMargin = MutableStateFlow("30")
    val waterfallFrontDiscount = MutableStateFlow("5")
    val waterfallBackBonus = MutableStateFlow("8")
    val waterfallLogistics = MutableStateFlow("3.5")
    val waterfallCogs = MutableStateFlow("25")

    private val _waterfallResult = MutableStateFlow(
        PriceWaterfallEngine.calculate(
            shelfRspWithVat = 100.0,
            vatRatePct = 20.0,
            retailerMarginPct = 30.0,
            frontDiscountPct = 5.0,
            backBonusPct = 8.0,
            logisticsCostPerUnit = 3.5,
            supplierCogs = 25.0
        )
    )
    val waterfallResult: StateFlow<PriceWaterfallResult> = _waterfallResult.asStateFlow()

    // 7. Promo Break-Even & Elasticity Modeler
    val promoBasePrice = MutableStateFlow("50")
    val promoUnitCost = MutableStateFlow("30")
    val promoDiscountPct = MutableStateFlow("20")
    val promoBaseVolume = MutableStateFlow("1000")
    val promoSimulatedLift = MutableStateFlow("50")

    private val _promoResult = MutableStateFlow(
        PromoBreakEvenEngine.calculate(
            regularPrice = 50.0,
            unitCost = 30.0,
            promoDiscountPct = 20.0,
            baseVolume = 1000,
            simulatedVolumeLiftPct = 50.0
        )
    )
    val promoResult: StateFlow<PromoBreakEvenResult> = _promoResult.asStateFlow()

    // 8. Payment Terms & Working Capital Cost
    val termsDealAmount = MutableStateFlow("100000")
    val termsDays = MutableStateFlow("60")
    val termsAnnualRate = MutableStateFlow("18")
    val termsCashDiscount = MutableStateFlow("2")

    private val _paymentTermsResult = MutableStateFlow(
        PaymentTermsEngine.calculate(
            dealAmount = 100000.0,
            paymentDays = 60,
            annualInterestRatePct = 18.0,
            cashDiscountPct = 2.0
        )
    )
    val paymentTermsResult: StateFlow<PaymentTermsResult> = _paymentTermsResult.asStateFlow()

    // 9. Multi-Currency Credit & Hedging (до 4х валют)
    val creditPrincipal = MutableStateFlow("100000")
    val creditTenureMonths = MutableStateFlow("12")
    val creditSelectedCurrencies = MutableStateFlow(listOf("USD", "EUR", "RUB", "CNY"))
    val creditHedgeEnabled = MutableStateFlow(true)
    val creditHedgeCostPct = MutableStateFlow("2.5")
    val creditExpectedFxShiftPct = MutableStateFlow("8.0")
    val creditCurrencyRates = MutableStateFlow(
        mapOf(
            "USD" to 7.5,
            "EUR" to 5.5,
            "RUB" to 18.5,
            "CNY" to 4.2,
            "BYN" to 11.5,
            "KZT" to 14.5,
            "AED" to 6.0,
            "TRY" to 42.0
        )
    )
    val creditAllocations = MutableStateFlow(
        mapOf(
            "USD" to 25.0,
            "EUR" to 25.0,
            "RUB" to 25.0,
            "CNY" to 25.0
        )
    )

    private val _multiCreditResult = MutableStateFlow(
        MultiCurrencyCreditEngine.calculate(
            dealAmount = 100000.0,
            baseCurrencyCode = "USD",
            tenureMonths = 12,
            legsInput = listOf(
                CurrencyCreditLegInput("USD", 7.5, 25.0, 2.5, 8.0, true),
                CurrencyCreditLegInput("EUR", 5.5, 25.0, 2.5, 8.0, true),
                CurrencyCreditLegInput("RUB", 18.5, 25.0, 2.5, 8.0, true),
                CurrencyCreditLegInput("CNY", 4.2, 25.0, 2.5, 8.0, true)
            )
        )
    )
    val multiCreditResult: StateFlow<MultiCurrencyCreditResult> = _multiCreditResult.asStateFlow()

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

    // --- Landed Cost & Express Multipliers ---
    fun updateLandedCostCalculations() {
        val fob = fobPriceInput.value.toDoubleOrNull() ?: 0.0
        val targetLanded = targetLandedCostInput.value.toDoubleOrNull() ?: 0.0
        val coeff = expressCoeffInput.value.toDoubleOrNull() ?: 1.48

        val logPct = customLogisticsPct.value.toDoubleOrNull() ?: 15.0
        val dutyPct = customDutyPct.value.toDoubleOrNull() ?: 7.5
        val vatPct = customVatPct.value.toDoubleOrNull() ?: 20.0
        val brokerPct = customBrokerPct.value.toDoubleOrNull() ?: 2.5

        if (isLandedReverseMode.value) {
            val result = LandedCostCalculator.calculateReverse(
                targetLandedCost = targetLanded,
                coefficient = coeff,
                logisticsPct = logPct,
                dutyPct = dutyPct,
                vatPct = vatPct,
                brokerFeesPct = brokerPct
            )
            _landedCostResult.value = result
            fobPriceInput.value = result.fobPrice.toInt().toString()
        } else {
            val result = LandedCostCalculator.calculateDirect(
                fobPrice = fob,
                coefficient = coeff,
                logisticsPct = logPct,
                dutyPct = dutyPct,
                vatPct = vatPct,
                brokerFeesPct = brokerPct
            )
            _landedCostResult.value = result
            targetLandedCostInput.value = result.landedCost.toInt().toString()
        }
    }

    fun setLandedReverseMode(reverse: Boolean) {
        isLandedReverseMode.value = reverse
        updateLandedCostCalculations()
    }

    fun applyCargoCategory(category: CargoCategory) {
        selectedCategory.value = category
        if (category != CargoCategory.CUSTOM) {
            expressCoeffInput.value = category.defaultCoeff.toString()
            customLogisticsPct.value = category.avgLogisticsPct.toString()
            customDutyPct.value = category.avgDutyPct.toString()
        }
        updateLandedCostCalculations()
    }

    fun applyCountryTradeProfile(country: CountryTradeProfile) {
        selectedCountry.value = country
        customVatPct.value = country.defaultVatPct.toString()
        customDutyPct.value = country.defaultDutyPct.toString()
        customLogisticsPct.value = country.defaultLogisticsPct.toString()
        customBrokerPct.value = country.defaultBrokerFeesPct.toString()
        recalculateCustomCoefficient()
    }

    fun recalculateCustomCoefficient() {
        val logPct = customLogisticsPct.value.toDoubleOrNull() ?: 15.0
        val dutyPct = customDutyPct.value.toDoubleOrNull() ?: 7.5
        val vatPct = customVatPct.value.toDoubleOrNull() ?: 20.0
        val brokerPct = customBrokerPct.value.toDoubleOrNull() ?: 2.5
        val computedCoeff = LandedCostCalculator.buildCoefficientFromComponents(
            logisticsPct = logPct,
            dutyPct = dutyPct,
            vatPct = vatPct,
            brokerFeesPct = brokerPct
        )
        expressCoeffInput.value = computedCoeff.toString()
        updateLandedCostCalculations()
    }

    fun calibrateCoefficientFromPrices() {
        val fob = fobPriceInput.value.toDoubleOrNull() ?: 0.0
        val landed = targetLandedCostInput.value.toDoubleOrNull() ?: 0.0
        val calibrated = LandedCostCalculator.calibrateCoefficient(fob, landed)
        expressCoeffInput.value = calibrated.toString()
        selectedCategory.value = CargoCategory.CUSTOM
        updateLandedCostCalculations()
        viewModelScope.launch {
            _userMessage.emit("Calibrated Express Multiplier: $calibrated")
        }
    }

    fun transferLandedCostToMarginSolver() {
        val landed = _landedCostResult.value.landedCost
        marginCostInput.value = landed.toInt().toString()
        val currentPrice = marginPriceInput.value.toDoubleOrNull() ?: 0.0
        if (currentPrice <= landed) {
            val suggestedPrice = (landed / (1.0 - 0.35)).toInt()
            marginPriceInput.value = suggestedPrice.toString()
        }
        onMarginCostOrPriceChanged()
        viewModelScope.launch {
            _userMessage.emit("Landed Cost $${landed.toInt()} transferred to Margin & Pricing Solver")
        }
    }

    fun transferLandedCostToDeal() {
        val landed = _landedCostResult.value.landedCost
        principalAmount.value = landed.toInt().toString()
        updateFinancing()
        viewModelScope.launch {
            _userMessage.emit("Deal value updated with Landed Cost: $${landed.toInt()}")
        }
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

    fun applyRoiPnlForecast(forecast: com.example.domain.calculator.RoiPnlForecast) {
        pnlRevenue.value = forecast.total12MonthRevenue.toInt().toString()
        val cogs = maxOf(0.0, forecast.total12MonthRevenue - forecast.total12MonthGrossProfit)
        pnlCogs.value = cogs.toInt().toString()
        pnlOpex.value = forecast.total12MonthOpex.toInt().toString()
        pnlInvestment.value = forecast.totalCacInvestment.toInt().toString()
        pnlDurationMonths.value = "12"
        updatePnlCalculations()
        viewModelScope.launch {
            _userMessage.emit("Applied 12-Month Cohort Forecast to Contract P&L")
        }
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
                margin = _marginState.value,
                landedCost = _landedCostResult.value
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

    // --- Currency Selection & Custom Currencies ---
    fun selectActiveCurrency(code: String) {
        selectedCurrency.value = code
        val found = CurrencyEngine.findByCode(code, customCurrencies.value)
        activeCurrency.value = found
        updateCurrencyConversion()
        viewModelScope.launch {
            _userMessage.emit("Валюта изменена: ${found.code} (${found.symbol})")
        }
    }

    fun addCustomCurrency(code: String, name: String, symbol: String, rateToBaseUsd: Double) {
        val safeCode = code.uppercase().trim().ifEmpty { "CUR" }
        val safeName = name.trim().ifEmpty { safeCode }
        val safeSym = symbol.trim().ifEmpty { safeCode }
        val safeRate = if (rateToBaseUsd > 0) rateToBaseUsd else 1.0
        val newRate = CurrencyRate(safeCode, safeName, safeSym, safeRate)
        val list = customCurrencies.value.filter { !it.code.equals(newRate.code, ignoreCase = true) } + newRate
        customCurrencies.value = list
        currencyRates.value = list
        selectActiveCurrency(newRate.code)
    }

    fun formatCurrency(amount: Double): String {
        val sym = activeCurrency.value.symbol
        val formattedNum = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }.format(amount)
        return if (sym.length > 2) "$formattedNum $sym" else "$sym$formattedNum"
    }

    // --- Price Waterfall Triggers ---
    fun updateWaterfallCalculations() {
        val rsp = waterfallRsp.value.toDoubleOrNull() ?: 100.0
        val vat = waterfallVat.value.toDoubleOrNull() ?: 20.0
        val retMargin = waterfallRetailerMargin.value.toDoubleOrNull() ?: 30.0
        val frontDisc = waterfallFrontDiscount.value.toDoubleOrNull() ?: 5.0
        val backBonus = waterfallBackBonus.value.toDoubleOrNull() ?: 8.0
        val logistics = waterfallLogistics.value.toDoubleOrNull() ?: 3.5
        val cogs = waterfallCogs.value.toDoubleOrNull() ?: 25.0

        _waterfallResult.value = PriceWaterfallEngine.calculate(
            shelfRspWithVat = rsp,
            vatRatePct = vat,
            retailerMarginPct = retMargin,
            frontDiscountPct = frontDisc,
            backBonusPct = backBonus,
            logisticsCostPerUnit = logistics,
            supplierCogs = cogs
        )
    }

    fun importCogsFromLandedCost() {
        val landed = _landedCostResult.value.landedCost
        waterfallCogs.value = landed.toString()
        updateWaterfallCalculations()
        viewModelScope.launch {
            _userMessage.emit("Себестоимость COGS обновлена из Landed Cost: ${formatCurrency(landed)}")
        }
    }

    fun applyWaterfallToMarginSolver() {
        val net = _waterfallResult.value.netNetPrice
        val cogs = _waterfallResult.value.supplierCogs
        onMarginCostOrPriceChanged(newCost = cogs.toString(), newPrice = net.toString())
        viewModelScope.launch {
            _userMessage.emit("Чистая цена (${formatCurrency(net)}) передана в расчет маржи")
        }
    }

    fun applyWaterfallToPnl() {
        val net = _waterfallResult.value.netNetPrice
        val cogs = _waterfallResult.value.supplierCogs
        unitPrice.value = net.toInt().toString()
        unitCost.value = cogs.toInt().toString()
        updatePnlCalculations()
        viewModelScope.launch {
            _userMessage.emit("Net-Net цена и себестоимость переданы в P&L / Точку безубыточности")
        }
    }

    // --- Promo Break-Even Triggers ---
    fun updatePromoCalculations() {
        val price = promoBasePrice.value.toDoubleOrNull() ?: 50.0
        val cost = promoUnitCost.value.toDoubleOrNull() ?: 30.0
        val disc = promoDiscountPct.value.toDoubleOrNull() ?: 20.0
        val vol = promoBaseVolume.value.toIntOrNull() ?: 1000
        val simLift = promoSimulatedLift.value.toDoubleOrNull() ?: 50.0

        _promoResult.value = PromoBreakEvenEngine.calculate(
            regularPrice = price,
            unitCost = cost,
            promoDiscountPct = disc,
            baseVolume = vol,
            simulatedVolumeLiftPct = simLift
        )
    }

    fun importPromoFromWaterfall() {
        val net = _waterfallResult.value.netNetPrice
        val cogs = _waterfallResult.value.supplierCogs
        promoBasePrice.value = net.toString()
        promoUnitCost.value = cogs.toString()
        updatePromoCalculations()
        viewModelScope.launch {
            _userMessage.emit("Данные для промо загружены из Водопада цен (Цена: ${formatCurrency(net)}, Себестоимость: ${formatCurrency(cogs)})")
        }
    }

    // --- Payment Terms Triggers ---
    fun updatePaymentTermsCalculations() {
        val amt = termsDealAmount.value.toDoubleOrNull() ?: 100000.0
        val days = termsDays.value.toIntOrNull() ?: 60
        val rate = termsAnnualRate.value.toDoubleOrNull() ?: 18.0
        val cashDisc = termsCashDiscount.value.toDoubleOrNull() ?: 2.0

        _paymentTermsResult.value = PaymentTermsEngine.calculate(
            dealAmount = amt,
            paymentDays = days,
            annualInterestRatePct = rate,
            cashDiscountPct = cashDisc
        )
    }

    fun importTermsFromDeal() {
        val p = principalAmount.value.toDoubleOrNull() ?: 120000.0
        termsDealAmount.value = p.toInt().toString()
        updatePaymentTermsCalculations()
        viewModelScope.launch {
            _userMessage.emit("Сумма сделки импортирована: ${formatCurrency(p)}")
        }
    }

    fun applyEffectiveNetToDeal() {
        val npv = _paymentTermsResult.value.netPresentValue
        principalAmount.value = npv.toInt().toString()
        updateFinancing()
        viewModelScope.launch {
            _userMessage.emit("Сумма сделки скорректирована на стоимость отсрочки: ${formatCurrency(npv)}")
        }
    }

    // --- Multi-Currency Credit & Hedging Methods ---
    fun toggleCreditCurrency(code: String) {
        val current = creditSelectedCurrencies.value.toMutableList()
        if (current.contains(code)) {
            if (current.size > 1) { // keep at least 1
                current.remove(code)
            }
        } else {
            if (current.size < 4) { // max 4
                current.add(code)
            }
        }
        creditSelectedCurrencies.value = current
        rebalanceCreditAllocations()
        updateMultiCreditCalculations()
    }

    fun setCreditCurrenciesCount(count: Int) {
        val allAvailable = listOf("USD", "EUR", "CNY", "RUB", "BYN", "KZT")
        val newCurrencies = allAvailable.take(count.coerceIn(1, 4))
        creditSelectedCurrencies.value = newCurrencies
        rebalanceCreditAllocations()
        updateMultiCreditCalculations()
    }

    private fun rebalanceCreditAllocations() {
        val currs = creditSelectedCurrencies.value
        val equalShare = 100.0 / currs.size
        val newAlloc = mutableMapOf<String, Double>()
        currs.forEach { c -> newAlloc[c] = equalShare }
        creditAllocations.value = newAlloc
    }

    fun updateCreditCurrencyRate(code: String, rate: Double) {
        val current = creditCurrencyRates.value.toMutableMap()
        current[code] = rate
        creditCurrencyRates.value = current
        updateMultiCreditCalculations()
    }

    fun updateCreditAllocation(code: String, alloc: Double) {
        val current = creditAllocations.value.toMutableMap()
        current[code] = alloc
        creditAllocations.value = current
        updateMultiCreditCalculations()
    }

    fun setCreditHedgeEnabled(enabled: Boolean) {
        creditHedgeEnabled.value = enabled
        updateMultiCreditCalculations()
    }

    fun updateMultiCreditCalculations() {
        val amt = creditPrincipal.value.toDoubleOrNull() ?: 100000.0
        val tenure = creditTenureMonths.value.toIntOrNull() ?: 12
        val hedgeRate = creditHedgeCostPct.value.toDoubleOrNull() ?: 2.5
        val fxShift = creditExpectedFxShiftPct.value.toDoubleOrNull() ?: 8.0
        val isHedged = creditHedgeEnabled.value
        val currs = creditSelectedCurrencies.value

        val legs = currs.map { c ->
            val rate = creditCurrencyRates.value[c] ?: MultiCurrencyCreditEngine.getDefaultRate(c)
            val alloc = creditAllocations.value[c] ?: (100.0 / currs.size)
            CurrencyCreditLegInput(
                currencyCode = c,
                annualRatePct = rate,
                allocationPct = alloc,
                hedgeCostAnnualPct = hedgeRate,
                expectedFxShiftPct = fxShift,
                isHedged = isHedged
            )
        }

        _multiCreditResult.value = MultiCurrencyCreditEngine.calculate(
            dealAmount = amt,
            baseCurrencyCode = activeCurrency.value.code,
            tenureMonths = tenure,
            legsInput = legs,
            rates = currencyRates.value
        )
    }

    fun importDealToMultiCredit() {
        val p = principalAmount.value.toDoubleOrNull() ?: 120000.0
        val t = tenureMonths.value.toIntOrNull() ?: 12
        creditPrincipal.value = p.toInt().toString()
        creditTenureMonths.value = t.toString()
        updateMultiCreditCalculations()
        viewModelScope.launch {
            _userMessage.emit("Данные сделки импортированы в мультивалютный кредитный калькулятор: ${formatCurrency(p)}")
        }
    }

    fun applyCreditHedgedRateToFinancing() {
        val hedgedRate = _multiCreditResult.value.weightedEffectiveRateWithHedgePct
        annualInterestRate.value = String.format(java.util.Locale.US, "%.1f", hedgedRate)
        updateFinancing()
        viewModelScope.launch {
            _userMessage.emit("Ставка финансирования сделки обновлена: ${annualInterestRate.value}% (с учетом корзины и хеджа)")
        }
    }
}
