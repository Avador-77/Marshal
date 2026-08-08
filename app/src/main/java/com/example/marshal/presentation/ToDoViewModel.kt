package com.example.marshal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marshal.model.Task
import com.example.marshal.repository.FirestoreRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.marshal.model.CheckListNote

import kotlinx.coroutines.flow.combine

// Ensure you have these imports pointing to your actual models and repo:
// import com.example.marshal.model.Task
// import com.example.marshal.model.CheckListNote
// import com.example.marshal.repository.FirestoreRepo

sealed class HomeItem {
    abstract val id: String
    abstract val isCompleted: Boolean

    data class ClassicTask(val task: Task) : HomeItem() {
        override val id = task.id
        override val isCompleted = task.isCompleted
    }

    data class Checklist(val note: CheckListNote) : HomeItem() {
        override val id = note.id
        // A checklist is "completed" if it has items AND all of them are checked
        override val isCompleted = note.items.isNotEmpty() && note.items.all { it.isChecked }
    }
}

sealed class ToDoUiState {
    object Loading : ToDoUiState()
    data class Success(val items: List<HomeItem>) : ToDoUiState()
}

class ToDoViewModel(
    private val repo: FirestoreRepo = FirestoreRepo()
) : ViewModel() {

    val uiState: StateFlow<ToDoUiState> = combine(
        repo.tasksStream(),
        repo.checkListStream()
    ) { tasks, checklists ->

        val combinedList = mutableListOf<HomeItem>()

        // Wrap classic tasks
        combinedList.addAll(tasks.map { HomeItem.ClassicTask(it) })
        // Wrap checklists
        combinedList.addAll(checklists.map { HomeItem.Checklist(it) })

        ToDoUiState.Success(combinedList)

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ToDoUiState.Loading
    )

    fun createNewTask(title: String, description: String, priority: Priority, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority, // 👈 Now safely passes the enum straight to the data class
                isCompleted = false
            )
            repo.createTask(task) { isSuccess ->
                onResult(isSuccess)
            }
        }
    }

    fun updateTask(updatedTask: Task, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repo.updateTask(updatedTask) { isSuccess ->
                onComplete(isSuccess)
            }
        }
    }

    fun updateCheckListNote(note: CheckListNote, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repo.updateCheckListNote(note) { isSuccess ->
                onComplete(isSuccess)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repo.deleteTask(taskId)
        }
    }

    fun deleteCheckListNote(noteId: String) {
        viewModelScope.launch {
            repo.deleteCheckListNote(noteId)
        }
    }
}