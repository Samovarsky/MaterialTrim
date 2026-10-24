package com.samovarsky.materialtrim.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private fun C(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

private fun light(vararg h: String): ColorScheme = lightColorScheme(
    primary = C(h[0]), onPrimary = C(h[1]), primaryContainer = C(h[2]), onPrimaryContainer = C(h[3]),
    secondary = C(h[4]), onSecondary = C(h[5]), secondaryContainer = C(h[6]), onSecondaryContainer = C(h[7]),
    tertiary = C(h[8]), onTertiary = C(h[9]), tertiaryContainer = C(h[10]), onTertiaryContainer = C(h[11]),
    error = C(h[12]), onError = C(h[13]), errorContainer = C(h[14]), onErrorContainer = C(h[15]),
    background = C(h[16]), onBackground = C(h[17]), surface = C(h[18]), onSurface = C(h[19]),
    surfaceVariant = C(h[20]), onSurfaceVariant = C(h[21]), outline = C(h[22]), outlineVariant = C(h[23]),
    scrim = C(h[24]), inverseSurface = C(h[25]), inverseOnSurface = C(h[26]), inversePrimary = C(h[27]),
    surfaceTint = C(h[28]), surfaceDim = C(h[29]), surfaceBright = C(h[30]),
    surfaceContainerLowest = C(h[31]), surfaceContainerLow = C(h[32]), surfaceContainer = C(h[33]),
    surfaceContainerHigh = C(h[34]), surfaceContainerHighest = C(h[35])
)

private fun dark(vararg h: String): ColorScheme = darkColorScheme(
    primary = C(h[0]), onPrimary = C(h[1]), primaryContainer = C(h[2]), onPrimaryContainer = C(h[3]),
    secondary = C(h[4]), onSecondary = C(h[5]), secondaryContainer = C(h[6]), onSecondaryContainer = C(h[7]),
    tertiary = C(h[8]), onTertiary = C(h[9]), tertiaryContainer = C(h[10]), onTertiaryContainer = C(h[11]),
    error = C(h[12]), onError = C(h[13]), errorContainer = C(h[14]), onErrorContainer = C(h[15]),
    background = C(h[16]), onBackground = C(h[17]), surface = C(h[18]), onSurface = C(h[19]),
    surfaceVariant = C(h[20]), onSurfaceVariant = C(h[21]), outline = C(h[22]), outlineVariant = C(h[23]),
    scrim = C(h[24]), inverseSurface = C(h[25]), inverseOnSurface = C(h[26]), inversePrimary = C(h[27]),
    surfaceTint = C(h[28]), surfaceDim = C(h[29]), surfaceBright = C(h[30]),
    surfaceContainerLowest = C(h[31]), surfaceContainerLow = C(h[32]), surfaceContainer = C(h[33]),
    surfaceContainerHigh = C(h[34]), surfaceContainerHighest = C(h[35])
)

// ===== Розовая (seed #E67AE8) — ОСНОВНАЯ =====
private val PinkLight = light(
    "#7D4E7C", "#FFFFFF", "#FFD6F9", "#633663",
    "#6C586A", "#FFFFFF", "#F6DBF0", "#544152",
    "#6B538C", "#FFFFFF", "#EDDCFF", "#523C73",
    "#904A45", "#FFFFFF", "#FFDAD6", "#73332F",
    "#FFF7FA", "#201A1F", "#FFF7FA", "#201A1F",
    "#EDDEE8", "#4D444B", "#7F747C", "#D0C3CC",
    "#000000", "#352E33", "#FAEDF4", "#EEB4EA", "#7D4E7C",
    "#E2D7DD", "#FFF7FA", "#FFFFFF", "#FCF0F7",
    "#F7EBF1", "#F1E5EC", "#EBDFE6"
)
private val PinkDark = dark(
    "#EEB4EA", "#4A204B", "#633663", "#FFD6F9",
    "#D9BFD4", "#3C2B3B", "#544152", "#F6DBF0",
    "#D7BBFB", "#3B255A", "#523C73", "#EDDCFF",
    "#FFB3AD", "#571E1B", "#73332F", "#FFDAD6",
    "#171216", "#EBDFE6", "#171216", "#EBDFE6",
    "#4D444B", "#D0C3CC", "#998D96", "#4D444B",
    "#000000", "#EBDFE6", "#352E33", "#7D4E7C", "#EEB4EA",
    "#171216", "#3E373C", "#120D11", "#201A1F",
    "#241E23", "#2E282D", "#393338"
)

// ===== Зелёная (seed #0FBC6C) =====
private val GreenLight = light(
    "#2E6A44", "#FFFFFF", "#B1F1C1", "#11512E",
    "#346940", "#FFFFFF", "#B5F1BC", "#1A512A",
    "#006A61", "#FFFFFF", "#9EF2E5", "#005048",
    "#BA1A1A", "#FFFFFF", "#FFDAD6", "#93000A",
    "#F6FBF3", "#181D18", "#F6FBF3", "#181D18",
    "#DDE5DB", "#414942", "#717971", "#C0C9BF",
    "#000000", "#2C322D", "#EDF2EB", "#96D5A7", "#2E6A44",
    "#D6DBD4", "#F6FBF3", "#FFFFFF", "#F0F5ED",
    "#EAEFE8", "#E5EAE2", "#DFE4DC"
)
private val GreenDark = dark(
    "#96D5A7", "#00391C", "#11512E", "#B1F1C1",
    "#9AD4A1", "#003916", "#1A512A", "#B5F1BC",
    "#82D5C9", "#003732", "#005048", "#9EF2E5",
    "#FFB4AB", "#690005", "#93000A", "#FFDAD6",
    "#101510", "#DFE4DC", "#101510", "#DFE4DC",
    "#414942", "#C0C9BF", "#8B938A", "#414942",
    "#000000", "#DFE4DC", "#2C322D", "#2E6A44", "#96D5A7",
    "#101510", "#353A35", "#0A0F0B", "#181D18",
    "#1C211C", "#262B27", "#313631"
)

// ===== Оливковая (обновлена, seed #63A002) =====
private val OliveLight = light(
    "#4C662B", "#FFFFFF", "#CDEDA3", "#354E16",
    "#586249", "#FFFFFF", "#DCE7C8", "#404A33",
    "#4C662B", "#FFFFFF", "#CDEDA3", "#354E16",
    "#BA1A1A", "#FFFFFF", "#FFDAD6", "#93000A",
    "#F9FAEF", "#1A1C16", "#F9FAEF", "#1A1C16",
    "#E1E4D5", "#44483D", "#75796C", "#C5C8BA",
    "#000000", "#2F312A", "#F1F2E6", "#B1D18A", "#4C662B",
    "#DADBD0", "#F9FAEF", "#FFFFFF", "#F3F4E9",
    "#EEEFE3", "#E8E9DE", "#E2E3D8"
)
private val OliveDark = dark(
    "#B1D18A", "#1F3701", "#354E16", "#CDEDA3",
    "#BFCBAD", "#2A331E", "#404A33", "#DCE7C8",
    "#B1D18A", "#1F3701", "#354E16", "#CDEDA3",
    "#FFB4AB", "#690005", "#93000A", "#FFDAD6",
    "#12140E", "#E2E3D8", "#12140E", "#E2E3D8",
    "#44483D", "#C5C8BA", "#8F9285", "#44483D",
    "#000000", "#E2E3D8", "#2F312A", "#4C662B", "#B1D18A",
    "#12140E", "#383A32", "#0C0F09", "#1A1C16",
    "#1E201A", "#282B24", "#33362E"
)

// ===== Жёлтая (seed #E8C000) =====
private val YellowLight = light(
    "#705D0D", "#FFFFFF", "#FCE186", "#554500",
    "#815511", "#FFFFFF", "#FFDDB6", "#643F00",
    "#725C0C", "#FFFFFF", "#FFE087", "#574500",
    "#BA1A1A", "#FFFFFF", "#FFDAD6", "#93000A",
    "#FFF8EF", "#1E1B13", "#FFF8EF", "#1E1B13",
    "#EAE2CF", "#4B4639", "#7D7767", "#CEC6B4",
    "#000000", "#343027", "#F8F0E2", "#DFC56D", "#705D0D",
    "#E0D9CC", "#FFF8EF", "#FFFFFF", "#FBF3E5",
    "#F5EDDF", "#EFE7DA", "#E9E2D4"
)
private val YellowDark = dark(
    "#DFC56D", "#3B2F00", "#554500", "#FCE186",
    "#F6BC70", "#462B00", "#643F00", "#FFDDB6",
    "#E2C46D", "#3C2F00", "#574500", "#FFE087",
    "#FFB4AB", "#690005", "#93000A", "#FFDAD6",
    "#16130B", "#E9E2D4", "#16130B", "#E9E2D4",
    "#4B4639", "#CEC6B4", "#979080", "#4B4639",
    "#000000", "#E9E2D4", "#343027", "#705D0D", "#DFC56D",
    "#16130B", "#3D392F", "#100E07", "#1E1B13",
    "#221F17", "#2D2A21", "#38342B"
)

// ===== Коричневая (обновлена, seed #E46111) =====
private val BrownLight = light(
    "#8E4D2E", "#FFFFFF", "#FFDBCC", "#713619",
    "#76574A", "#FFFFFF", "#FFDBCC", "#5D4033",
    "#88511E", "#FFFFFF", "#FFDCC2", "#6C3A06",
    "#BA1A1A", "#FFFFFF", "#FFDAD6", "#93000A",
    "#FFF8F6", "#221A16", "#FFF8F6", "#221A16",
    "#F4DED5", "#52443D", "#85736C", "#D8C2BA",
    "#000000", "#382E2A", "#FFEDE6", "#FFB694", "#8E4D2E",
    "#E8D6D0", "#FFF8F6", "#FFFFFF", "#FFF1EC",
    "#FCEAE3", "#F6E5DE", "#F1DFD8"
)
private val BrownDark = dark(
    "#FFB694", "#542105", "#713619", "#FFDBCC",
    "#E6BEAD", "#442A1F", "#5D4033", "#FFDBCC",
    "#FFB77C", "#4D2700", "#6C3A06", "#FFDCC2",
    "#FFB4AB", "#690005", "#93000A", "#FFDAD6",
    "#1A120E", "#F1DFD8", "#1A120E", "#F1DFD8",
    "#52443D", "#D8C2BA", "#A08D85", "#52443D",
    "#000000", "#F1DFD8", "#382E2A", "#8E4D2E", "#FFB694",
    "#1A120E", "#423732", "#140C09", "#221A16",
    "#271E1A", "#322824", "#3D332E"
)

// ===== Красная (seed #B71500) =====
private val RedLight = light(
    "#904B3E", "#FFFFFF", "#FFDAD4", "#733429",
    "#904B3E", "#FFFFFF", "#FFDAD4", "#733429",
    "#904B3E", "#FFFFFF", "#FFDAD4", "#733429",
    "#BA1A1A", "#FFFFFF", "#FFDAD6", "#93000A",
    "#FFF8F6", "#231918", "#FFF8F6", "#231918",
    "#F5DDD9", "#534340", "#857370", "#D8C2BE",
    "#000000", "#392E2C", "#FFEDE9", "#FFB4A6", "#904B3E",
    "#E8D6D3", "#FFF8F6", "#FFFFFF", "#FFF0EE",
    "#FCEAE6", "#F7E4E1", "#F1DFDB"
)
private val RedDark = dark(
    "#FFB4A6", "#561E15", "#733429", "#FFDAD4",
    "#FFB4A6", "#561E15", "#733429", "#FFDAD4",
    "#FFB4A6", "#561E15", "#733429", "#FFDAD4",
    "#FFB4AB", "#690005", "#93000A", "#FFDAD6",
    "#1A1110", "#F1DFDB", "#1A1110", "#F1DFDB",
    "#534340", "#D8C2BE", "#A08C89", "#534340",
    "#000000", "#F1DFDB", "#392E2C", "#904B3E", "#FFB4A6",
    "#1A1110", "#423735", "#140C0B", "#231918",
    "#271D1C", "#322826", "#3D3230"
)

// ===== Голубая (seed #3CDDDA) =====
private val CyanLight = light(
    "#006A68", "#FFFFFF", "#9CF1EE", "#00504E",
    "#06677E", "#FFFFFF", "#B6EAFF", "#004E60",
    "#0F6681", "#FFFFFF", "#BCE9FF", "#004D63",
    "#BA1A1A", "#FFFFFF", "#FFDAD6", "#93000A",
    "#F4FBF9", "#161D1C", "#F4FBF9", "#161D1C",
    "#DAE5E3", "#3F4948", "#6F7978", "#BEC9C7",
    "#000000", "#2B3231", "#ECF2F1", "#80D5D2", "#006A68",
    "#D5DBDA", "#F4FBF9", "#FFFFFF", "#EFF5F4",
    "#E9EFEE", "#E3E9E8", "#DDE4E3"
)
private val CyanDark = dark(
    "#80D5D2", "#003736", "#00504E", "#9CF1EE",
    "#87D1EB", "#003543", "#004E60", "#B6EAFF",
    "#8AD0EE", "#003545", "#004D63", "#BCE9FF",
    "#FFB4AB", "#690005", "#93000A", "#FFDAD6",
    "#0E1514", "#DDE4E3", "#0E1514", "#DDE4E3",
    "#3F4948", "#BEC9C7", "#889392", "#3F4948",
    "#000000", "#DDE4E3", "#2B3231", "#006A68", "#80D5D2",
    "#0E1514", "#343A3A", "#090F0F", "#161D1C",
    "#1A2120", "#252B2B", "#2F3636"
)

private val Presets: Map<String, Pair<ColorScheme, ColorScheme>> = mapOf(
    "pink" to (PinkLight to PinkDark),
    "green" to (GreenLight to GreenDark),
    "olive" to (OliveLight to OliveDark),
    "yellow" to (YellowLight to YellowDark),
    "brown" to (BrownLight to BrownDark),
    "red" to (RedLight to RedDark),
    "cyan" to (CyanLight to CyanDark)
)

@Composable
fun MusicEditorTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = false,
    colorPreset: String = "pink",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val pair = Presets[colorPreset] ?: Presets.getValue("pink")
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        else -> if (darkTheme) pair.second else pair.first
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}