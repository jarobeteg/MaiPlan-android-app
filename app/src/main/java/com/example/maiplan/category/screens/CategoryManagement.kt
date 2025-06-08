package com.example.maiplan.category.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.components.SearchFieldComponent
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.utils.model.IconData
import com.example.maiplan.viewmodel.category.CategoryViewModel

/**
 * Composable screen for managing categories.
 *
 * Displays a list of categories in cards with swipe actions for delete and edit.
 * A search bar is included and a top bar with back and create functions.
 *
 * @param viewModel The ViewModel providing the logic for managing categories.
 * @param onCardSwipeDelete Callback invoked when a category card is swiped from left to right.
 * @param onCardSwipeEdit Callback invoked when a category card is swiped from right to left.
 * @param onCreateCategoryClick Callback invoked when the add button is clicked.
 *
 * @see CategoryViewModel
 * @see CategoryResponse
 * @see SearchFieldComponent
 */
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    onCardSwipeDelete: (Int) -> Unit,
    onCardSwipeEdit: (CategoryResponse) -> Unit,
    onCreateCategoryClick: () -> Unit
) {
    val context = LocalContext.current
    val categoryList by viewModel.categoryList.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }

    Scaffold (
        topBar =
            { CategoryManagementTopBar(
                text = stringResource(R.string.categories),
                onBackClick = { (context as? Activity)?.finish() },
                onCreateCategoryClick = onCreateCategoryClick
            ) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SearchFieldComponent(searchQuery, 32) { searchQuery = it }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredCategories = categoryList.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredCategories, key = { it.categoryId }) { category ->
                    val density = LocalDensity.current
                    val dismissState = remember(categoryList) {
                        SwipeToDismissBoxState(
                            initialValue = SwipeToDismissBoxValue.Settled,
                            confirmValueChange = {
                                when (it) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        onCardSwipeDelete(category.categoryId)
                                    }
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        onCardSwipeEdit(category)
                                    }
                                    SwipeToDismissBoxValue.Settled -> return@SwipeToDismissBoxState false
                                }
                                return@SwipeToDismissBoxState true
                            },
                            density = density,
                            positionalThreshold = { it * 0.9f }
                        )
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = { DismissBackground(dismissState) },
                        content = {
                            CategoryCard(category = category)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Top app bar for the category management screen.
 *
 * Displays the screen title and includes a back and create button.
 *
 * @param text The title shown in the top app bar.
 * @param onBackClick Callback invoked when the back button is clicked.
 * @param onCreateCategoryClick Callback invoked when the add category button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementTopBar(
    text: String,
    onBackClick: () -> Unit,
    onCreateCategoryClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onCreateCategoryClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * Card representation of a category
 *
 * Displays the category's icon, name, and description.
 * The background color reflects the category's assigned color.
 *
 * @param category The category data to display.
 *
 * @see IconData
 */
@Composable
fun CategoryCard(category: CategoryResponse) {
    val backgroundColor = Color(category.color.toULong())
    val isDarkTheme = isSystemInDarkTheme()

    val textColor = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
    val borderColor = if (isDarkTheme && backgroundColor.luminance() < 0.5f) {
        Color.LightGray
    } else if (!isDarkTheme && backgroundColor.luminance() > 0.5f) {
        Color.DarkGray
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .border(2.dp, borderColor, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconData.getIconByKey(category.icon),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = textColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Background shown during swipe to dismiss gesture.
 *
 * Displays a red background with a delete icon when swiping left to right.
 * Displays a green background with an edit icon when swiping right to left.
 */
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFF1744)
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF1DE9B6)
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(color)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            Arrangement.Start
        } else {
            Arrangement.End
        }
    ) {
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
