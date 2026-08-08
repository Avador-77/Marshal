package com.example.marshal.repository

import android.util.Log
import com.example.marshal.model.CheckListNote
import com.example.marshal.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreRepo {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // 👈 Added Firebase Auth

    // 👇 Helper to dynamically get the private tasks collection for the logged-in user
    private fun getTasksCollection(): CollectionReference? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection("users").document(uid).collection("tasks")
    }

    // 👇 Helper to dynamically get the private checklists collection for the logged-in user
    private fun getCheckListsCollection(): CollectionReference? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection("users").document(uid).collection("checklists")
    }

    fun tasksStream(): Flow<List<Task>> = callbackFlow {
        val collection = getTasksCollection()

        // If the user isn't logged in, send an empty list and close the flow safely
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val taskListener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val toDoTasks = snapshot.toObjects(Task::class.java)
                trySend(toDoTasks)
            }
        }
        awaitClose { taskListener.remove() }
    }

    fun createTask(task: Task, onResult: (Boolean) -> Unit) {
        val collection = getTasksCollection()
        if (collection == null) {
            onResult(false)
            return
        }

        val newDocRef = collection.document()
        val taskWithId = task.copy(id = newDocRef.id)

        newDocRef.set(taskWithId)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Failed to create task", exception)
                onResult(false)
            }
    }

    fun updateTask(task: Task, onResult: (Boolean) -> Unit) {
        val collection = getTasksCollection()
        if (collection != null && task.id.isNotEmpty()) {
            collection.document(task.id).set(task)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Failed to update task", exception)
                    onResult(false)
                }
        } else {
            onResult(false)
        }
    }

    fun deleteTask(taskId: String) {
        val collection = getTasksCollection()
        if (collection != null && taskId.isNotEmpty()) {
            collection.document(taskId).delete()
        }
    }

    fun checkListStream(): Flow<List<CheckListNote>> = callbackFlow {
        val collection = getCheckListsCollection()

        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val checkListListener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val toDoCheckLists = snapshot.toObjects(CheckListNote::class.java)
                trySend(toDoCheckLists)
            }
        }
        awaitClose { checkListListener.remove() }
    }

    fun createCheckListNote(note: CheckListNote, onResult: (Boolean) -> Unit) {
        val collection = getCheckListsCollection()
        if (collection == null) {
            onResult(false)
            return
        }

        val newDocRef = collection.document()
        val noteWithId = note.copy(id = newDocRef.id)

        newDocRef.set(noteWithId)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Failed to create checklist: ", exception)
                onResult(false)
            }
    }

    fun updateCheckListNote(note: CheckListNote, onResult: (Boolean) -> Unit) {
        val collection = getCheckListsCollection()
        if (collection != null && note.id.isNotEmpty()) {
            collection.document(note.id).set(note)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Failed to update checklist: ", exception)
                    onResult(false)
                }
        } else {
            onResult(false)
        }
    }

    fun deleteCheckListNote(noteId: String) {
        val collection = getCheckListsCollection()
        if (collection != null && noteId.isNotEmpty()) {
            collection.document(noteId).delete()
        }
    }
}