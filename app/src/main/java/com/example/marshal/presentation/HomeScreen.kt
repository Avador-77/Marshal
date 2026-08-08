package com.example.marshal.presentation

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marshal.R
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.marshal.ui.theme.appRedColor

enum class SortOrder {
    HIGH_TO_LOW,
    LOW_TO_HIGH
}
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ToDoViewModel = viewModel(),
    onTaskClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onCheckListClick: (String) -> Unit,
    onAddClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var showNoteTypeDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortOrder by remember { mutableStateOf(SortOrder.HIGH_TO_LOW) }
    var showFilterClassic by remember { mutableStateOf(false) }
    var showFilterChecklist by remember { mutableStateOf(false) }

    val sharedPrefs = remember { context.getSharedPreferences("MarshalPrefs", Context.MODE_PRIVATE) }
    var showSwipeHint by remember {
        // Defaults to true, meaning new users will always see it first
        mutableStateOf(sharedPrefs.getBoolean("showSwipeHint", true))
    }

    // Collect the UI State (Loading or Success) from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNoteTypeDialog = true },
                containerColor = appRedColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                ),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_marshal_logo),
                        contentDescription = "Marshal Logo"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sign Out")
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                showSignOutDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tabs Header
            val taskTabs = listOf("All Tasks", "Active Tasks", "Completed Tasks")

            val pageState = rememberPagerState(
                initialPage = 0,
                pageCount = { taskTabs.size }
            )

            val coroutineScope = rememberCoroutineScope()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                taskTabs.forEachIndexed { index, title ->
                    val isSelected = pageState.currentPage == index

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFFF6347) else Color.Transparent,
                        label = "tabBackgroundColor"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.Gray,
                        label = "tabTextColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(backgroundColor)
                            .clickable {
                                coroutineScope.launch {
                                    pageState.scrollToPage(index)
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 👇 The Contextual Controls (Filters on Left, Sort on Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // Separates left and right elements
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- LEFT SIDE: Filter Chips ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Classic Tasks Chip
                    val classicBg = if (showFilterClassic) MaterialTheme.colorScheme.primary else Color.Transparent
                    val classicContent = if (showFilterClassic) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(classicBg)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .clickable { showFilterClassic = !showFilterClassic }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Classic", fontSize = 12.sp, color = classicContent)
                    }

                    // Checklists Chip
                    val checkBg = if (showFilterChecklist) MaterialTheme.colorScheme.primary else Color.Transparent
                    val checkContent = if (showFilterChecklist) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(checkBg)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .clickable { showFilterChecklist = !showFilterChecklist }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Checklists", fontSize = 12.sp, color = checkContent)
                    }
                }

                // --- RIGHT SIDE: Sort Control ---
                Box {
                    TextButton(
                        onClick = { showSortMenu = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Tasks",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentSortOrder == SortOrder.HIGH_TO_LOW) "High to Low" else "Low to High",
                            fontSize = 14.sp
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("High to Low") },
                            onClick = {
                                currentSortOrder = SortOrder.HIGH_TO_LOW
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Low to High") },
                            onClick = {
                                currentSortOrder = SortOrder.LOW_TO_HIGH
                                showSortMenu = false
                            }
                        )
                    }
                }
            }


            when (val state = uiState) {
                is ToDoUiState.Loading -> {
                    // Displays animated skeleton loader while database loads
                    TaskListShimmerLoading()
                }

                is ToDoUiState.Success -> {
                    val items = state.items

                    HorizontalPager(
                        state = pageState,
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { pageIndex ->

                        // 👇 Added the two filter states as keys!
                        val filteringItems = remember(items, pageIndex, currentSortOrder, showFilterClassic, showFilterChecklist) {

                            // 1. Filter by Tab (All, Active, Completed)
                            val tabFilteredList = when (pageIndex) {
                                0 -> items
                                1 -> items.filter { !it.isCompleted }
                                else -> items.filter { it.isCompleted }
                            }

                            // 2. Filter by Note Type (Classic vs Checklist)
                            val typeFilteredList = tabFilteredList.filter { item ->
                                if (showFilterClassic && !showFilterChecklist) {
                                    item is HomeItem.ClassicTask
                                } else if (showFilterChecklist && !showFilterClassic) {
                                    item is HomeItem.Checklist
                                } else {
                                    // If both are selected, or neither is selected, show everything
                                    true
                                }
                            }

                            // 3. Base sort (High -> Low)
                            val sortedList = typeFilteredList.sortedBy { item ->
                                val priorityName = when (item) {
                                    is HomeItem.ClassicTask -> item.task.priority.name
                                    is HomeItem.Checklist -> item.note.priority.name
                                }

                                when (priorityName.uppercase()) {
                                    "HIGH" -> 1
                                    "MEDIUM" -> 2
                                    "LOW" -> 3
                                    else -> 4
                                }
                            }

                            // 4. Apply the user's chosen direction
                            if (currentSortOrder == SortOrder.HIGH_TO_LOW) {
                                sortedList
                            } else {
                                sortedList.reversed()
                            }
                        }


                        if (filteringItems.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (pageIndex) {
                                        0 -> "It's all alone here!"
                                        1 -> "No Active Tasks Yet!"
                                        else -> "No Completed Tasks Yet!"
                                    },
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (showSwipeHint) {
                                    item {
                                        SwipeHintCard(
                                            onDismiss = {
                                                showSwipeHint = false
                                                // Save the preference so it never shows again
                                                sharedPrefs.edit().putBoolean("showSwipeHint", false).apply()
                                            }
                                        )
                                    }
                                }
                                items(
                                    items = filteringItems,
                                    key = { item -> item.id }
                                ) { item ->

                                    when (item) {
                                        is HomeItem.ClassicTask -> {
                                            val task = item.task
                                            SwipeableToDoItem(
                                                title = task.title,
                                                description = task.description,
                                                priority = task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                                isCompleted = task.isCompleted,
                                                onCheckedChange = { checked ->
                                                    viewModel.updateTask(task.copy(isCompleted = checked))
                                                },
                                                onEditClick = { onEditClick(task.id) },
                                                onCardClick = { onTaskClick(task.id) },
                                                onDeleteClick = { viewModel.deleteTask(task.id) }
                                            )
                                        }
                                        is HomeItem.Checklist -> {
                                            val note = item.note
                                            val completedCount = note.items.count { it.isChecked }
                                            val totalCount = note.items.size
                                            SwipeableToDoItem(
                                                title = note.title.ifEmpty { "Checklist" },
                                                description = "$completedCount/$totalCount (Tasks Completed)",
                                                priority = note.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                                isCompleted = item.isCompleted,
                                                onCheckedChange = {
                                                        checked ->

                                                    val updatedItems = note.items.map { checklistItem ->
                                                        checklistItem.copy(isChecked = checked)
                                                    }

                                                    val updatedNote = note.copy(items = updatedItems)
                                                    viewModel.updateCheckListNote(updatedNote)
                                                },
                                                onEditClick = { onCheckListClick(note.id) },
                                                onCardClick = { onCheckListClick(note.id) },
                                                onDeleteClick = { viewModel.deleteCheckListNote(note.id) }
                                            )
                                        }
                                    }

//                                    HorizontalDivider(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 13.dp),
//                                        thickness = 0.25.dp,
//                                        color = MaterialTheme.colorScheme.onSurface
//                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNoteTypeDialog) {
            NoteTypeDialog(
                onDismissRequest = { showNoteTypeDialog = false },
                onClassicNoteClick = { showAddTaskDialog = true },
                onCheckListNoteClick = { onAddClick() }
            )
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { title, description, priority ->
                    viewModel.createNewTask(title, description, priority) { success ->
                        if (success) {
                            showAddTaskDialog = false
                        }
                    }
                }
            )
        }

        // The Sign-Out Confirmation Dialog
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = {
                    Text(text = "Sign Out")
                },
                text = {
                    Text(text = "Are you sure you want to sign out of Marshal?")
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSignOutDialog = false // Just close the dialog
                        }
                    ) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSignOutDialog = false
                            authViewModel.signOut() // Actually sign the user out here
                        }
                    ) {
                        Text("Yes, Sign Out")
                    }
                }

            )
        }
    }
}

@Composable
fun SwipeHintCard(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Tip",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Tip: Swipe any task or checklist to the left to reveal the 'Delete Button!'",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Tip",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}