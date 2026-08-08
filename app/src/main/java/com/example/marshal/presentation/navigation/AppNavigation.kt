package com.example.marshal.presentation.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.marshal.presentation.AuthViewModel
import com.example.marshal.presentation.CheckListNote
import com.example.marshal.presentation.CheckListUiState
import com.example.marshal.presentation.CheckListViewModel
import com.example.marshal.presentation.EditTaskScreen
import com.example.marshal.presentation.HomeScreen
import com.example.marshal.presentation.TaskDetailScreen
import com.example.marshal.presentation.ToDoUiState
import com.example.marshal.presentation.ToDoViewModel
import com.example.marshal.presentation.HomeItem
import com.example.marshal.presentation.LoginScreen

@Composable
fun AppNavigation() {
    val navDriver = rememberNavController()
    val viewModel: ToDoViewModel = viewModel()
    val checkListViewModel: CheckListViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsStateWithLifecycle()

    NavHost(
        navController = navDriver,
        startDestination = if (isUserAuthenticated) MarshalRoutes.Home else MarshalRoutes.Login
    ) {

        composable<MarshalRoutes.Login> {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navDriver.navigate(MarshalRoutes.Home) {
                        popUpTo(MarshalRoutes.Login) { inclusive = true }
                    }
                }
            )
        }


        composable<MarshalRoutes.Home> {
            HomeScreen(
                authViewModel = authViewModel,
                onTaskClick = { taskId ->
                    navDriver.navigate(MarshalRoutes.TaskDetail(taskId = taskId))
                },
                onEditClick = { taskId ->
                    navDriver.navigate(MarshalRoutes.EditTask(taskId = taskId))
                },
                onCheckListClick = { checkListId ->
                    navDriver.navigate(MarshalRoutes.CheckListDetail(checkListId = checkListId))
                },
                onAddClick = {
                    navDriver.navigate(MarshalRoutes.CreateCheckList)
                }
            )
        }

        composable<MarshalRoutes.TaskDetail> { backStackEntry ->
            val detailScreenRoute: MarshalRoutes.TaskDetail = backStackEntry.toRoute()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // 1. Read the combined items list
            val items = (uiState as? ToDoUiState.Success)?.items ?: emptyList()

            // 2. Find the item, ensure it's a ClassicTask, and extract the underlying 'task'
            val currentTask = (items.find { it.id == detailScreenRoute.taskId } as? HomeItem.ClassicTask)?.task

            currentTask?.let { task ->
                TaskDetailScreen(
                    task = task,
                    onBackClick = {
                        navDriver.popBackStack()
                    },
                    onEditClick = {
                        navDriver.navigate(MarshalRoutes.EditTask(taskId = detailScreenRoute.taskId))
                    },
                    onDeleteClick = {
                        viewModel.deleteTask(detailScreenRoute.taskId)
                        navDriver.popBackStack()
                    }
                )
            }
        }

        composable<MarshalRoutes.EditTask> { destination ->
            val route: MarshalRoutes.EditTask = destination.toRoute()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // 1. Read the combined items list
            val items = (uiState as? ToDoUiState.Success)?.items ?: emptyList()

            // 2. Find the item, ensure it's a ClassicTask, and extract the underlying 'task'
            val currentTask = (items.find { it.id == route.taskId } as? HomeItem.ClassicTask)?.task

            currentTask?.let { taskToEdit ->
                EditTaskScreen(
                    task = taskToEdit,
                    onBackClick = {
                        navDriver.popBackStack()
                    },
                    onSaveClick = { updatedTask ->
                        viewModel.updateTask(updatedTask) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, "Task Updated!", Toast.LENGTH_SHORT).show()
                                navDriver.popBackStack()
                            } else {
                                Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }

        composable<MarshalRoutes.CreateCheckList> {
            CheckListNote(
                viewModel = checkListViewModel,
                onNavigateBack = { navDriver.popBackStack() }
            )
        }

        composable<MarshalRoutes.CheckListDetail> { backStackEntry ->
            val route: MarshalRoutes.CheckListDetail = backStackEntry.toRoute()

            // This remains untouched because CheckListViewModel still maintains its own CheckListUiState
            val uiState by checkListViewModel.uiState.collectAsStateWithLifecycle()
            val checkListNotes = (uiState as? CheckListUiState.Success)?.checkListNotes ?: emptyList()
            val currentNote = checkListNotes.find { it.id == route.checkListId }

            currentNote?.let { note ->
                CheckListNote(
                    viewModel = checkListViewModel,
                    existingNote = note,
                    onNavigateBack = { navDriver.popBackStack() }
                )
            }
        }
    }
}