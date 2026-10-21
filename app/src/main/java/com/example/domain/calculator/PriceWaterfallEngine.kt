package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.round

data class PriceWaterfallResult(
    val shelfRspWithVat: Double,
    val vatRatePct: Double,
    val vatAmount: Double,
    val priceExVat: Double,
    val retailerMarginPct: Double,
    val retailerMarginAmount: Double,
    val retailerPurchasePrice: Double,
    val frontDiscountPct: Double,
    val frontDiscountAmount: Double,
    val invoicePrice: Double,
    val backBonusPct: Double,
    val backBonusAmount: Double,
    val logisticsCostPerUnit: Double,
    val netNetPrice: Double,
    val supplierCogs: Double,
    val supplierGrossProfit: Double,
    val supplierMarginPct: Double,
    val supplierMarkupPct: Double,
    val totalDeductionPct: Double
)

object PriceWaterfallEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Calculates the complete price waterfall chain from Shelf RSP down to Supplier Net-Net Realization:
     * 1. Shelf RSP with VAT
     * 2. Shelf RSP Ex VAT = RSP / (1 + VAT%)
     * 3. Retailer Margin Amount = Ex VAT * RetailerMargin%
     * 4. Retailer Purchase Price = Ex VAT - Retailer Margin Amount
     * 5. Front Discount = Retailer Purchase Price * FrontDiscount%
     * 6. Invoice Price (On-Invoice) = Retailer Purchase Price - Front Discount
     * 7. Back Bonuses / Retro = Invoice Price * BackBonus%
     * 8. Logistics = Logistics Cost Per Unit
     * 9. Net-Net Price = Invoice Price - Back Bonuses - Logistics
     * 10. Supplier Gross Profit = Net-Net Price - Supplier COGS
     */
    fun calculate(
        shelfRspWithVat: Double,
        vatRatePct: Double = 20.0,
        retailerMarginPct: Double = 30.0,
        frontDiscountPct: Double = 5.0,
        backBonusPct: Double = 8.0,
        logisticsCostPerUnit: Double = 3.5,
        supplierCogs: Double = 25.0
    ): PriceWaterfallResult {
        val safeRsp = max(0.0, shelfRspWithVat)
        val safeVat = max(0.0, vatRatePct) / 100.0
        val safeRetMargin = max(0.0, retailerMarginPct) / 100.0
        val safeFrontDisc = max(0.0, frontDiscountPct) / 100.0
        val safeBackBonus = max(0.0, backBonusPct) / 100.0
        val safeLogistics = max(0.0, logisticsCostPerUnit)
        val safeCogs = max(0.0, supplierCogs)

        val priceExVat = if (safeVat > -1.0) safeRsp / (1.0 + safeVat) else safeRsp
        val vatAmount = safeRsp - priceExVat

        val retailerMarginAmount = priceExVat * safeRetMargin
        val retailerPurchasePrice = max(0.0, priceExVat - retailerMarginAmount)

        val frontDiscountAmount = retailerPurchasePrice * safeFrontDisc
        val invoicePrice = max(0.0, retailerPurchasePrice - frontDiscountAmount)

        val backBonusAmount = invoicePrice * safeBackBonus
        val netNetPrice = max(0.0, invoicePrice - backBonusAmount - safeLogistics)

        val supplierGrossProfit = netNetPrice - safeCogs
        val supplierMarginPct = if (netNetPrice > 0.0) (supplierGrossProfit / netNetPrice) * 100.0 else 0.0
        val supplierMarkupPct = if (safeCogs > 0.0) (supplierGrossProfit / safeCogs) * 100.0 else 0.0

        val totalDeductionPct = if (safeRsp > 0.0) ((safeRsp - netNetPrice) / safeRsp) * 100.0 else 0.0

        return PriceWaterfallResult(
            shelfRspWithVat = r2(safeRsp),
            vatRatePct = r2(vatRatePct),
            vatAmount = r2(vatAmount),
            priceExVat = r2(priceExVat),
            retailerMarginPct = r2(retailerMarginPct),
            retailerMarginAmount = r2(retailerMarginAmount),
            retailerPurchasePrice = r2(retailerPurchasePrice),
            frontDiscountPct = r2(frontDiscountPct),
            frontDiscountAmount = r2(frontDiscountAmount),
            invoicePrice = r2(invoicePrice),
            backBonusPct = r2(backBonusPct),
            backBonusAmount = r2(backBonusAmount),
            logisticsCostPerUnit = r2(safeLogistics),
            netNetPrice = r2(netNetPrice),
            supplierCogs = r2(safeCogs),
            supplierGrossProfit = r2(supplierGrossProfit),
            supplierMarginPct = r2(supplierMarginPct),
            supplierMarkupPct = r2(supplierMarkupPct),
            totalDeductionPct = r2(totalDeductionPct)
        )
    }
}
