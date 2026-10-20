# BigDeal - BDM & KAM Financial Modeler & Analytics Suite

A comprehensive business calculation engine and executive deal-structuring workstation tailored for Business Development Managers (BDMs) and Key Account Managers (KAMs) to model customer lifetime value, churn rates, sales funnels, financing payments, cross-currency purchase packages, margin/markup dynamics, and PnL performance, backed by cloud synchronization and professional PDF report exports.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The app is named **BigDeal** as confirmed. All key modules are designed around enterprise deal velocity, contract negotiation, and executive business cases.

- **Confirmed App Identity**: **BigDeal**, targeting B2B Sales Executives, BDMs, and KAMs.
- **SaaS & Pipeline Metrics**: Full customer economics suite including LTV, Customer Churn & Revenue Churn, Customer Acquisition Cost (CAC), LTV:CAC health score, and multi-stage funnel conversion velocity.
- **Financing & Purchase Modeling**: Flexible loan/credit amortization calculations with weekly, monthly, quarterly, and annual payment schedules, paired with an integrated cross-currency engine (USD, EUR, GBP, JPY, CAD, AUD, CHF, SGD, and custom rates).
- **Two-Way Margin & Markup Matrix**: Multi-variable solver that instantaneously reconciles Cost, Selling Price, Gross Profit, Margin %, and Markup % from any two known variables.
- **ROI & PnL Performance Analytics**: Comprehensive investment appraisal including Net ROI %, Payback Period, Break-Even volume, and mini P&L (Revenue, COGS, OPEX, EBITDA, Net Profit).
- **Executive PDF Export**: Generates printable, high-resolution vector PDF deal summary reports via Android's native `android.graphics.pdf.PdfDocument` with share sheet integration.
- **Cloud Sync & Collaboration**: Cloud persistence via Firebase Firestore with Google Sign-In (using Credential Manager), plus local offline fallback for uninterrupted pitch meetings.

---

### 1. Overview & Core Concept

**BigDeal** eliminates the friction of switching between spreadsheets, basic calculator apps, and currency converter websites during client presentations, pricing reviews, and board-level deal approvals. It equips BDMs and KAMs with an executive-grade toolbox for structuring profitable contracts, verifying account unit economics, and exporting boardroom-ready proposal briefs on the fly.

#### Target Audience & Use Cases
- **Key Account Managers (KAMs)**: Reviewing multi-year client renewals, analyzing contract expansion revenue, account churn risks, and structuring tiered payment schedules.
- **Business Development Managers (BDMs)**: Modeling enterprise acquisition proposals, customer acquisition economics (CAC vs. LTV), lead-to-win conversion funnels, and margin thresholds.
- **Commercial Directors & Sales Leaders**: Instant ROI justification and PnL impact simulations during executive pricing negotiations.

---

### 2. User Experience & Visual Design

#### Visual Identity & Theme
- **Theme Concept**: *Executive Obsidian & Emerald* — commanding, authoritative, and clean, reminiscent of high-end financial terminals like Bloomberg and Carta.
- **Color Palette**:
  - `Background`: Deep Midnight Slate (`#0B132B`) & Rich Dark Blue (`#1C2541`)
  - `Surface`: Elevated Card Slate (`#1E293B`) with subtle border strokes (`#334155`)
  - `Primary Accent`: Kinetic Emerald (`#10B981` / `#059669`) signifying revenue, profitability, and positive ROI
  - `Secondary Accent`: Electric Cyan (`#06B6D4`) for pipeline conversion and ratios
  - `Warning / Alert`: Coral Amber (`#F59E0B` / `#EF4444`) for churn spikes or margin compression
  - `Text Hierarchy`: Crisp White (`#F8FAFC`) headlines, Cool Gray (`#94A3B8`) captions, Monospace tabular digits for monetary values.
- **Typography**: Clean modern sans-serif with high legibility, paired with formatted tabular numbers (`$124,500.00`) to prevent visual jitter on recalculation.

#### Navigation & Screen Hierarchy
The app features an M3 `NavigationBar` with 5 dedicated functional domains:

1. **Pipeline & LTV**:
   - **SaaS / Account Unit Economics**: ARPU (Average Revenue Per User/Account), Gross Margin %, Churn Rate % (Logo Churn & Net Revenue Churn), calculating LTV and LTV:CAC health ratio with visual benchmarks.
   - **Lead Conversion Funnel**: Interactive stages (Total Leads → MQLs → SQLs → Proposals/Opportunities → Closed Won Deals), showing stage-by-stage drop-off, overall conversion rate, and deal pipeline value.

2. **Financing & Purchase Modeler**:
   - **Credit & Loan Amortization**: Deal principal amount, down payment, interest rate %, loan term (months/years), with instant toggles for **Weekly, Monthly, Quarterly, and Yearly** installment schedules.
   - **Currency Cross-Exchange**: Real-time cross-currency converter between major enterprise currencies (USD, EUR, GBP, JPY, CAD, AUD, etc.) with custom rate override for locked client rate agreements.

3. **Margin & Markup Matrix**:
   - Dynamic real-time solver: Enter any two values among **Cost**, **Price**, **Gross Profit ($)**, **Margin (%)**, and **Markup (%)**; the engine calculates the remaining fields in real-time.
   - Quick presets for standard industry markups (e.g. 15%, 25%, 35%, 50%) and margin safety alerts.

4. **Analytics (ROI & PnL)**:
   - **ROI & Payback Calculator**: Initial Investment, Projected Gains, Net Profit, ROI %, and Payback Period in months.
   - **Deal PnL Modeler**: Comprehensive contract income statement (Gross Revenue, Direct Cost/COGS, Gross Margin, Sales/Delivery OPEX, EBITDA, and Net Margin %).
   - **Break-Even Analysis**: Fixed vs Variable costs to determine required sales units or billing volume.

5. **Deal Vault & Export**:
   - Saved deal scenarios and comparison workspace.
   - **Export as PDF**: High-quality PDF deal memo generator featuring company header, deal summary cards, financing schedule, and signature block.
   - **Cloud Sync**: Firebase Firestore integration with Google Sign-In for backing up and syncing deals across mobile devices.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Android PdfDocument vs External PDF Libraries**
  - *Chosen Approach*: Use Android's built-in `android.graphics.pdf.PdfDocument` with a custom canvas renderer.
  - *Why*: Zero third-party dependency bloat, 100% offline capability, pixel-perfect vector rendering matching corporate branding, and direct dispatch to Android's native print and share sheets (`Intent.ACTION_SEND`).
- **Decision 2: Two-Way Reactive Calculation Architecture**
  - *Chosen Approach*: Pure Kotlin math engine wrapped in unidirectional StateFlow models with calculation guards against divide-by-zero (e.g., zero churn, zero conversion).
  - *Why*: Instant recalculation on keystroke with no UI lag.
- **Decision 3: Cloud Sync with Google Sign-In & Offline Local Persistence**
  - *Chosen Approach*: Provision Firebase Firestore via `set_up_firebase` tool and integrate Google Sign-In with Jetpack Credential Manager. Fallback to local Room/state persistence when offline or unauthenticated.
  - *Why*: BDMs frequently present in low-connectivity conference rooms; changes must be persistent locally and synced automatically once online.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│  ┌───────────────┬────────────────┬────────────────┬────────────────┐  │
│  │ Pipeline & LTV│ Financing & FX │ Margin & Markup│ Analytics(PnL) │  │
│  └───────┬───────┴────────┬───────┴────────┬───────┴────────┬───────┘  │
│          │                │                │                │          │
│          ▼                ▼                ▼                ▼          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                BigDealViewModel & State Holders                  │  │
│  │   • PipelineState   • FinanceState   • MarginState  • PnlState   │  │
│  └───────────────────────────────┬──────────────────────────────────┘  │
└──────────────────────────────────┼─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Domain Calculation Engines                      │
│  ┌────────────────────┬────────────────────┬────────────────────────┐  │
│  │ LtvCalculator      │ AmortizationEngine │ MarginMarkupSolver     │  │
│  ├────────────────────┼────────────────────┼────────────────────────┤  │
│  │ FunnelCalculator   │ CurrencyConverter  │ PnlRoiCalculator       │  │
│  └────────────────────┴────────────────────┴────────────────────────┘  │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
               ┌───────────────────┴───────────────────┐
               ▼                                       ▼
┌───────────────────────────────┐     ┌──────────────────────────────────┐
│      PDF Generator Service    │     │   Firebase Firestore Sync Layer  │
│   • Vector Canvas Layout      │     │   • Google Sign-In via Credential│
│   • Table & Metric Cards      │     │   • User-scoped /deals collection│
│   • Share / Print Intent      │     │   • Offline snapshot cache       │
└───────────────────────────────┘     └──────────────────────────────────┘
```

#### Core Data Entities
- **DealProfile**: `id`, `dealName`, `clientName`, `dateCreated`, `currencyCode`, `dealValue`, `financingTerms`, `ltvMetrics`, `marginData`, `roiMetrics`.
- **AmortizationItem**: `periodNumber`, `installmentAmount`, `principalPart`, `interestPart`, `remainingBalance`.
- **CurrencyRate**: `code`, `name`, `symbol`, `rateToBaseUsd`.
- **FunnelStage**: `stageName`, `count`, `conversionRateFromPrevious`, `stageDropOff`.

---

### Step-by-Step Implementation Sequence

1. **Step 1: Firebase Provisioning**:
   - Invoke `set_up_firebase` tool (initiating user confirmation) for Firestore and Auth.
2. **Step 2: Dependencies & Theme Setup**:
   - Update `app/build.gradle.kts` with Google Sign-In (`androidx.credentials`, `googleid`), Firestore, and Kotlin Serialization.
   - Establish dark executive styling in `Theme.kt`, `Color.kt`, and `Type.kt`.
   - Update `metadata.json` and `res/values/strings.xml` with "BigDeal".
3. **Step 3: Core Calculation Engines**:
   - Implement math engines with unit tests: `LtvCalculator`, `FunnelCalculator`, `AmortizationEngine`, `CurrencyEngine`, `MarginMarkupSolver`, and `PnlRoiEngine`.
4. **Step 4: PDF Export Engine**:
   - Build `DealPdfExporter` rendering professional tables, summaries, and metrics directly to file cache and launching the Android share sheet.
5. **Step 5: Jetpack Compose UI Views**:
   - Implement the 5 primary tabs: Pipeline, Finance/FX, Margin/Markup, ROI/PnL, and Deal Vault/Export.
6. **Step 6: Cloud Sync & Auth Integration**:
   - Connect Firestore repository with Credential Manager Google Sign-In and local state.
7. **Step 7: Verification & Build**:
   - Run `compile_applet` and test all calculation flows.
