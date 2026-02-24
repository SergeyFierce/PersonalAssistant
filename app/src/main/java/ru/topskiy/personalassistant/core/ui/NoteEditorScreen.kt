package ru.topskiy.personalassistant.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.model.NoteId
import ru.topskiy.personalassistant.ui.theme.ScreenBackgroundDark
import ru.topskiy.personalassistant.ui.theme.ScreenBackgroundLight
import ru.topskiy.personalassistant.ui.theme.TopAppBarDark
import ru.topskiy.personalassistant.ui.theme.TopAppBarLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    params: ScreenParams,
    noteId: Long?
) {
    val mainEntry = params.navController.getBackStackEntry(MAIN_ROUTE)
    val viewModel: NotesViewModel = hiltViewModel(mainEntry!!)
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var initialTitle by remember { mutableStateOf("") }
    var initialBody by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(noteId == null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    val noteIdTyped = noteId?.let { NoteId(it) }

    LaunchedEffect(noteIdTyped) {
        if (noteIdTyped != null) {
            viewModel.getNote(noteIdTyped).onSuccess { note ->
                if (note != null) {
                    title = note.title
                    body = note.body
                    initialTitle = note.title
                    initialBody = note.body
                }
            }
        } else {
            initialTitle = ""
            initialBody = ""
        }
    }

    val hasChanges = title != initialTitle || body != initialBody
    val saveLabel = stringResource(R.string.notes_editor_save)

    val saveAndClose: () -> Unit = {
        val t = title.trim()
        val b = body.trim()
        if (t.isEmpty() && b.isEmpty()) {
            showUnsavedDialog = false
            params.navController.popBackStack()
        } else {
            if (noteIdTyped == null) {
                viewModel.onCreateNote(t, b)
            } else {
                viewModel.onUpdateNote(noteIdTyped, t, b)
            }
        }
        showUnsavedDialog = false
        params.navController.popBackStack()
    }

    BackHandler(enabled = isEditing && hasChanges && !showDeleteConfirm && !showUnsavedDialog) {
        showUnsavedDialog = true
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.notes_delete_confirm_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteNote(noteIdTyped!!)
                        showDeleteConfirm = false
                        params.navController.popBackStack()
                    }
                ) {
                    Text(
                        stringResource(R.string.notes_delete_confirm_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.notes_delete_confirm_cancel))
                }
            }
        )
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(R.string.notes_unsaved_changes_title)) },
            confirmButton = {
                TextButton(onClick = { saveAndClose() }) {
                    Text(saveLabel)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            params.navController.popBackStack()
                        }
                    ) {
                        Text(stringResource(R.string.notes_unsaved_changes_discard))
                    }
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text(stringResource(R.string.notes_delete_confirm_cancel))
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteIdTyped == null) stringResource(R.string.notes_editor_title_new)
                        else stringResource(R.string.notes_editor_title_edit)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isEditing && hasChanges) {
                                showUnsavedDialog = true
                            } else {
                                params.navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = { saveAndClose() }) {
                            Text(
                                text = saveLabel,
                                color = Color.White
                            )
                        }
                        if (noteIdTyped != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.notes_swipe_delete),
                                    tint = Color.White
                                )
                            }
                        }
                    } else if (noteIdTyped != null) {
                        TextButton(onClick = { isEditing = true }) {
                            Text(
                                text = stringResource(R.string.notes_view_edit),
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (params.darkTheme) TopAppBarDark else TopAppBarLight,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (params.darkTheme) ScreenBackgroundDark else ScreenBackgroundLight)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isEditing) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_editor_title"),
                    placeholder = { Text(stringResource(R.string.notes_editor_title_hint)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                TextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("note_editor_body"),
                    placeholder = { Text(stringResource(R.string.notes_editor_body_hint)) },
                    minLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            } else {
                SelectionContainer {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                        }
                        if (body.isNotBlank()) {
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.95f),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
