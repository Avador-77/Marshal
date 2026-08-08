package com.example.marshal.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface MarshalRoutes {
    @Serializable
    data object Home: MarshalRoutes

    @Serializable
    data object Login

    @Serializable
    data class TaskDetail(val taskId: String): MarshalRoutes

    @Serializable
    data class EditTask(val taskId: String): MarshalRoutes

    @Serializable
    data object CreateCheckList: MarshalRoutes

    @Serializable
    data class CheckListDetail(val checkListId: String): MarshalRoutes

}