package com.example.maiplan.home.more.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.maiplan.R
import com.example.maiplan.category.CategoryActivity
import com.example.maiplan.home.navigation.HomeNavigationBar

/**
 * Displays the `More` screen, structured with a top app bar,
 * a bottom navigation bar, and a list of navigable options.
 *
 * @param localNavController The controller for navigating between `More` sub-screens.
 * @param rootNavController The controller used to navigate across the broader `Home` tabs.
 *
 * @see MoreTopBar
 * @see HomeNavigationBar
 * @see MoreScreenButton
 */
@Composable
fun MoreScreen(
    localNavController: NavController,
    rootNavController: NavHostController
) {
    val context = LocalContext.current
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
            MoreScreenButton(stringResource(R.string.settings), onClick = { println("settings") }, Icons.Filled.Settings, true)
            MoreScreenButton(stringResource(R.string.help), onClick = { println("help") }, Icons.AutoMirrored.Filled.Help, false)
        }
    }
}

/**
 * A centered top app bar displaying localized title for the [MoreScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.more),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * A reusable button row for the [MoreScreen] options list.
 *
 * Each button includes:
 * - A leading [Icon].
 * - A [Text] label.
 * - A trailing arrow icon indicating navigation.
 * - An optional horizontal divider below the item.
 *
 * @param text The localized label to display.
 * @param onClick Callback to invoke when the button is clicked.
 * @param leadingIcon The leading [ImageVector] displayed at the start.
 * @param showDivider Whether to show a divider under this button.
 */
@Composable
fun MoreScreenButton(
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector,
    showDivider: Boolean
) {
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
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(24.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * Launches a new [Activity] from the given [context].
 *
 * @param context The [Context] from which to start the activity.
 * @param activity The class of the [Activity] to launch.
 */
fun openActivity(context: Context, activity: Class<out Activity>) {
    context.startActivity(Intent(context, activity))
}