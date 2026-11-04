# NotaBigDeal — B2B Pricing, Deal Desk & Commercial Negotiation Workstation

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/Tests-42%20Passed%20(100%25)-brightgreen.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-orange.svg)]()
[![AI Engine](https://img.shields.io/badge/AI-Gemini%203.8%20Flash%20%2B%20Offline-blueviolet.svg)]()

[🇬🇧 English](README.md) | [🇷🇺 Читать на русском](README_RU.md)

> **Stop margin leakage at the negotiation table.** NotaBigDeal is an offline-first, executive-grade financial calculation engine and deal desk platform designed for Key Account Managers (KAMs), Commercial Directors, and CFOs. Structure Net-Net price waterfalls, eliminate uncompensated discounting with the "Give-Get" concession engine, evaluate trade credit deferrals, stress-test FX baskets, and export boardroom-ready PDF proposals directly from your Android device — without laptops or heavy spreadsheets.

---

## Visual Overview

<p align="center">
  <img src="app/src/main/assets/screenshots/deal_approval.jpg" width="31%" alt="Deal Approval & Traffic Light Scoring" />
  <img src="app/src/main/assets/screenshots/analytics_cashflow.jpg" width="31%" alt="Executive Analytics & Cashflow" />
  <img src="app/src/main/assets/screenshots/cash_gap.jpg" width="31%" alt="Cash Gap & Working Capital Timeline" />
</p>

---

## Why NotaBigDeal?

In B2B contract negotiations with enterprise buyers and retail chains, multi-million dollar decisions are made across the negotiation table. When a procurement officer demands:
* an extra **+30 days payment deferral**;
* an additional **5% on-invoice discount**; and
* an **8% off-invoice retro-rebate**;

sales executives typically have seconds to determine:
1. **Real pocket Net-Net margin** after all layered concessions, rebates, and transport costs;
2. **Working capital drain** and the real financial cost of carry under corporate WACC;
3. **Breakeven volume lift** needed on promotions to prevent destroying company EBITDA.

**NotaBigDeal** brings Wall Street financial discipline to your pocket: from retail waterfall decomposition and "Give-Get" counter-trade mathematics to 0ms offline AI risk audits.

---

## Core Features & Financial Engines

### 1. Retail Price Waterfall & Net-Net Engine
Deconstructs transactions from consumer shelf RSP down to supplier pocket margin:
- Shelf RSP with VAT & Ex-VAT base;
- Retailer markup & gross margin deduction;
- On-invoice front discounts and wholesale invoice price;
- Off-invoice retro-rebates, slotting fees, and warehouse logistics cost;
- **Supplier Net-Net Price & True Contribution Margin %**.

### 2. "Give-Get" Concession Balancing Engine (EBITDA Guard)
Eliminates uncompensated margin giveaways during tough procurement sessions:
- **Model Buyer Concessions ("The Give")**:
  - Payment Deferral (+Days);
  - Extra Price Discount (+%);
  - Extra Retro-Rebate (+%);
  - Free Delivery / Logistics Absorption;
  - Marketing / Slotting Allowance.
- **Quantify Monetary Drain**: Direct financial cost of the concession and margin erosion in percentage points.
- **4 Compensatory Counter-Trades ("The Gets") to Keep Deals EBITDA-Neutral**:
  1. **Contracted Volume Lift**: Minimum units and percentage increase required so incremental gross profit absorbs the concession;
  2. **Base Price Compensation**: Target price list indexation if volume remains fixed;
  3. **Rebate Clawback**: Offsetting deduction from quarterly marketing allowances;
  4. **Payment Acceleration / Prepayment**: Cash prepayment requirement to eliminate working capital freeze.
- **Actionable KAM Talk Track Script**: Pre-computed negotiation speech module in Russian and English with 1-tap clipboard copy.

### 3. Trade Credit Terms, Prolongation & Cash Gap Engine
Models the real financial cost of deferred payment terms (30, 60, 90, 120 days):
- Cost of credit: $\text{Deal Amount} \times \text{WACC} \times \frac{\text{Days}}{365}$;
- **Trade Credit Prolongation Analyzer**: Incremental working capital drain when terms extend (e.g. 30d $\to$ 60d), and trade credit effective APR;
- **Cash Discount Arbitrage**: Evaluates prompt payment terms (e.g., 2/10 net 30/60) against delayed payment NPV;
- **Cash Conversion Cycle (CCC)**: Tracks $DIO + DSO - DPO$ and peak overdraft financing exposure.

### 4. Commercial Financing, Amortization & Multi-Currency Basket
Structures equipment leasing, client credit, and installment debt:
- 5 payment frequencies: Weekly (52/yr), Bi-Weekly (26/yr), Monthly (12/yr), Quarterly (4/yr), Annually (1/yr);
- **Two-Tier Grace Period Amortization**: Subsidized/zero rate during grace + standard annuity repayment post-grace;
- **Credit Prolongation Analyzer**: Compares month-to-month and week-to-week tenure extensions, detailing periodic cashflow relief (-47%) versus cumulative interest increase (+96%);
- **Multi-Currency Debt Basket**: Allocates borrowing across up to 4 currencies (USD, EUR, CNY, RUB, etc.) with forward FX hedging cost calculations.

### 5. Commercial Deal Scoring & Traffic Light Risk Corridor (BATNA)
Pre-flight approval engine providing instant commercial governance:
- 🟢 **Profitable Deal**: Healthy Net-Net margin exceeding corporate hurdle rates. Eligible for KAM self-approval;
- 🟡 **Borderline Deal**: Margin compressed by rebates or prolonged deferral (>45 days). Commercial Director review required with mandatory counter-trades;
- 🔴 **Margin Destroyer**: Below unit cost or financing charge turns deal toxic. Automated sign-off freeze with CFO escalation.

### 6. Landed Cost & Incoterms Modeler (FOB ➔ Warehouse)
Converts overseas supplier factory pricing into landed domestic cost:
- Coefficient multipliers ($K$) incorporating freight, customs duties, brokerage fees, and import VAT;
- Built-in trade profiles for major hubs: China, Turkey, UAE, European Union, India, and EAEU;
- **Reverse Solver**: Derives maximum allowable FOB purchasing price from a target landed cost budget.

### 7. Promo Break-Even & Elasticity Lift Solver
Answers the fundamental retail promotion question: *«By what percentage must volume increase under a discount of $d\%$, so company gross profit does not decrease?»*
- Mathematical breakeven lift: $\text{Lift}\% = \frac{d\%}{\text{Margin}\% - d\%} \times 100\%$;
- Dynamic feasibility flags preventing sales teams from approving margin-negative promotions.

### 8. Retail SLA Penalty & Fill-Rate Sensitivity Modeler
Simulates net pocket margin after supermarket logistics deductions:
- Calculates underdelivery fines based on planned vs actual order Fill-Rate (80%–100%);
- Models time-slot arrival penalties and pallet quantum non-compliance;
- Exposes actual "Pocket Margin" versus theoretical contract margin.

### 9. Client Negotiation History & Terms Locker
Enterprise counterparty database with multi-device cloud synchronization:
- Tracks agreed discount caps, volume tiers, payment deferrals, and currency settings;
- Historical contract log and chronological multi-round negotiation ledger;
- 1-click **"Apply Client Terms"** workflow into active deal calculations.

### 10. Native Vector PDF Proposal & LinkedIn Product Flyer Generator
Generates publication-quality vector PDF documents on-device without external web services:
- **Executive Deal Proposal (PDF)**: A4 contract brief with deal structure, pricing waterfall, and repayment schedule;
- **LinkedIn Product Flyer (PDF)**: High-impact executive one-pager showcasing 6 core modules, UI spotlights, and CFO ROI metrics for LinkedIn outreach.

### 11. Dual-Core AI Deal Risk Express Audit
1-tap deal audit button on the top toolbar:
- **Google Gemini 3.8 Flash (Serverless Firebase AI Logic)**: Analyzes deal parameters and synthesizes 3 concise negotiation theses (Margin traps, Working capital risk, Counter-offer strategy);
- **Deterministic Offline Heuristic Backup (0ms)**: Guaranteed instant operation inside secure meeting rooms or flights with zero internet connectivity;
- **100% Free**: No recurring API subscription or user billing required.

---

## Architecture & Tech Stack

```
com.example
├── data
│   ├── ai           # AI Deal Risk Auditor (Firebase AI Logic + Offline Heuristics)
│   ├── model        # Immutable Kotlin Data Models, Enums & Value Objects
│   └── repository   # AuthRepository & DealRepository (Firestore + Cache)
├── domain
│   └── calculator   # Pure Mathematical Engines (Amortization, GiveGet, Waterfall, ROIC)
├── service          # Android Native PdfExportService (Vector A4 Graphics)
└── ui
    ├── components   # Modular Material 3 Cards, Sliders, and Dialogs
    ├── screens      # Screen Scaffolds (Margin, Financing, Pipeline, Vault)
    ├── theme        # Executive Midnight & Emerald Palette (Dark/Light)
    └── viewmodel    # BigDealViewModel (Reactive StateFlow & Coroutines)
```

- **Runtime & Language**: 100% Kotlin 2.0.21, Android SDK 26+ (Target SDK 36);
- **UI Framework**: Jetpack Compose with Material Design 3 (M3);
- **Asynchronous Execution**: Kotlin Coroutines & `StateFlow`;
- **Cloud & Auth**: Firebase Firestore & Jetpack Credential Manager;
- **AI Integration**: Firebase AI Logic (`gemini-3.8-flash`) with App Check;
- **Document Engine**: Android Native Graphics & `PdfDocument`.

---

## Installation & Build

### Prerequisites
- Android Studio Ladybug / Meerkat or newer;
- JDK 17+;
- Android SDK 26+.

### Clone & Build
```bash
git clone https://github.com/your-username/notabigdeal.git
cd notabigdeal
gradle assembleDebug
```

### Running Automated Tests
Run the comprehensive suite of 42 financial and mathematical verification tests:
```bash
gradle :app:testDebugUnitTest
```

---

## Author & Contact

Developed for commercial executives, financial analysts, and enterprise Key Account Managers.
- **LinkedIn Profile**: [www.linkedin.com/in/snitkor](https://www.linkedin.com/in/snitkor)
- **License**: Proprietary / Enterprise Confidential
