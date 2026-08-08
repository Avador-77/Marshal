package com.example.marshal.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marshal.model.CheckList
import com.example.marshal.model.CheckListNote
import com.example.marshal.repository.FirestoreRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CheckListUiState {
    object Loading : CheckListUiState()
    data class Success(val checkListNotes: List<CheckListNote>) : CheckListUiState()
}

class CheckListViewModel(
    private val repo: FirestoreRepo = FirestoreRepo()
) : ViewModel() {

    // Stream state for displaying saved checklist notes on the home screen
    val uiState: StateFlow<CheckListUiState> = repo.checkListStream()
        .map<List<CheckListNote>, CheckListUiState> { notes ->
            CheckListUiState.Success(notes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CheckListUiState.Loading
        )

    var title = mutableStateOf("")
        private set

    var priority = mutableStateOf(Priority.MEDIUM)
        private set

    val checkListItems = mutableStateListOf(CheckList())

    var isLoading = mutableStateOf(false)
        private set

    // Track the current note ID (null if creating a new note)
    private var currentNoteId: String? = null

    fun onTitleChange(newTitle: String) {
        title.value = newTitle
    }

    fun onPriorityChange(newPriority: Priority) {
        priority.value = newPriority
    }

    fun onItemTextChange(index: Int, newText: String) {
        if (index in checkListItems.indices) {
            checkListItems[index] = checkListItems[index].copy(text = newText)
        }
    }

    fun onItemCheckedChange(index: Int, isChecked: Boolean) {
        if (index in checkListItems.indices) {
            checkListItems[index] = checkListItems[index].copy(isChecked = isChecked)
        }
    }

    fun addNewItem() {
        checkListItems.add(CheckList())
    }

    fun removeItem(index: Int) {
        if (checkListItems.size > 1 && index in checkListItems.indices) {
            checkListItems.removeAt(index)
        } else if (checkListItems.size == 1) {
            checkListItems[0] = CheckList()
        }
    }

    // Populate data when editing an existing note
    fun loadExistingNote(note: CheckListNote) {
        currentNoteId = note.id
        title.value = note.title
        priority.value = note.priority
        checkListItems.clear()
        if (note.items.isNotEmpty()) {
            checkListItems.addAll(note.items)
        } else {
            checkListItems.add(CheckList())
        }
    }

    // Reset form state for new checklist creation
    fun resetForm() {
        currentNoteId = null
        title.value = ""
        priority.value = Priority.LOW
        checkListItems.clear()
        checkListItems.add(CheckList())
    }

    fun createCheckListNote(onResult: (Boolean) -> Unit) {
        val validItems = checkListItems.filter { it.text.isNotBlank() }
        val noteTitle = title.value.ifBlank { "Untitled Checklist" }

        if (validItems.isEmpty()) {
            onResult(false)
            return
        }

        isLoading.value = true

        viewModelScope.launch {
            val note = CheckListNote(
                id = currentNoteId ?: "",
                title = noteTitle,
                items = validItems,
                priority = priority.value,
                createdOn = System.currentTimeMillis()
            )


            if (currentNoteId.isNullOrEmpty()) {
                // It's a brand new note -> Create it
                repo.createCheckListNote(note) { isSuccess ->
                    isLoading.value = false
                    if (isSuccess) resetForm()
                    onResult(isSuccess)
                }
            } else {
                // It's an existing note -> Update it
                repo.updateCheckListNote(note) { isSuccess ->
                    isLoading.value = false
                    if (isSuccess) resetForm()
                    onResult(isSuccess)
                }
            }
        }
    }

    fun deleteCheckListNote(noteId: String) {
        viewModelScope.launch {
            repo.deleteCheckListNote(noteId)
        }
    }
}