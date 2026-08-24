package com.example.maiplan.home.more.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.theme.AppColorTheme
import com.example.maiplan.theme.AppThemeMode
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.theme.LocalAppDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val dark = LocalAppDarkTheme.current
    val selectedTheme = AppThemeManager.selectedTheme
    val background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)

    Scaffold(
        containerColor = background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.theme_screen_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = (if (dark) Color(0xFF191D2E) else Color.White)
                        .copy(alpha = 0.96f),
                    titleContentColor = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
                    navigationIconContentColor = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
                ),
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 260.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = stringResource(R.string.theme_screen_heading),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.theme_screen_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085),
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                AppearanceModeSelector(
                    selectedMode = AppThemeManager.selectedMode,
                    onModeSelected = { AppThemeManager.selectMode(context, it) },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.theme_color_palette_title),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
                )
            }

            items(AppColorTheme.entries, key = { it.storageKey }) { theme ->
                ThemeOptionCard(
                    theme = theme,
                    selected = theme == selectedTheme,
                    onClick = { AppThemeManager.selectTheme(context, theme) },
                )
            }
        }
    }
}

@Composable
private fun AppearanceModeSelector(
    selectedMode: AppThemeMode,
    onModeSelected: (AppThemeMode) -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val foreground = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033)
    val muted = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (dark) Color(0xFF191D2E) else Color.White,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else Color(0xFFDDE3EC)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.theme_mode_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = foreground,
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = stringResource(R.string.theme_mode_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
            Spacer(Modifier.size(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppThemeMode.entries.forEach { mode ->
                    val selected = mode == selectedMode
                    ThemeModeOption(
                        mode = mode,
                        icon = when (mode) {
                            AppThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
                            AppThemeMode.LIGHT -> Icons.Rounded.LightMode
                            AppThemeMode.DARK -> Icons.Rounded.DarkMode
                        },
                        selected = selected,
                        onClick = { onModeSelected(mode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    mode: AppThemeMode,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = LocalAppDarkTheme.current
    val accent = AppThemeManager.selectedTheme.primary
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            accent
        } else if (dark) {
            Color(0xFF22283A)
        } else {
            Color(0xFFF5F7FA)
        },
        border = BorderStroke(
            1.dp,
            if (selected) accent else if (dark) Color(0xFF3A4257) else Color(0xFFDDE3EC),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else if (dark) Color(0xFFDCE3EE) else Color(0xFF4B5565),
                modifier = Modifier.size(21.dp),
            )
            Spacer(Modifier.size(5.dp))
            Text(
                text = stringResource(mode.nameRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    theme: AppColorTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val foreground = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033)
    val muted = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (dark) Color(0xFF191D2E) else Color.White,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) theme.primary else if (dark) Color(0xFF30374D) else Color(0xFFDDE3EC),
        ),
        shadowElevation = if (selected && !dark) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Brush.linearGradient(listOf(theme.primary, theme.primaryLight))),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(25.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
                ) {}
            }

            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(theme.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(theme.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = theme.primary,
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.theme_selected),
                        tint = Color.White,
                        modifier = Modifier.padding(5.dp).size(17.dp),
                    )
                }
            }
        }
    }
}
