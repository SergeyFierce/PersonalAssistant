package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    val noteIdTyped = noteId?.let { NoteId(it) }

    LaunchedEffect(noteIdTyped) {
        if (noteIdTyped != null) {
            viewModel.getNote(noteIdTyped).onSuccess { note ->
                if (note != null) {
                    title = note.title
                    body = note.body
                }
            }
        }
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
                    IconButton(onClick = { params.navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (noteIdTyped != null) {
                        IconButton(
                            onClick = {
                                viewModel.onDeleteNote(noteIdTyped)
                                params.navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.notes_swipe_delete),
                                tint = Color.White
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth().testTag("note_editor_title"),
                label = { Text(stringResource(R.string.notes_editor_title_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("note_editor_body"),
                label = { Text(stringResource(R.string.notes_editor_body_hint)) },
                minLines = 6,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            val saveLabel = stringResource(R.string.notes_editor_save)
            Button(
                onClick = {
                    val t = title.trim()
                    val b = body.trim()
                    if (t.isEmpty() && b.isEmpty()) return@Button
                    if (noteIdTyped == null) {
                        viewModel.onCreateNote(t, b)
                    } else {
                        viewModel.onUpdateNote(noteIdTyped, t, b)
                    }
                    params.navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(saveLabel)
            }
        }
    }
}
