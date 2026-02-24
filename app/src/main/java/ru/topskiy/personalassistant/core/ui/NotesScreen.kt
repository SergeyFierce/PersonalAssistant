package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.topskiy.personalassistant.R

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    params: ScreenParams,
    viewModel: NotesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val notes = uiState.notes
    val pinned = notes.filter { it.pinned }
    val unpinned = notes.filter { !it.pinned }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        if (pinned.isNotEmpty()) {
                            item(key = "pinned_header") {
                                SectionHeader(
                                    text = stringResource(R.string.notes_section_pinned)
                                )
                            }
                            items(
                                items = pinned,
                                key = { it.id.value }
                            ) { item ->
                                NoteRowWithSwipe(
                                    item = item,
                                    darkTheme = params.darkTheme,
                                    onTap = { params.navController.navigate("$NOTE_EDITOR_ROUTE/${item.id.value}") },
                                    onSwipeToDelete = { viewModel.onDeleteNote(item.id) },
                                    onSwipeToTogglePin = { viewModel.onTogglePinned(item.id) }
                                )
                            }
                        }
                        if (unpinned.isNotEmpty()) {
                            if (pinned.isNotEmpty()) {
                                item(key = "all_header") {
                                    SectionHeader(
                                        text = stringResource(R.string.notes_section_all)
                                    )
                                }
                            }
                            items(
                                items = unpinned,
                                key = { it.id.value }
                            ) { item ->
                                NoteRowWithSwipe(
                                    item = item,
                                    darkTheme = params.darkTheme,
                                    onTap = { params.navController.navigate("$NOTE_EDITOR_ROUTE/${item.id.value}") },
                                    onSwipeToDelete = { viewModel.onDeleteNote(item.id) },
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
        shape = RoundedCornerShape(999.dp),
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteRowWithSwipe(
    item: NoteListItemUi,
    darkTheme: Boolean,
    onTap: () -> Unit,
    onSwipeToDelete: () -> Unit,
    onSwipeToTogglePin: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeToDelete()
                    true
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
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            NoteRow(
                item = item,
                modifier = Modifier
                    .clickable(onClick = onTap)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun NoteRow(
    item: NoteListItemUi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(20.dp)) {
            if (item.pinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (item.bodyPreview.isNotBlank()) {
                Text(
                    text = item.bodyPreview,
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
