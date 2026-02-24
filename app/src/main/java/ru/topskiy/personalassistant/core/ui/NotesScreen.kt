package ru.topskiy.personalassistant.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.model.NoteId

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    params: ScreenParams,
    viewModel: NotesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val notes = uiState.notes
    var pendingDeleteId by remember { mutableStateOf<NoteId?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<NoteId>>(emptySet()) }
    val deletedMessage = stringResource(R.string.notes_deleted_snackbar)
    val undoLabel = stringResource(R.string.notes_undo)
    val selectedCount = selectedIds.size
    val selectedItems = notes.filter { selectedIds.contains(it.id) }
    val allPinned = selectedItems.isNotEmpty() && selectedItems.all { it.pinned }

    LaunchedEffect(Unit, deletedMessage, undoLabel) {
        viewModel.undoSnackbarRequest.collect {
            val result = snackbarHostState.showSnackbar(deletedMessage, undoLabel)
            if (result == SnackbarResult.ActionPerformed) viewModel.restoreLastDeleted()
        }
    }

    BackHandler(enabled = selectionMode) {
        selectionMode = false
        selectedIds = emptySet()
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(R.string.notes_delete_confirm_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteNote(pendingDeleteId!!)
                        pendingDeleteId = null
                    }
                ) {
                    Text(
                        stringResource(R.string.notes_delete_confirm_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(R.string.notes_delete_confirm_cancel))
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NotesHeader(
                totalCount = notes.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            SearchBar(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                darkTheme = params.darkTheme
            )
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (notes.isEmpty()) {
                    NotesEmptyState(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        isSearch = searchQuery.isNotBlank()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.sections.forEachIndexed { index, section ->
                            val headerKey = "section_header_${section.type}_$index"
                            item(key = headerKey) {
                                SectionHeader(
                                    text = when (section.type) {
                                        NotesSectionType.PINNED -> stringResource(R.string.notes_section_pinned)
                                        NotesSectionType.TODAY -> stringResource(R.string.notes_section_today)
                                        NotesSectionType.YESTERDAY -> stringResource(R.string.notes_section_yesterday)
                                        NotesSectionType.EARLIER -> stringResource(R.string.notes_section_earlier)
                                    }
                                )
                            }
                            items(
                                items = section.items,
                                key = { it.id.value }
                            ) { item ->
                                val isSelected = selectedIds.contains(item.id)
                                val onItemClick: () -> Unit = {
                                    if (selectionMode) {
                                        selectedIds = if (isSelected) {
                                            selectedIds - item.id
                                        } else {
                                            selectedIds + item.id
                                        }
                                        if (selectedIds.isEmpty()) {
                                            selectionMode = false
                                        }
                                    } else {
                                        params.navController.navigate("$NOTE_EDITOR_ROUTE/${item.id.value}")
                                    }
                                }
                                val onItemLongClick: () -> Unit = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedIds = setOf(item.id)
                                    } else {
                                        // В режиме выбора длинное нажатие ведёт себя как обычный клик
                                        onItemClick()
                                    }
                                }
                                NoteRowWithSwipe(
                                    item = item,
                                    darkTheme = params.darkTheme,
                                    selectionMode = selectionMode,
                                    isSelected = isSelected,
                                    onTap = onItemClick,
                                    onLongPress = onItemLongClick,
                                    onSwipeToDeleteRequested = { pendingDeleteId = item.id },
                                    onSwipeToTogglePin = { viewModel.onTogglePinned(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { params.navController.navigate(NOTE_EDITOR_ROUTE) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.notes_fab_add),
                modifier = Modifier.size(24.dp)
            )
        }
        AnimatedVisibility(
            visible = selectionMode,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            NotesSelectionBottomBar(
                selectedCount = selectedCount,
                allPinned = allPinned,
                onTogglePinClick = {
                    if (selectedCount == 0) return@NotesSelectionBottomBar
                    val targetPinned = !allPinned
                    val targetIds = selectedItems
                        .filter { it.pinned != targetPinned }
                        .map { it.id }
                    targetIds.forEach { id ->
                        viewModel.onTogglePinned(id)
                    }
                    selectionMode = false
                    selectedIds = emptySet()
                },
                onDeleteClick = {
                    if (selectedCount == 0) return@NotesSelectionBottomBar
                    val idsToDelete = selectedIds
                    selectionMode = false
                    selectedIds = emptySet()
                    viewModel.onDeleteNotes(idsToDelete)
                },
                onCancelClick = {
                    selectionMode = false
                    selectedIds = emptySet()
                }
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.notes_search_hint)) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun NotesEmptyState(
    modifier: Modifier = Modifier,
    isSearch: Boolean
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = if (isSearch) {
                    stringResource(R.string.notes_empty_search)
                } else {
                    stringResource(R.string.notes_empty_title)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp)
            )
            if (!isSearch) {
                Text(
                    text = stringResource(R.string.notes_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun NotesSelectionBottomBar(
    selectedCount: Int,
    allPinned: Boolean,
    onTogglePinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedCount.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onTogglePinClick,
                    enabled = selectedCount > 0
                ) {
                    Text(
                        text = if (allPinned) {
                            stringResource(R.string.notes_swipe_unpin)
                        } else {
                            stringResource(R.string.notes_swipe_pin)
                        }
                    )
                }
                TextButton(
                    onClick = onDeleteClick,
                    enabled = selectedCount > 0
                ) {
                    Text(
                        text = stringResource(R.string.notes_delete_confirm_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(onClick = onCancelClick) {
                    Text(stringResource(R.string.notes_delete_confirm_cancel))
                }
            }
        }
    }
}

@Composable
private fun NotesHeader(
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.notes_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.notes_header_count, totalCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun NoteRowWithSwipe(
    item: NoteListItemUi,
    darkTheme: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onSwipeToDeleteRequested: () -> Unit,
    onSwipeToTogglePin: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeToDeleteRequested()
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeToTogglePin()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val (bgColor, textColor) = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                SwipeToDismissBoxValue.Settled -> Color.Transparent to Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.Settled -> Alignment.Center
                }
            ) {
                Text(
                    text = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> stringResource(R.string.notes_swipe_delete)
                        SwipeToDismissBoxValue.StartToEnd ->
                            if (item.pinned) stringResource(R.string.notes_swipe_unpin)
                            else stringResource(R.string.notes_swipe_pin)
                        SwipeToDismissBoxValue.Settled -> ""
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor
                )
            }
        },
        enableDismissFromStartToEnd = !selectionMode,
        enableDismissFromEndToStart = !selectionMode
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ) {
            NoteRow(
                item = item,
                selectionMode = selectionMode,
                isSelected = isSelected,
                modifier = Modifier
                    .combinedClickable(
                        onClick = onTap,
                        onLongClick = onLongPress
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun NoteRow(
    item: NoteListItemUi,
    selectionMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildHighlightedText(
                        text = item.title,
                        ranges = item.titleHighlightRanges,
                        highlightColor = MaterialTheme.colorScheme.secondary
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (item.pinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                    )
                }
            }
            if (item.bodyPreview.isNotBlank()) {
                Text(
                    text = buildHighlightedText(
                        text = item.bodyPreview,
                        ranges = item.bodyHighlightRanges,
                        highlightColor = MaterialTheme.colorScheme.secondary
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        Text(
            text = item.updatedAtFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildHighlightedText(
    text: String,
    ranges: List<IntRange>,
    highlightColor: Color,
    highlightFontWeight: FontWeight = FontWeight.SemiBold
): AnnotatedString {
    if (ranges.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        ranges.forEach { range ->
            if (range.first in text.indices && range.last in text.indices && range.first <= range.last) {
                addStyle(
                    SpanStyle(color = highlightColor, fontWeight = highlightFontWeight),
                    start = range.first,
                    end = range.last + 1
                )
            }
        }
    }
}
