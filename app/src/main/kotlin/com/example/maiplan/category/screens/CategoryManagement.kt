package com.example.maiplan.category.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maiplan.R
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.utils.common.IconData
import com.example.maiplan.viewmodel.category.CategoryViewModel

@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    onCardSwipeDelete: (Int) -> Unit,
    onCardSwipeEdit: (CategoryEntity) -> Unit,
    onCreateCategoryClick: () -> Unit,
) {
    val context = LocalContext.current
    val categoryList by viewModel.categoryList.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val filteredCategories = categoryList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
    }

    CategoryScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CategoryTopBar(
                    title = stringResource(R.string.categories),
                    onBackClick = { (context as? Activity)?.finish() },
                )
            },
            floatingActionButton = {
                CategoryAddFloatingButton(onClick = onCreateCategoryClick)
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Spacer(Modifier.height(22.dp))
                    CategoryListHeader(count = categoryList.size)
                    Spacer(Modifier.height(18.dp))
                    CategorySearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                    )
                    Spacer(Modifier.height(11.dp))
                    Text(
                        text = stringResource(R.string.category_gesture_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSystemInDarkTheme()) Color(0xFFAEB7C9) else CategoryMuted,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (filteredCategories.isEmpty()) {
                            item {
                                CategoryEmptyState(isSearching = searchQuery.isNotBlank())
                            }
                        } else {
                            items(filteredCategories, key = { it.categoryId }) { category ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    positionalThreshold = { it * 0.45f },
                                )
                                LaunchedEffect(dismissState) {
                                    snapshotFlow { dismissState.currentValue }.collect { value ->
                                        when (value) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                                onCardSwipeEdit(category)
                                            }
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                                onCardSwipeDelete(category.categoryId)
                                            }
                                            SwipeToDismissBoxValue.Settled -> Unit
                                        }
                                    }
                                }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = { CategoryDismissBackground(dismissState) },
                                ) {
                                    CategoryListCard(
                                        name = category.name,
                                        description = category.description,
                                        color = Color(category.color.toULong()),
                                        icon = IconData.getIconByKey(category.icon),
                                        onEditClick = { onCardSwipeEdit(category) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDismissBackground(dismissState: SwipeToDismissBoxState) {
    val editing = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
    val deleting = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
    val color = when {
        deleting -> Color(0xFFE5484D)
        editing -> CategoryTeal
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (editing) Arrangement.Start else Arrangement.End,
    ) {
        if (deleting || editing) {
            Icon(
                imageVector = if (deleting) Icons.Rounded.DeleteOutline else Icons.Rounded.Edit,
                contentDescription = null,
                tint = Color.White,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (deleting) R.string.delete else R.string.edit,
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
