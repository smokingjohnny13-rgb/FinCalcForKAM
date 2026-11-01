package com.example.data.repository

import com.example.data.model.ClientNegotiationProfile
import com.example.data.model.NegotiationTouchpoint
import com.example.data.model.PaymentFrequency
import com.example.data.model.SavedDeal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ClientNegotiationRepository {

    private val _clients = MutableStateFlow<List<ClientNegotiationProfile>>(emptyList())
    val clients: StateFlow<List<ClientNegotiationProfile>> = _clients.asStateFlow()

    init {
        // Pre-seed realistic counterparties with complete historical records
        _clients.value = listOf(
            ClientNegotiationProfile(
                id = "client-omnitech-01",
                name = "OmniTech Solutions Inc.",
                industry = "Enterprise IT & Cloud",
                creditLimit = 350000.0,
                agreedPaymentTermsDays = 30,
                standardCurrency = "USD",
                standardOnInvoiceDiscountPct = 5.0,
                maxAllowedDiscountPct = 10.0,
                standardOffInvoiceBonusPct = 6.0,
                annualVolumeRebatePct = 2.5,
                notes = "Стратегический партнер. Платит строго в срок 30 дней. Чувствителен к прозрачности Net-Net цен.",
                pastDeals = listOf(
                    SavedDeal(
                        id = "deal-omni-past-01",
                        title = "Q1 Infrastructure License Renewal",
                        clientName = "OmniTech Solutions Inc.",
                        currencyCode = "USD",
                        dealValue = 180000.0,
                        interestRatePct = 5.5,
                        tenureMonths = 12,
                        frequency = PaymentFrequency.MONTHLY,
                        marginPct = 48.0,
                        updatedAt = System.currentTimeMillis() - 7776000000L // 90 days ago
                    ),
                    SavedDeal(
                        id = "deal-omni-past-02",
                        title = "Enterprise Cloud Support Addendum",
                        clientName = "OmniTech Solutions Inc.",
                        currencyCode = "USD",
                        dealValue = 65000.0,
                        interestRatePct = 5.0,
                        tenureMonths = 6,
                        frequency = PaymentFrequency.MONTHLY,
                        marginPct = 52.0,
                        updatedAt = System.currentTimeMillis() - 15552000000L // 180 days ago
                    )
                ),
                negotiationHistory = listOf(
                    NegotiationTouchpoint(
                        roundNumber = 1,
                        dateTimestamp = System.currentTimeMillis() - 1209600000L,
                        discountDemandedPct = 12.0,
                        bonusDemandedPct = 8.0,
                        paymentTermsDemandedDays = 60,
                        buyerDemands = "Закупщик запросил 12% фронт-скидку и отсрочку 60 дней для включения в годовой бюджет.",
                        kamCounterOffer = "КАМ отклонил 60 дней (стоимость отсрочки съедает 3% маржи). Предложено 5% скидки при строгой оплате за 30 дней.",
                        outcomeStatus = "COUNTER_OFFER_PENDING"
                    ),
                    NegotiationTouchpoint(
                        roundNumber = 2,
                        dateTimestamp = System.currentTimeMillis() - 432000000L,
                        discountDemandedPct = 6.0,
                        bonusDemandedPct = 6.0,
                        paymentTermsDemandedDays = 30,
                        buyerDemands = "Клиент согласился на 30 дней, но запросил 6% ретро-бонус по итогам года при объеме от 1500 шт.",
                        kamCounterOffer = "Принято с привязкой к жесткому KPI по объему. Согласована Net-Net маржа 38%.",
                        outcomeStatus = "TERMS_LOCKED"
                    )
                )
            ),
            ClientNegotiationProfile(
                id = "client-metropolis-02",
                name = "Metropolis Hypermarket Group",
                industry = "FMCG & National Retail",
                creditLimit = 800000.0,
                agreedPaymentTermsDays = 65,
                standardCurrency = "USD",
                standardOnInvoiceDiscountPct = 8.0,
                maxAllowedDiscountPct = 14.0,
                standardOffInvoiceBonusPct = 12.0,
                annualVolumeRebatePct = 4.0,
                notes = "Крупная федеральная сеть. Жесткие требования по отсрочке (60-75 дней) и бэк-бонусам (10-14%). Высокий риск заморозки оборотного капитала.",
                pastDeals = listOf(
                    SavedDeal(
                        id = "deal-metro-past-01",
                        title = "Annual Shelf Listing Contract",
                        clientName = "Metropolis Hypermarket Group",
                        currencyCode = "USD",
                        dealValue = 420000.0,
                        interestRatePct = 7.5,
                        tenureMonths = 12,
                        frequency = PaymentFrequency.MONTHLY,
                        marginPct = 26.5,
                        updatedAt = System.currentTimeMillis() - 5184000000L
                    )
                ),
                negotiationHistory = listOf(
                    NegotiationTouchpoint(
                        roundNumber = 1,
                        dateTimestamp = System.currentTimeMillis() - 2592000000L,
                        discountDemandedPct = 15.0,
                        bonusDemandedPct = 14.0,
                        paymentTermsDemandedDays = 90,
                        buyerDemands = "Требование 90 дней отсрочки + 14% маркетинг-бонус + 15% скидка. Пограничная зона (уничтожение маржи).",
                        kamCounterOffer = "КАМ выставил расчет стоимости отсрочки: при 90 днях компания теряет 5.2% стоимости контракта на финансировании.",
                        outcomeStatus = "CONCESSION_GRANTED"
                    ),
                    NegotiationTouchpoint(
                        roundNumber = 2,
                        dateTimestamp = System.currentTimeMillis() - 864000000L,
                        discountDemandedPct = 8.0,
                        bonusDemandedPct = 12.0,
                        paymentTermsDemandedDays = 65,
                        buyerDemands = "Компромисс: отсрочка 65 дней, 8% он-инвойс, 12% офф-инвойс ретро при гарантии 4 торцевых промо-выкладок в год.",
                        kamCounterOffer = "Согласовано с обязательным trade-off: включение в контракт двух премиальных высокомаржинальных позиций.",
                        outcomeStatus = "TERMS_LOCKED"
                    )
                )
            ),
            ClientNegotiationProfile(
                id = "client-vanguard-03",
                name = "Vanguard Financial AG",
                industry = "Banking & FinTech",
                creditLimit = 500000.0,
                agreedPaymentTermsDays = 21,
                standardCurrency = "EUR",
                standardOnInvoiceDiscountPct = 3.5,
                maxAllowedDiscountPct = 8.0,
                standardOffInvoiceBonusPct = 4.0,
                annualVolumeRebatePct = 1.5,
                notes = "Европейский финтех. Высокая платежная дисциплина (оплата в течение 21 дня). Низкие требования по скидкам.",
                pastDeals = listOf(
                    SavedDeal(
                        id = "deal-van-past-01",
                        title = "EMEA Secure Gateway Rollout",
                        clientName = "Vanguard Financial AG",
                        currencyCode = "EUR",
                        dealValue = 95000.0,
                        interestRatePct = 4.8,
                        tenureMonths = 24,
                        frequency = PaymentFrequency.QUARTERLY,
                        marginPct = 44.5,
                        updatedAt = System.currentTimeMillis() - 172800000
                    )
                ),
                negotiationHistory = listOf(
                    NegotiationTouchpoint(
                        roundNumber = 1,
                        dateTimestamp = System.currentTimeMillis() - 1728000000L,
                        discountDemandedPct = 5.0,
                        bonusDemandedPct = 4.0,
                        paymentTermsDemandedDays = 21,
                        buyerDemands = "Запрос оптовой скидки 5% при авансировании 20% сделки.",
                        kamCounterOffer = "Согласовано 3.5% скидки + 4% накопительный годовой бонус. Зеленый коридор сделки.",
                        outcomeStatus = "TERMS_LOCKED"
                    )
                )
            )
        )
    }

    fun addClient(profile: ClientNegotiationProfile) {
        val current = _clients.value.toMutableList()
        current.add(0, profile)
        _clients.value = current
    }

    fun updateClient(profile: ClientNegotiationProfile) {
        val current = _clients.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile
            _clients.value = current
        }
    }

    fun deleteClient(clientId: String) {
        val current = _clients.value.toMutableList()
        current.removeAll { it.id == clientId }
        _clients.value = current
    }

    fun addNegotiationTouchpoint(clientId: String, touchpoint: NegotiationTouchpoint) {
        val current = _clients.value.toMutableList()
        val index = current.indexOfFirst { it.id == clientId }
        if (index >= 0) {
            val client = current[index]
            val updatedHistory = listOf(touchpoint) + client.negotiationHistory
            current[index] = client.copy(negotiationHistory = updatedHistory)
            _clients.value = current
        }
    }

    fun attachPastDealToClient(clientId: String, deal: SavedDeal) {
        val current = _clients.value.toMutableList()
        val index = current.indexOfFirst { it.id == clientId }
        if (index >= 0) {
            val client = current[index]
            val updatedPast = listOf(deal) + client.pastDeals
            current[index] = client.copy(pastDeals = updatedPast)
            _clients.value = current
        }
    }
}
