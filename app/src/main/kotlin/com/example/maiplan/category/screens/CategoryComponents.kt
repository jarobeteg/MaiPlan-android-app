package com.example.maiplan.category.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maiplan.R
import com.example.maiplan.components.ColorPickerDialog
import com.example.maiplan.components.IconPickerDialog
import com.example.maiplan.theme.AppThemeManager

internal val CategoryPrimary: Color get() = AppThemeManager.selectedTheme.primary
internal val CategoryPrimaryLight: Color get() = AppThemeManager.selectedTheme.primaryLight
internal val CategoryTeal = Color(0xFF14B8A6)
internal val CategoryInk = Color(0xFF172033)
internal val CategoryMuted = Color(0xFF667085)
internal val CategoryBorder = Color(0xFFDDE3EC)

@Composable
internal fun CategoryScreenBackground(
    showDecorations: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val background = if (dark) Color(0xFF101321) else Color(0xFFF4F6FC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        if (showDecorations) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            CategoryPrimary.copy(alpha = if (dark) 0.18f else 0.09f),
                            Color.Transparent,
                        ),
                    ),
                    center = Offset(size.width * 0.05f, size.height * 0.03f),
                    radius = size.minDimension * 0.68f,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            CategoryTeal.copy(alpha = if (dark) 0.10f else 0.06f),
                            Color.Transparent,
                        ),
                    ),
                    center = Offset(size.width, size.height),
                    radius = size.minDimension * 0.52f,
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = foreground,
                letterSpacing = (-0.3).sp,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = foreground,
                )
            }
        },
        actions = {
            Spacer(Modifier.size(48.dp))
            Spacer(Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = surface.copy(alpha = 0.94f),
        ),
    )
}

@Composable
internal fun CategoryAddFloatingButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = CategoryPrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(17.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.category_new),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
    )
}

@Composable
internal fun CategorySearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val field = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (dark) Color(0xFFAEB7C9) else CategoryMuted

    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 32) onValueChange(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.category_search)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = field,
            unfocusedContainerColor = field,
            focusedBorderColor = CategoryPrimary,
            unfocusedBorderColor = if (dark) Color(0xFF3A435C) else CategoryBorder,
            focusedTextColor = foreground,
            unfocusedTextColor = foreground,
            focusedLeadingIconColor = CategoryPrimary,
            unfocusedLeadingIconColor = muted,
            focusedPlaceholderColor = muted,
            unfocusedPlaceholderColor = muted,
            cursorColor = CategoryPrimary,
        ),
    )
}

@Composable
internal fun CategoryListHeader(count: Int) {
    val foreground = if (LocalAppDarkTheme.current) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (LocalAppDarkTheme.current) Color(0xFFAEB7C9) else CategoryMuted

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.category_collection_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = foreground,
                letterSpacing = (-0.5).sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.category_collection_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = CategoryPrimary.copy(alpha = if (LocalAppDarkTheme.current) 0.22f else 0.10f),
        ) {
            Text(
                text = stringResource(R.string.category_total, count),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (LocalAppDarkTheme.current) Color(0xFFC9D7E5) else CategoryPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
internal fun CategoryListCard(
    name: String,
    description: String,
    color: Color,
    icon: ImageVector,
    onEditClick: () -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (dark) Color(0xFFAEB7C9) else CategoryMuted
    val visibleAccent = if (color.luminance() > 0.82f) CategoryPrimaryLight else color

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(20.dp),
        color = surface,
        shadowElevation = if (dark) 0.dp else 3.dp,
        border = BorderStroke(
            1.dp,
            if (dark) Color(0xFF30374D) else CategoryBorder.copy(alpha = 0.75f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = visibleAccent.copy(alpha = if (dark) 0.24f else 0.14f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = visibleAccent,
                        modifier = Modifier.size(27.dp),
                    )
                }
            }

            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
            }

            Spacer(Modifier.width(10.dp))
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.category_edit),
                    tint = CategoryPrimary,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
    }
}

@Composable
internal fun CategoryEmptyState(isSearching: Boolean) {
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (dark) Color(0xFFAEB7C9) else CategoryMuted

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(22.dp),
        color = surface,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else CategoryBorder),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = CategoryPrimary.copy(alpha = if (dark) 0.22f else 0.10f),
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Rounded.Search else Icons.Rounded.Category,
                    contentDescription = null,
                    tint = CategoryPrimary,
                    modifier = Modifier.padding(16.dp).size(28.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(
                    if (isSearching) R.string.category_empty_search_title
                    else R.string.category_empty_title,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = foreground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (isSearching) R.string.category_empty_search_subtitle
                    else R.string.category_empty_subtitle,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun CategoryEditorLayout(
    title: String,
    heading: String,
    subtitle: String,
    name: String,
    description: String,
    selectedColor: Color,
    selectedIcon: ImageVector,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorSelected: (Color) -> Unit,
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit,
    submitLabel: String,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
) {
    CategoryScreenBackground {
        androidx.compose.material3.Scaffold(
            containerColor = Color.Transparent,
            topBar = { CategoryTopBar(title = title, onBackClick = onBackClick) },
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
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    CategoryEditorHeading(heading = heading, subtitle = subtitle)
                    CategoryPreview(
                        name = name,
                        description = description,
                        color = selectedColor,
                        icon = selectedIcon,
                    )
                    CategoryEditorSection(
                        title = stringResource(R.string.category_details_title),
                        subtitle = stringResource(R.string.category_details_subtitle),
                    ) {
                        CategoryEditorTextField(
                            value = name,
                            onValueChange = { if (it.length <= 255) onNameChange(it) },
                            label = stringResource(R.string.name),
                            leadingIcon = Icons.Rounded.Title,
                            singleLine = true,
                            imeAction = ImeAction.Next,
                        )
                        Spacer(Modifier.height(14.dp))
                        CategoryEditorTextField(
                            value = description,
                            onValueChange = { if (it.length <= 512) onDescriptionChange(it) },
                            label = stringResource(R.string.description),
                            leadingIcon = Icons.Rounded.Description,
                            singleLine = false,
                            imeAction = ImeAction.Default,
                        )
                    }
                    CategoryEditorSection(
                        title = stringResource(R.string.category_appearance_title),
                        subtitle = stringResource(R.string.category_appearance_subtitle),
                    ) {
                        CategoryColorSelector(
                            selectedColor = selectedColor,
                            onColorSelected = onColorSelected,
                        )
                        Spacer(Modifier.height(12.dp))
                        CategoryIconSelector(
                            selectedIcon = selectedIcon,
                            onIconSelected = onIconSelected,
                            onIconSelectedString = onIconSelectedString,
                        )
                    }
                    if (errorMessage != null) CategoryEditorError(errorMessage)
                    CategoryEditorButton(
                        text = submitLabel,
                        isLoading = isLoading,
                        onClick = onSubmit,
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryEditorHeading(heading: String, subtitle: String) {
    val foreground = if (LocalAppDarkTheme.current) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (LocalAppDarkTheme.current) Color(0xFFAEB7C9) else CategoryMuted
    Column {
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = foreground,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = muted,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun CategoryPreview(
    name: String,
    description: String,
    color: Color,
    icon: ImageVector,
) {
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (dark) Color(0xFFAEB7C9) else CategoryMuted

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surface,
        shadowElevation = if (dark) 0.dp else 4.dp,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else CategoryBorder),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.72f))))
                    .border(1.dp, CategoryBorder.copy(alpha = 0.75f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.68f) CategoryInk else Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { stringResource(R.string.category_preview_name) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = description.ifBlank { stringResource(R.string.category_preview_description) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = CategoryTeal.copy(alpha = if (dark) 0.18f else 0.10f),
            ) {
                Text(
                    text = stringResource(R.string.category_live_preview),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CategoryTeal,
                )
            }
        }
    }
}

@Composable
private fun CategoryEditorSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val surface = if (dark) Color(0xFF191D2E) else Color.White
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (dark) Color(0xFFAEB7C9) else CategoryMuted

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surface,
        border = BorderStroke(1.dp, if (dark) Color(0xFF30374D) else CategoryBorder),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = foreground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
            Spacer(Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun CategoryEditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    singleLine: Boolean,
    imeAction: ImeAction,
) {
    val dark = LocalAppDarkTheme.current
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (dark) Color(0xFFAEB7C9) else CategoryMuted

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (singleLine) 56.dp else 112.dp),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
            )
        },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(15.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = imeAction,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = field,
            unfocusedContainerColor = field,
            focusedBorderColor = CategoryPrimary,
            unfocusedBorderColor = if (dark) Color(0xFF3A435C) else CategoryBorder,
            focusedLabelColor = CategoryPrimary,
            unfocusedLabelColor = muted,
            focusedLeadingIconColor = CategoryPrimary,
            unfocusedLeadingIconColor = muted,
            focusedTextColor = foreground,
            unfocusedTextColor = foreground,
            cursorColor = CategoryPrimary,
        ),
    )
}

@Composable
private fun CategoryColorSelector(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    CategorySelectionRow(
        title = stringResource(R.string.category_color),
        subtitle = stringResource(R.string.category_color_subtitle),
        leadingIcon = Icons.Rounded.Palette,
        onClick = { showDialog = true },
        selection = {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(1.dp, CategoryBorder, CircleShape),
            )
        },
    )
    if (showDialog) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = onColorSelected,
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun CategoryIconSelector(
    selectedIcon: ImageVector,
    onIconSelected: (ImageVector) -> Unit,
    onIconSelectedString: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    CategorySelectionRow(
        title = stringResource(R.string.category_icon),
        subtitle = stringResource(R.string.category_icon_subtitle),
        leadingIcon = Icons.Rounded.GridView,
        onClick = { showDialog = true },
        selection = {
            Icon(
                imageVector = selectedIcon,
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                tint = CategoryPrimary,
            )
        },
    )
    if (showDialog) {
        IconPickerDialog(
            onIconSelected = onIconSelected,
            onIconSelectedString = onIconSelectedString,
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun CategorySelectionRow(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    selection: @Composable () -> Unit,
) {
    val dark = LocalAppDarkTheme.current
    val field = if (dark) Color(0xFF20263A) else Color(0xFFF8FAFC)
    val foreground = if (dark) Color(0xFFF5F7FB) else CategoryInk
    val muted = if (dark) Color(0xFFAEB7C9) else CategoryMuted

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = field,
        border = BorderStroke(1.dp, if (dark) Color(0xFF3A435C) else CategoryBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = CategoryPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(14.dp))
            Row(
                modifier = Modifier.width(69.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (dark) Color(0xFF2A3148) else Color.White,
                    border = BorderStroke(1.dp, if (dark) Color(0xFF3A435C) else CategoryBorder),
                ) {
                    Box(contentAlignment = Alignment.Center) { selection() }
                }
                Spacer(Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = muted,
                )
            }
        }
    }
}

@Composable
private fun CategoryEditorButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CategoryPrimary,
            contentColor = Color.White,
            disabledContainerColor = CategoryPrimary.copy(alpha = 0.64f),
            disabledContentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CategoryEditorError(message: String) {
    val dark = LocalAppDarkTheme.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = if (dark) Color(0xFF3B2027) else Color(0xFFFFF1F2),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFE5484D),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (dark) Color(0xFFFFB4B8) else Color(0xFFA61B29),
            )
        }
    }
}
