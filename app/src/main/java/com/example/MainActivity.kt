package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.tr
import com.example.ui.theme.ElectricCyan
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.AuthRepository
import com.example.data.repository.DealRepository
import com.example.ui.components.CurrencySelectionDialog
import com.example.ui.screens.AnalyticsPnlScreen
import com.example.ui.screens.DealVaultScreen
import com.example.ui.screens.FinancingFxScreen
import com.example.ui.screens.LandedCostScreen
import com.example.ui.screens.MarginMarkupScreen
import com.example.ui.screens.PipelineLtvScreen
import com.example.ui.theme.BigDealTheme
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveMidnight
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.viewmodel.BigDealViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    FINANCING("Finance", Icons.Default.Payments),
    LANDED("Landed", Icons.Default.LocalShipping),
    MARGIN("Margin", Icons.Default.Percent),
    PIPELINE("Pipeline", Icons.Default.FilterAlt),
    ANALYTICS("Analytics", Icons.Default.Assessment),
    VAULT("Vault", Icons.Default.PictureAsPdf)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepo = AuthRepository(applicationContext)
        val dealRepo = DealRepository()

        setContent {
            BigDealTheme(darkTheme = true) {
                val viewModel: BigDealViewModel = viewModel {
                    BigDealViewModel(
                        dealRepository = dealRepo,
                        authRepository = authRepo
                    )
                }

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.userMessage.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                BigDealAppRoot(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigDealAppRoot(
    viewModel: BigDealViewModel,
    snackbarHostState: SnackbarHostState
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val tabs = NavigationTab.values()
    val activeDealTitle by viewModel.dealTitle.collectAsState()
    val activeClientName by viewModel.clientName.collectAsState()
    val context = LocalContext.current

    val currentLanguage by viewModel.appLanguage.collectAsState()

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            viewModel = viewModel,
            onDismissRequest = { showCurrencyDialog = false }
        )
    }

    CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ExecutiveMidnight,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "BD",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "BigDeal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = tr("BDM & KAM Executive Workstation", "Рабочее место BDM и KAM руководителя"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Language Selector Button (EN / RU)
                        Button(
                            onClick = { viewModel.toggleLanguage() },
                            colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("action_toggle_language")
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.EN) "🇺🇸 EN" else "🇷🇺 RU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Currency Selector Button
                        Button(
                            onClick = { showCurrencyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("action_select_currency")
                        ) {
                            Text(
                                text = "${activeCurrency.symbol} ${activeCurrency.code} ▾",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Quick PDF Export icon
                        IconButton(
                            onClick = { viewModel.exportPdf(context) },
                            modifier = Modifier.testTag("action_export_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = tr("Export PDF Proposal", "Экспорт КП в PDF"),
                                tint = EmeraldPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ExecutiveMidnight
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = ExecutiveSurface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(width = 0.5.dp, color = ExecutiveBorder)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_navigation_bar")
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTab == index
                        val tabLabel = when (tab) {
                            NavigationTab.FINANCING -> tr("Finance", "Финансы")
                            NavigationTab.LANDED -> tr("Landed", "Себестоимость")
                            NavigationTab.MARGIN -> tr("Margin", "Маржа")
                            NavigationTab.PIPELINE -> tr("Pipeline", "Воронка")
                            NavigationTab.ANALYTICS -> tr("Analytics", "Аналитика")
                            NavigationTab.VAULT -> tr("Vault", "Сделки")
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tabLabel
                                )
                            },
                            label = {
                                Text(
                                    text = tabLabel,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = selectedTab, label = "ScreenTransition") { tabIndex ->
                    when (tabIndex) {
                        0 -> FinancingFxScreen(viewModel = viewModel)
                        1 -> LandedCostScreen(
                            viewModel = viewModel,
                            onNavigateToMargin = { selectedTab = 2 }
                        )
                        2 -> MarginMarkupScreen(viewModel = viewModel)
                        3 -> PipelineLtvScreen(viewModel = viewModel)
                        4 -> AnalyticsPnlScreen(viewModel = viewModel)
                        5 -> DealVaultScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

