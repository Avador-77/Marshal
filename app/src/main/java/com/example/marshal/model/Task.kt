package com.example.marshal.model

import com.example.marshal.presentation.Priority
import com.google.firebase.firestore.DocumentId
import com.google.type.DateTime

data class Task(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.LOW,
    @field:JvmField
    val isCompleted: Boolean = false,
    val createdOn: Long = System.currentTimeMillis()
)
