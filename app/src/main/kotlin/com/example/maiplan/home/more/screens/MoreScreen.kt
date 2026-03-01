package com.example.maiplan.home.more.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.category.CategoryActivity
import com.example.maiplan.home.navigation.HomeNavigationBar
import com.example.maiplan.network.sync.SyncScheduler
import com.example.maiplan.utils.BaseActivity
import com.example.maiplan.utils.LocalUiScale

@Composable
fun MoreScreen(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    val context = LocalContext.current
    val onLogoutClick = logoutHandler()

    Scaffold(
        topBar = { MoreTopBar() },
        bottomBar = { HomeNavigationBar(rootNavController, context) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            MoreScreenButton(stringResource(R.string.category_management), { openActivity(context, CategoryActivity::class.java) }, Icons.Filled.Category, true)
            //MoreScreenButton(stringResource(R.string.settings), onClick = { println("settings") }, Icons.Filled.Settings, true)
            //MoreScreenButton(stringResource(R.string.help), onClick = { println("help") }, Icons.AutoMirrored.Filled.Help, true)
            MoreScreenButton(stringResource(R.string.sync), onClick = { SyncScheduler.runOneTimeSync(context) }, Icons.Filled.Sync, true)
            MoreScreenButton(stringResource(R.string.logout), onClick = onLogoutClick, Icons.AutoMirrored.Filled.Logout, false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreTopBar() {
    val ui = LocalUiScale.current

    CenterAlignedTopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.more),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = ui.fonts.generalTopBarTitleSize,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.height(ui.components.generalTopBarHeight)
    )
}

@Composable
fun MoreScreenButton(
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector,
    showDivider: Boolean
) {
    val ui = LocalUiScale.current
    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(ui.components.generalIconSize)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = ui.fonts.generalTextSize,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(ui.components.generalIconSize)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = ui.dimensions.generalDividerThickness,
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun logoutHandler() : () -> Unit {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.logout_dialog),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        (context as? BaseActivity)?.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.primary
        )
    }

    return { showDialog = true }
}

fun openActivity(context: Context, activity: Class<out Activity>) {
    context.startActivity(Intent(context, activity))
}