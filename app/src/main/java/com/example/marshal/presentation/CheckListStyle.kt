package com.example.marshal.presentation

import android.annotation.SuppressLint
import android.widget.CheckBox
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marshal.model.CheckList
import com.example.marshal.model.CheckListNote
import com.example.marshal.ui.theme.appRedColor
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RememberInComposition")
@Composable
fun CheckListNote(
    viewModel: CheckListViewModel,
    existingNote: CheckListNote? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    val titleText = viewModel.title.value
    val checkListItems = viewModel.checkListItems
    val isLoading = viewModel.isLoading.value

    val focusRequesters = remember { mutableStateMapOf<String, FocusRequester>() }

    LaunchedEffect(existingNote) {
        if (existingNote != null) {
            viewModel.loadExistingNote(existingNote)
        } else {
            viewModel.resetForm()
        }
        if (checkListItems.isEmpty() || checkListItems.last().text.isNotBlank()) {
            viewModel.addNewItem()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(15.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!isLoading) {
                        viewModel.createCheckListNote { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, "Checklist Saved!", Toast.LENGTH_SHORT).show()
                                onNavigateBack() // Go back to Home Screen on success
                            } else {
                                Toast.makeText(context, "Failed to save checklist", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                containerColor = appRedColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(15.dp)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TextField(
                value = titleText,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = {
                    Text(
                        text = "title...", fontSize = 36.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 15.dp),
                thickness = 0.25.dp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Priority:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.values().forEach { prio ->
                        val isSelected = viewModel.priority.value == prio
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(containerColor)
                                .clickable { viewModel.onPriorityChange(prio) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prio.name.lowercase().replaceFirstChar { it.uppercase() }, // e.g., "Medium"
                                fontSize = 14.sp,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = checkListItems,
                    key = { _, item -> item.id }
                ) { index, item ->

                    val itemFocusRequester = focusRequesters.getOrPut(item.id) { FocusRequester() }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { isChecked ->
                                viewModel.onItemCheckedChange(index, isChecked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        BasicTextField(
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            value = item.text,
                            decorationBox = { placeholderText ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (item.text.isBlank()) {
                                        Text(
                                            text = "Add task...",
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Faded placeholder
                                        )
                                    }
                                    placeholderText()
                                }
                            },
                            onValueChange = { newText ->
                                viewModel.onItemTextChange(index, newText)
                            },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                color = if (item.isChecked) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    if (item.text.isNotBlank()) {
                                        viewModel.addNewItem()

                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(20.milliseconds)
                                            checkListItems.lastOrNull()?.id?.let { newId ->
                                                val newFocusRequester = focusRequesters.getOrPut(newId) { FocusRequester() }
                                                newFocusRequester.requestFocus()
                                            }
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .focusRequester(itemFocusRequester)
                        )

                        if (item.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    focusRequesters.remove(item.id)
                                    viewModel.removeItem(index)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete or clear item",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}