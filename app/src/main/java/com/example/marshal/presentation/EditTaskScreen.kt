package com.example.marshal.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.marshal.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    task: Task,
    onBackClick: () -> Unit,
    onSaveClick: (updatedTask: Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var priority by remember { mutableStateOf(task.priority) }

    // Validation state
    var isTitleError by remember { mutableStateOf(false) }

    val priorities = listOf("Low", "Medium", "High")

    val handleSave = {
        if (title.trim().isEmpty()) {
            isTitleError = true
        } else {
            isTitleError = false
            val updated = task.copy(
                title = title.trim(),
                description = description.trim(),
                priority = priority
            )
            onSaveClick(updated)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }

            )

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = handleSave,
                containerColor = Color(0xFFFF6347),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save updated task"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isTitleError = false
                },
                label = { Text("Task Title *") },
                isError = isTitleError,
                supportingText = {
                    if (isTitleError) {
                        Text(
                            text = "Title cannot be empty!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )


            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Priority.values().forEach { option ->
                        val displayName = option.name.lowercase().replaceFirstChar { it.uppercase() }
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }


            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp), // Generous space for long descriptions
                maxLines = 10,
                shape = RoundedCornerShape(12.dp)
            )


        }
    }
}