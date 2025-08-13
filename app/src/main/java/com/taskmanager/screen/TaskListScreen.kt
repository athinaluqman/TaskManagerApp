package com.taskmanager.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskmanager.model.TaskDto
import com.taskmanager.util.TaskStatus
import com.taskmanager.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    isDarkMode: MutableState<Boolean>,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing = state.isLoading
    val listState = rememberLazyListState()

    // Trigger load more when scrolling near the bottom
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= state.filteredTasks.lastIndex - 5 && state.filteredTasks.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMoreTasks()
        }
    }

    // PullToRefreshBox
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadTasks(true) },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tasks") },
                    actions = {
                        // Theme switch button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { isDarkMode.value = !isDarkMode.value }
                                .padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Toggle Theme",
                                tint = if (isDarkMode.value) Color.Yellow else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDarkMode.value) "Dark" else "Light",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddTaskClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // Filter row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip("All", state.filter == TaskFilter.ALL) {
                        viewModel.setFilter(
                            TaskFilter.ALL
                        )
                    }
                    FilterChip(
                        "Completed",
                        state.filter == TaskFilter.COMPLETED
                    ) { viewModel.setFilter(TaskFilter.COMPLETED) }
                    FilterChip("Pending", state.filter == TaskFilter.PENDING) {
                        viewModel.setFilter(
                            TaskFilter.PENDING
                        )
                    }
                }

                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                LazyColumn(modifier = Modifier.fillMaxSize(),
                    state = listState) {
                    itemsIndexed(state.filteredTasks) {  index, t ->
                        TaskRow(t, onClick = {
                            viewModel.loadTask(t.id ?: "")
                            onTaskClick(t.id ?: "")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = MaterialTheme.shapes.small, shadowElevation = 2.dp) {
        Text(text = text, modifier = Modifier.padding(8.dp), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun TaskRow(task: TaskDto, onClick: () -> Unit) {
    val taskStatus =  when (task.status) {
        TaskStatus.COMPLETED -> "Completed"
        TaskStatus.NEEDS_ACTION -> "Needs Action"
        else -> "No Status"
    }

    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = task.status == TaskStatus.COMPLETED, onCheckedChange = null)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(task.title ?: "(no title)", fontWeight = FontWeight.Bold)
            Text(task.description ?: "No Description", style = if (task.description == null) {
                MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
            } else {
                MaterialTheme.typography.bodyMedium
            })
            Text(taskStatus, style = if (task.status == null) {
                MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
            } else {
                MaterialTheme.typography.bodyMedium
            })
        }
    }
}

enum class TaskFilter { ALL, COMPLETED, PENDING }
