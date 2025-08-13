package com.taskmanager.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskmanager.util.TaskStatus
import com.taskmanager.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskViewModel, onEdit: () -> Unit, onBack: () -> Unit
) {
    val state by viewModel.selectedTask.collectAsStateWithLifecycle()
    val t = state ?: return

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val dateString = if (t.dueDate != null) LocalDate.parse(t.dueDate, dateFormatter) else "No Due Date"

    Scaffold(topBar = {
        TopAppBar(title = { Text(t.title ?: "") }, navigationIcon = { IconButton(onClick = onBack) { Icon(
            Icons.Default.ArrowBack, contentDescription = "Back") } }, actions = {
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = { t.id?.let { viewModel.deleteTask(it); onBack() } }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Description: ${t.description?.takeIf { it.isNotBlank() } ?: "No Description"}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Status: ${
                    when (t.status) {
                        TaskStatus.COMPLETED -> "Completed"
                        TaskStatus.NEEDS_ACTION -> "Needs Action"
                        else -> "No Status"
                    }
                }"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Due Date: $dateString")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.toggleTaskStatus(t) }) { Text(if (t.status == TaskStatus.COMPLETED) "Mark as Incomplete" else "Mark as Complete") }
        }
    }
}
