package com.example.maiplan.home.more.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DesktopWindows
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.home.clock.HomeClockHourFormat
import com.example.maiplan.home.clock.HomeClockPreferences
import com.example.maiplan.home.clock.HomeClockStyle
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.theme.LocalAppDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockSelectionScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val dark = LocalAppDarkTheme.current
    val background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)

    Scaffold(
        containerColor = background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.clock_screen_title),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ClockSettingsIntro()

                ClockPreferenceSection(
                    title = stringResource(R.string.clock_style_title),
                    subtitle = stringResource(R.string.clock_style_subtitle),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ClockOption(
                            label = stringResource(R.string.clock_style_digital),
                            preview = "10:24",
                            icon = Icons.Rounded.DesktopWindows,
                            selected = HomeClockPreferences.selectedStyle == HomeClockStyle.DIGITAL,
                            onClick = {
                                HomeClockPreferences.selectStyle(context, HomeClockStyle.DIGITAL)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        ClockOption(
                            label = stringResource(R.string.clock_style_analog),
                            preview = null,
                            icon = Icons.Rounded.AccessTime,
                            selected = HomeClockPreferences.selectedStyle == HomeClockStyle.ANALOG,
                            onClick = {
                                HomeClockPreferences.selectStyle(context, HomeClockStyle.ANALOG)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                ClockPreferenceSection(
                    title = stringResource(R.string.clock_format_title),
                    subtitle = stringResource(R.string.clock_format_subtitle),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ClockOption(
                            label = stringResource(R.string.clock_format_12_hour),
                            preview = stringResource(R.string.clock_format_12_preview),
                            selected = HomeClockPreferences.selectedHourFormat ==
                                HomeClockHourFormat.TWELVE_HOUR,
                            onClick = {
                                HomeClockPreferences.selectHourFormat(
                                    context,
                                    HomeClockHourFormat.TWELVE_HOUR,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                        ClockOption(
                            label = stringResource(R.string.clock_format_24_hour),
                            preview = stringResource(R.string.clock_format_24_preview),
                            selected = HomeClockPreferences.selectedHourFormat ==
                                HomeClockHourFormat.TWENTY_FOUR_HOUR,
                            onClick = {
                                HomeClockPreferences.selectHourFormat(
                                    context,
                                    HomeClockHourFormat.TWENTY_FOUR_HOUR,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockSettingsIntro() {
    val dark = LocalAppDarkTheme.current
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = stringResource(R.string.clock_screen_heading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = stringResource(R.string.clock_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085),
        )
    }
}

@Composable
private fun ClockPreferenceSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (dark) Color(0xFF191D2E) else Color.White,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else Color(0xFFDDE3EC)),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085),
            )
            Spacer(Modifier.size(15.dp))
            content()
        }
    }
}

@Composable
private fun ClockOption(
    label: String,
    preview: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val dark = LocalAppDarkTheme.current
    val accent = AppThemeManager.selectedTheme.primary
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = when {
            selected -> accent
            dark -> Color(0xFF22283A)
            else -> Color(0xFFF5F7FA)
        },
        border = BorderStroke(
            1.dp,
            if (selected) accent else if (dark) Color(0xFF3A4257) else Color(0xFFDDE3EC),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else if (dark) Color(0xFFDCE3EE) else Color(0xFF4B5565),
                    modifier = Modifier.size(25.dp),
                )
            } else if (preview != null) {
                Text(
                    text = preview,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else if (dark) Color(0xFFDCE3EE) else Color(0xFF4B5565),
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else if (dark) Color(0xFFF5F7FB) else Color(0xFF172033),
            )
        }
    }
}
