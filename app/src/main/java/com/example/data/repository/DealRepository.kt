package com.example.data.repository

import android.util.Log
import com.example.data.model.PaymentFrequency
import com.example.data.model.SavedDeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DealRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val _deals = MutableStateFlow<List<SavedDeal>>(emptyList())
    val deals: StateFlow<List<SavedDeal>> = _deals.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var dealsListener: ListenerRegistration? = null

    init {
        // Seed default high-value enterprise templates
        _deals.value = listOf(
            SavedDeal(
                id = "deal-enterprise-saas-01",
                title = "Global Enterprise Platform License",
                clientName = "OmniTech Solutions Inc.",
                currencyCode = "USD",
                dealValue = 180000.0,
                interestRatePct = 5.5,
                tenureMonths = 36,
                frequency = PaymentFrequency.MONTHLY,
                periodicPayment = 5437.42,
                ltv = 240000.0,
                ltvCacRatio = 4.8,
                marginPct = 52.0,
                markupPct = 108.3,
                roiPct = 168.0,
                paybackMonths = 7.1,
                updatedAt = System.currentTimeMillis() - 86400000,
                createdBy = "local_executive"
            ),
            SavedDeal(
                id = "deal-emea-expansion-02",
                title = "EMEA Multi-Region Cloud Migration",
                clientName = "Vanguard Financial AG",
                currencyCode = "EUR",
                dealValue = 95000.0,
                interestRatePct = 4.8,
                tenureMonths = 24,
                frequency = PaymentFrequency.QUARTERLY,
                periodicPayment = 12510.35,
                ltv = 150000.0,
                ltvCacRatio = 3.6,
                marginPct = 44.5,
                markupPct = 80.2,
                roiPct = 132.5,
                paybackMonths = 9.2,
                updatedAt = System.currentTimeMillis() - 172800000,
                createdBy = "local_executive"
            ),
            SavedDeal(
                id = "deal-apac-retail-03",
                title = "APAC Retail Core Transformation",
                clientName = "Aegis Retail Network",
                currencyCode = "GBP",
                dealValue = 240000.0,
                interestRatePct = 6.2,
                tenureMonths = 48,
                frequency = PaymentFrequency.ANNUALLY,
                periodicPayment = 69820.10,
                ltv = 360000.0,
                ltvCacRatio = 5.4,
                marginPct = 58.0,
                markupPct = 138.1,
                roiPct = 210.0,
                paybackMonths = 5.8,
                updatedAt = System.currentTimeMillis() - 259200000,
                createdBy = "local_executive"
            )
        )
    }

    fun startFirestoreSync(scope: CoroutineScope) {
        val uid = auth.currentUser?.uid ?: return
        dealsListener?.remove()

        dealsListener = firestore.collection("users").document(uid).collection("deals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("DealRepo", "Firestore deals listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val remoteDeals = snapshot.documents.mapNotNull { doc ->
                        try {
                            SavedDeal(
                                id = doc.getString("id") ?: doc.id,
                                title = doc.getString("title") ?: "Untitled Deal",
                                clientName = doc.getString("clientName") ?: "Client",
                                currencyCode = doc.getString("currencyCode") ?: "USD",
                                dealValue = doc.getDouble("dealValue") ?: 0.0,
                                interestRatePct = doc.getDouble("interestRatePct") ?: 0.0,
                                tenureMonths = (doc.getLong("tenureMonths") ?: 12).toInt(),
                                frequency = PaymentFrequency.valueOf(doc.getString("frequency") ?: PaymentFrequency.MONTHLY.name),
                                periodicPayment = doc.getDouble("periodicPayment") ?: 0.0,
                                ltv = doc.getDouble("ltv") ?: 0.0,
                                ltvCacRatio = doc.getDouble("ltvCacRatio") ?: 0.0,
                                marginPct = doc.getDouble("marginPct") ?: 0.0,
                                markupPct = doc.getDouble("markupPct") ?: 0.0,
                                roiPct = doc.getDouble("roiPct") ?: 0.0,
                                paybackMonths = doc.getDouble("paybackMonths") ?: 0.0,
                                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                                createdBy = uid
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (remoteDeals.isNotEmpty()) {
                        // Merge remote deals with local presets
                        val merged = (remoteDeals + _deals.value).distinctBy { it.id }
                        _deals.value = merged.sortedByDescending { it.updatedAt }
                    }
                }
            }
    }

    fun stopFirestoreSync() {
        dealsListener?.remove()
        dealsListener = null
    }

    suspend fun saveDeal(deal: SavedDeal): Result<Unit> {
        return try {
            _isSyncing.value = true
            // Update local state immediately
            val current = _deals.value.toMutableList()
            val existingIndex = current.indexOfFirst { it.id == deal.id }
            if (existingIndex >= 0) {
                current[existingIndex] = deal
            } else {
                current.add(0, deal)
            }
            _deals.value = current.sortedByDescending { it.updatedAt }

            // Sync to Firestore if authenticated
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val data = hashMapOf(
                    "id" to deal.id,
                    "title" to deal.title,
                    "clientName" to deal.clientName,
                    "currencyCode" to deal.currencyCode,
                    "dealValue" to deal.dealValue,
                    "interestRatePct" to deal.interestRatePct,
                    "tenureMonths" to deal.tenureMonths,
                    "frequency" to deal.frequency.name,
                    "periodicPayment" to deal.periodicPayment,
                    "ltv" to deal.ltv,
                    "ltvCacRatio" to deal.ltvCacRatio,
                    "marginPct" to deal.marginPct,
                    "markupPct" to deal.markupPct,
                    "roiPct" to deal.roiPct,
                    "paybackMonths" to deal.paybackMonths,
                    "updatedAt" to deal.updatedAt,
                    "createdBy" to uid
                )
                firestore.collection("users").document(uid).collection("deals")
                    .document(deal.id)
                    .set(data)
                    .await()
            }
            _isSyncing.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e("DealRepo", "Failed to sync deal to Firestore: ${e.message}", e)
            // Local save still succeeded
            Result.success(Unit)
        }
    }

    suspend fun deleteDeal(dealId: String): Result<Unit> {
        return try {
            val current = _deals.value.toMutableList()
            current.removeAll { it.id == dealId }
            _deals.value = current

            val uid = auth.currentUser?.uid
            if (uid != null) {
                firestore.collection("users").document(uid).collection("deals")
                    .document(dealId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DealRepo", "Failed to delete deal from Firestore: ${e.message}", e)
            Result.success(Unit)
        }
    }
}
