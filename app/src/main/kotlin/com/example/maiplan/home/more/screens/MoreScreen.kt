package com.example.maiplan.home.more.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import com.example.maiplan.theme.LocalAppDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.maiplan.BuildConfig
import com.example.maiplan.R
import com.example.maiplan.category.CategoryActivity
import com.example.maiplan.home.clock.HomeClockPreferences
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.utils.BaseActivity
import com.example.maiplan.utils.common.UserSession

private val MorePrimary: Color get() = AppThemeManager.selectedTheme.primary
private val MorePrimaryLight: Color get() = AppThemeManager.selectedTheme.primaryLight
private val MoreTeal = Color(0xFF14B8A6)
private val MoreInk = Color(0xFF172033)
private val MoreMuted = Color(0xFF667085)
private val MoreBorder = Color(0xFFDDE3EC)
private val MoreDanger = Color(0xFFD92D3A)

@Composable
fun MoreScreen(
    rootNavController: NavHostController,
    onThemeClick: () -> Unit,
    onClockClick: () -> Unit,
) {
    val context = LocalContext.current
    val onLogoutClick = rememberLogoutHandler()

    MoreScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { MoreTopBar() },
            bottomBar = { HomeNavigationBar(rootNavController, context) },
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
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    MoreIntro()
                    MoreProfileCard()

                    MoreSection(title = stringResource(R.string.more_workspace_section)) {
                        MoreActionRow(
                            title = stringResource(R.string.category_management),
                            subtitle = stringResource(R.string.more_categories_subtitle),
                            icon = Icons.Rounded.Category,
                            onClick = { openActivity(context, CategoryActivity::class.java) },
                        )
                        MoreSectionDivider()
                        MoreActionRow(
                            title = stringResource(R.string.sync),
                            subtitle = stringResource(R.string.more_sync_subtitle),
                            icon = Icons.Rounded.CloudSync,
                            accent = MoreTeal,
                            onClick = { SyncScheduler.runOneTimeSync(context) },
                        )
                    }

                    MoreSection(title = stringResource(R.string.more_preferences_section)) {
                        MoreActionRow(
                            title = stringResource(R.string.app_theme),
                            subtitle = stringResource(R.string.more_theme_subtitle),
                            icon = Icons.Rounded.Palette,
                            onClick = onThemeClick,
                        )
                        MoreSectionDivider()
                        MoreActionRow(
                            title = stringResource(R.string.home_clock),
                            subtitle = stringResource(
                                R.string.more_clock_subtitle,
                                stringResource(HomeClockPreferences.selectedStyle.nameRes),
                                stringResource(HomeClockPreferences.selectedHourFormat.nameRes),
                            ),
                            icon = Icons.Rounded.Schedule,
                            onClick = onClockClick,
                        )
                    }

                    MoreSection(title = stringResource(R.string.more_account_section)) {
                        MoreActionRow(
                            title = stringResource(R.string.logout),
                            subtitle = stringResource(R.string.more_logout_subtitle),
                            icon = Icons.AutoMirrored.Rounded.Logout,
                            accent = MoreDanger,
                            destructive = true,
                            onClick = onLogoutClick,
                        )
                    }

                    MoreVersionFooter()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MoreScreenBackground(content: @Composable () -> Unit) {
    val dark = LocalAppDarkTheme.current
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        if (!(isTablet && dark)) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            MorePrimary.copy(alpha = if (dark) 0.18f else 0.09f),
                            Color.Transparent,
                        ),
                    ),
                    center = Offset(size.width * 0.06f, size.height * 0.02f),
                    radius = size.minDimension * 0.68f,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            MoreTeal.copy(alpha = if (dark) 0.10f else 0.06f),
                            Color.Transparent,
                        ),
                    ),
                    center = Offset(size.width, size.height),
                    radius = size.minDimension * 0.50f,
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreTopBar() {
    val dark = LocalAppDarkTheme.current
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.more),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFFF5F7FB) else MoreInk,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.3).sp,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = (if (dark) Color(0xFF191D2E) else Color.White).copy(alpha = 0.94f),
        ),
    )
}

@Composable
private fun MoreIntro() {
    val dark = LocalAppDarkTheme.current
    Column {
        Text(
            text = stringResource(R.string.more_heading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (dark) Color(0xFFF5F7FB) else MoreInk,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.more_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = if (dark) Color(0xFFAEB7C9) else MoreMuted,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun MoreProfileCard() {
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else MoreInk
    val muted = if (dark) Color(0xFFAEB7C9) else MoreMuted
    val username = UserSession.username.orEmpty().ifBlank {
        stringResource(R.string.more_default_user)
    }
    val email = UserSession.email.orEmpty().ifBlank {
        stringResource(R.string.more_email_unavailable)
    }
    val initial = username.firstOrNull()?.uppercaseChar()?.toString() ?: "M"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surface,
        shadowElevation = if (dark) 0.dp else 4.dp,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else MoreBorder),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(MorePrimary, MorePrimaryLight))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                shape = CircleShape,
                color = MoreTeal.copy(alpha = if (dark) 0.18f else 0.10f),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = stringResource(R.string.more_signed_in),
                    tint = MoreTeal,
                    modifier = Modifier.padding(9.dp).size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun MoreSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (dark) Color(0xFFAEB7C9) else MoreMuted,
            letterSpacing = 0.8.sp,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = if (dark) Color(0xFF191D2E) else Color.White,
            border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else MoreBorder),
            content = { Column(content = content) },
        )
    }
}

@Composable
private fun MoreActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    accent: Color = MorePrimary,
    destructive: Boolean = false,
) {
    val dark = LocalAppDarkTheme.current
    val titleColor = when {
        destructive && dark -> Color(0xFFFFB4B8)
        destructive -> MoreDanger
        dark -> Color(0xFFF5F7FB)
        else -> MoreInk
    }
    val muted = if (dark) Color(0xFFAEB7C9) else MoreMuted

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = if (dark) 0.22f else 0.11f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp),
                        tint = if (destructive && dark) Color(0xFFFFB4B8) else accent,
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = if (destructive) titleColor else muted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun MoreSectionDivider() {
    val dark = LocalAppDarkTheme.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 74.dp, end = 16.dp),
        color = if (dark) Color(0xFF30374D) else MoreBorder.copy(alpha = 0.80f),
    )
}

@Composable
private fun MoreVersionFooter() {
    Text(
        text = stringResource(R.string.more_version, BuildConfig.VERSION_NAME),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = if (LocalAppDarkTheme.current) Color(0xFF818AA0) else MoreMuted,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun rememberLogoutHandler(): () -> Unit {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val dark = LocalAppDarkTheme.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MoreDanger.copy(alpha = if (dark) 0.20f else 0.10f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = if (dark) Color(0xFFFFB4B8) else MoreDanger,
                        modifier = Modifier.padding(12.dp).size(24.dp),
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.more_logout_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color(0xFFF5F7FB) else MoreInk,
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.more_logout_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dark) Color(0xFFAEB7C9) else MoreMuted,
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        (context as? BaseActivity)?.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (dark) Color(0xFFFFB4B8) else MoreDanger,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.logout),
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = if (dark) Color(0xFFAEB7C9) else MoreMuted,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            shape = RoundedCornerShape(26.dp),
            containerColor = if (dark) Color(0xFF191D2E) else Color.White,
        )
    }

    return { showDialog = true }
}

private fun openActivity(context: Context, activity: Class<out Activity>) {
    context.startActivity(Intent(context, activity))
}
