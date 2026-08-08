package com.example.marshal.model

import java.util.UUID

data class CheckList(
    var id: String = UUID.randomUUID().toString(), // Changed from val to var
    var text: String = "",
    var isChecked: Boolean = false
) {
    // Required empty constructor for Firebase Firestore deserialization
    constructor() : this(
        id = UUID.randomUUID().toString(),
        text = "",
        isChecked = false
    )
}