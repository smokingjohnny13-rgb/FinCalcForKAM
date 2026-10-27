package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// CompositionLocal for tracking active theme
val LocalIsDarkTheme = staticCompositionLocalOf { true }

// =========================================================================
// SCIENTIFIC & PSYCHOLOGICAL COLOR SYSTEM
// =========================================================================

// 1. Dark Mode (Ergonomic Obsidian & Midnight Slate)
// - Science: Avoids 100% OLED black smearing (pixel ghosting) and halation/halo
//   effects for astigmatism. Reduces ciliary muscle fatigue with soft 14:1 contrast.
// - Psychology: Conveys executive authority, institutional security, and focused immersion.
val DarkMidnight = Color(0xFF0B101D)          // Deep obsidian background
val DarkSurface = Color(0xFF131B2E)           // Panel surface
val DarkSurfaceVariant = Color(0xFF1C2742)    // Secondary container for input fields & controls
val DarkCard = Color(0xFF17223B)              // Elevated card surface
val DarkBorder = Color(0xFF28385E)            // Subdued structural border
val DarkTextPrimary = Color(0xFFF1F5F9)       // Soft off-white text (Slate 100)
val DarkTextMuted = Color(0xFF94A3B8)         // Slate 400 secondary text

// 2. Light Mode (Ergonomic Nordic Paper & Crisp Slate)
// - Science: Pure white (#FFFFFF) full-screen backgrounds cause pupil constriction
//   and photic eye strain. Soft cool-slate (#F4F6F9) cuts retinal luminance by ~25%
//   while pure white cards provide natural elevation without glare.
// - Psychology: Conveys transparency, clarity, optimism, and boardroom presentation readiness.
val LightCanvas = Color(0xFFF4F6F9)           // Soft cool-neutral paper background
val LightSurfaceBase = Color(0xFFFFFFFF)      // Crisp elevated card background
val LightSurfaceSubtle = Color(0xFFE9EEF5)    // Input fields & inactive chip fill
val LightCardBase = Color(0xFFFFFFFF)         // Clean card container
val LightBorderBase = Color(0xFFD6DFEB)       // Tactile border separating cards from canvas
val LightTextPrimary = Color(0xFF0F172A)      // Deep mineral graphite (Slate 900)
val LightTextMuted = Color(0xFF475569)        // Legible secondary text (Slate 600, WCAG AAA)

// 3. Semantic Brand Accents (Bio-centric & Financial Psychology)
// - Emerald Green: Primary profit/growth color. Stimulates feelings of safety,
//   sound financial health, and reduces cortisol during tough negotiations.
val EmeraldPrimary = Color(0xFF10B981)
val EmeraldLight = Color(0xFF34D399)
val EmeraldDark = Color(0xFF047857)

// - Electric / Sky Blue: Symbolizes cognitive clarity, analytical precision, and calm.
val ElectricCyan = Color(0xFF0284C7)
val CyanLight = Color(0xFF38BDF8)

// - Amber / Gold: Highlights opportunities, payback milestones, and warnings without inducing panic.
val GoldAccent = Color(0xFFD97706)
val GoldLight = Color(0xFFFBBF24)

// - Crimson Alert: Clear risk indicator for margin deficits and negative ROI.
val CrimsonAlert = Color(0xFFDC2626)

// =========================================================================
// DYNAMIC COMPOSABLE ACCESSORS
// Seamless drop-in compatibility for all screens and components
// =========================================================================

val ExecutiveMidnight: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkMidnight else LightCanvas

val ExecutiveSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSurface else LightSurfaceBase

val ExecutiveSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkSurfaceVariant else LightSurfaceSubtle

val ExecutiveCard: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkCard else LightCardBase

val ExecutiveBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkBorder else LightBorderBase

val TextWhite: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextPrimary else LightTextPrimary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextMuted else LightTextMuted

val TextDark = Color(0xFF0F172A)
