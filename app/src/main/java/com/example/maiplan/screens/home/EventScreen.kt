package com.example.maiplan.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.example.maiplan.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen() {
    val context = LocalContext.current
    var selectedView by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    // Animated arrow rotation when dropdown opens/closes
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "Dropdown Arrow Rotation"
    )

    val today = remember { LocalDate.now() }
    val formattedTitle = when (selectedView) {
        0 -> today.format(DateTimeFormatter.ofPattern("MMM yyyy"))
        1 -> "${getString(context, R.string.week)} ${today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())}"
        2 -> today.format(DateTimeFormatter.ofPattern("EE, MMM d"))
        else -> ""
    }

    val views = listOf(getString(context, R.string.monthly), getString(context, R.string.weekly), getString(context, R.string.daily))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box (
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4A6583)) // Matches blue-gray theme
                            .border(
                                width = 2.dp,
                                color = Color(0xFF2D3E50),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        OutlinedButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.height(40.dp),
                            border = null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = formattedTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = Color.White,
                                modifier = Modifier.rotate(rotationAngle)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(Color(0xFF4A6583))
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            views.forEachIndexed { index, view ->
                                DropdownMenuItem(
                                    text = {
                                        Text(view, color = Color.White)
                                    },
                                    onClick = {
                                        selectedView = index
                                        expanded = false
                                    },
                                    modifier = Modifier
                                        .background(Color(0xFF4A6583))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF4A6583),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedView) {
                0 -> MonthlyView()
                1 -> WeeklyView()
                2 -> DailyView()
            }
        }
    }
}


@Composable
fun MonthlyView() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(30) { day ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(text = "Day ${day + 1}",  style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun WeeklyView() {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(7) { day ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "Day ${day + 1} Summary", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

@Composable
fun DailyView() {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(24) { hour ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "${hour}:00 - ${hour + 1}:00", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewActivityScreen() {
    EventScreen()
}