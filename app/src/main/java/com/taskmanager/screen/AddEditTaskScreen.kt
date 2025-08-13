package com.taskmanager.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskmanager.viewmodel.TaskViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(viewModel: TaskViewModel, taskId: String? = null, onSave: () -> Unit, onCancel: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate?.atStartOfDay(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli())
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(taskId, state.selectedTask) {
        if (taskId != null) {
            // Editing existing task
            if (state.selectedTask?.id != taskId) {
                viewModel.loadTask(taskId)
            } else {
                state.selectedTask?.let { t ->
                    title = t.title ?: ""
                    description = t.description ?: ""
                    dueDate = t.dueDate?.let { ds ->
                        try {
                            LocalDate.parse(ds, dateFormatter)
                        } catch (e: DateTimeParseException) { null }
                    }
                }
            }
        } else {
            // Adding new task → always clear form
            title = ""
            description = ""
            dueDate = null
            titleError = null
            viewModel.clearSelectedTask()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dueDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(if (taskId == null) "Add Task" else "Edit Task") }, navigationIcon = { IconButton(onClick = onCancel) { Icon(
        Icons.Default.ArrowBack, contentDescription = "Back") } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = title, onValueChange = {
                title = it
                titleError = null },
                label = { Text("Title") },
                isError = titleError != null,
                modifier = Modifier.fillMaxWidth())
            if (titleError != null) {
                Text(
                    text = titleError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showDatePicker = true }) { Text(dueDate?.toString() ?: "Pick due date") }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (title.isBlank()) {
                        titleError = "Title cannot be empty"
                    } else {
                        viewModel.saveTask(taskId, title, description, dueDate)
                        onSave()
                    }
                }) { Text("Save") }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}
