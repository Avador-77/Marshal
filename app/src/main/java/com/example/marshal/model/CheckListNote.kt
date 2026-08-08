package com.example.marshal.model

import com.example.marshal.presentation.Priority
import com.google.firebase.firestore.DocumentId

data class CheckListNote(
    @DocumentId
    var id: String = "",
    var title: String = "",
    var items: List<CheckList> = emptyList(),
    var priority: Priority = Priority.LOW,
    var createdOn: Long = System.currentTimeMillis()
) {
    // Required empty constructor for Firebase Firestore deserialization
    constructor() : this(
        id = "",
        title = "",
        items = emptyList(),
        priority = Priority.LOW,
        createdOn = System.currentTimeMillis()
    )
}