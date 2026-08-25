package com.example.maiplan.home.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.maiplan.home.clock.HomeClockHourFormat
import com.example.maiplan.home.clock.HomeClockPreferences
import com.example.maiplan.home.clock.HomeClockStyle
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.theme.AppThemeManager
import com.example.maiplan.theme.LocalAppDarkTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(rootNavController: NavHostController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val dark = LocalAppDarkTheme.current
    val primary = AppThemeManager.selectedTheme.primary
    val primaryLight = AppThemeManager.selectedTheme.primaryLight
    val background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)
    val foreground = if (dark) Color(0xFFF5F7FB) else Color(0xFF172033)
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            currentTime = now
            delay(1_000L - now.mod(1_000L))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { HomeNavigationBar(rootNavController, context) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .padding(
                        top = if (isTablet) 46.dp else 30.dp,
                        start = if (isTablet) 34.dp else 20.dp,
                        end = if (isTablet) 34.dp else 20.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = if (isTablet) 720.dp else 460.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ClockAura(
                        primary = primary,
                        primaryLight = primaryLight,
                        dark = dark,
                        isTablet = isTablet,
                    ) {
                        when (HomeClockPreferences.selectedStyle) {
                            HomeClockStyle.DIGITAL -> DigitalClock(
                                currentTime = currentTime,
                                format = HomeClockPreferences.selectedHourFormat,
                                primary = primary,
                                foreground = foreground,
                                isTablet = isTablet,
                            )
                            HomeClockStyle.ANALOG -> AnalogClock(
                                currentTime = currentTime,
                                primary = primary,
                                foreground = foreground,
                                dark = dark,
                                isTablet = isTablet,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockAura(
    primary: Color,
    primaryLight: Color,
    dark: Boolean,
    isTablet: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val glowCenter = Offset(size.width * 0.5f, size.height * 0.48f)
            val glowRadius = size.minDimension * if (isTablet) 0.72f else 1.08f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = if (dark) 0.18f else 0.25f),
                        primaryLight.copy(alpha = if (dark) 0.08f else 0.13f),
                        Color.Transparent,
                    ),
                    center = glowCenter,
                    radius = glowRadius,
                ),
                center = glowCenter,
                radius = glowRadius,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isTablet) 40.dp else 20.dp,
                    vertical = if (isTablet) 36.dp else 22.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}

@Composable
private fun DigitalClock(
    currentTime: Long,
    format: HomeClockHourFormat,
    primary: Color,
    foreground: Color,
    isTablet: Boolean,
) {
    Icon(
        imageVector = Icons.Rounded.Schedule,
        contentDescription = null,
        tint = primary,
        modifier = Modifier.size(if (isTablet) 34.dp else 28.dp),
    )
    Spacer(Modifier.height(if (isTablet) 18.dp else 12.dp))
    Text(
        text = formatDigitalTime(currentTime, format),
        style = MaterialTheme.typography.displayLarge.merge(
            TextStyle(
                fontSize = if (isTablet) 82.sp else 57.sp,
                lineHeight = if (isTablet) 90.sp else 64.sp,
                shadow = Shadow(
                    color = primary.copy(alpha = 0.22f),
                    offset = Offset(0f, if (isTablet) 5f else 3f),
                    blurRadius = if (isTablet) 16f else 10f,
                ),
            ),
        ),
        fontWeight = FontWeight.Bold,
        color = foreground,
        letterSpacing = (-1.5).sp,
    )
}

@Composable
private fun AnalogClock(
    currentTime: Long,
    primary: Color,
    foreground: Color,
    dark: Boolean,
    isTablet: Boolean,
) {
    val localTime = Instant.ofEpochMilli(currentTime)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    val second = localTime.second.toFloat()
    val minute = localTime.minute + second / 60f
    val hour = localTime.hour.mod(12) + minute / 60f
    val density = LocalDensity.current
    val numberPaint = remember(foreground, density, isTablet) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foreground.toArgb()
            textSize = with(density) { (if (isTablet) 19.sp else 15.sp).toPx() }
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    Surface(
        modifier = Modifier
            .size(if (isTablet) 270.dp else 200.dp)
            .shadow(
                elevation = if (isTablet) 13.dp else 9.dp,
                shape = CircleShape,
                ambientColor = primary.copy(alpha = if (dark) 0.20f else 0.14f),
                spotColor = primary.copy(alpha = if (dark) 0.28f else 0.22f),
            ),
        shape = CircleShape,
        color = if (dark) Color(0xFF191D2E) else Color.White,
        shadowElevation = if (dark) 1.dp else 3.dp,
        border = BorderStroke(
            if (isTablet) 1.5.dp else 1.dp,
            primary.copy(alpha = if (dark) 0.48f else 0.26f),
        ),
    ) {
        Canvas(modifier = Modifier.padding(if (isTablet) 8.dp else 6.dp)) {
            val clockCenter = center
            val radius = size.minDimension / 2f
            val muted = if (dark) Color(0xFFAEB7C9) else Color(0xFF667085)
            val scale = if (isTablet) 1.28f else 1f

            repeat(60) { tick ->
                val angle = Math.toRadians((tick * 6.0) - 90.0)
                val major = tick % 5 == 0
                val outerRadius = radius * 0.91f
                val innerRadius = radius * if (major) 0.77f else 0.84f
                drawLine(
                    color = if (major) foreground else muted.copy(alpha = 0.58f),
                    start = Offset(
                        clockCenter.x + cos(angle).toFloat() * innerRadius,
                        clockCenter.y + sin(angle).toFloat() * innerRadius,
                    ),
                    end = Offset(
                        clockCenter.x + cos(angle).toFloat() * outerRadius,
                        clockCenter.y + sin(angle).toFloat() * outerRadius,
                    ),
                    strokeWidth = if (major) {
                        (4.dp * scale).toPx()
                    } else {
                        (1.5.dp * scale).toPx()
                    },
                    cap = StrokeCap.Round,
                )
            }

            val numberRadius = radius * 0.62f
            val verticalCenterOffset = (numberPaint.ascent() + numberPaint.descent()) / 2f
            drawIntoCanvas { canvas ->
                (1..12).forEach { number ->
                    val angle = Math.toRadians((number * 30.0) - 90.0)
                    val numberCenterX = clockCenter.x + cos(angle).toFloat() * numberRadius
                    val numberCenterY = clockCenter.y + sin(angle).toFloat() * numberRadius
                    canvas.nativeCanvas.drawText(
                        number.toString(),
                        numberCenterX,
                        numberCenterY - verticalCenterOffset,
                        numberPaint,
                    )
                }
            }

            fun drawHand(angleDegrees: Float, length: Float, width: Float, color: Color) {
                val angle = Math.toRadians(angleDegrees.toDouble() - 90.0)
                drawLine(
                    color = color,
                    start = clockCenter,
                    end = Offset(
                        clockCenter.x + cos(angle).toFloat() * radius * length,
                        clockCenter.y + sin(angle).toFloat() * radius * length,
                    ),
                    strokeWidth = width,
                    cap = StrokeCap.Round,
                )
            }

            drawHand(hour * 30f, 0.43f, (7.dp * scale).toPx(), foreground)
            drawHand(minute * 6f, 0.58f, (5.dp * scale).toPx(), foreground)
            drawHand(second * 6f, 0.62f, (2.dp * scale).toPx(), primary)
            drawCircle(color = primary, radius = (7.dp * scale).toPx(), center = clockCenter)
            drawCircle(color = Color.White, radius = (2.5.dp * scale).toPx(), center = clockCenter)
        }
    }
}

private fun formatDigitalTime(
    currentTime: Long,
    format: HomeClockHourFormat,
): String {
    val pattern = when (format) {
        HomeClockHourFormat.TWELVE_HOUR -> "h:mm a"
        HomeClockHourFormat.TWENTY_FOUR_HOUR -> "HH:mm"
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(currentTime))
}
