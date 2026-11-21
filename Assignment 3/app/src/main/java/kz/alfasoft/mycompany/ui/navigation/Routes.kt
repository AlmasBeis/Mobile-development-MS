package kz.alfasoft.mycompany.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object Login
@Serializable data object Home
@Serializable data object Leave
@Serializable data object LeaveNew
@Serializable data class LeaveDetail(val id: Int)
@Serializable data class LeaveEdit(val id: Int)
@Serializable data object Org
@Serializable data class Employee(val id: String)
@Serializable data object Approvals
@Serializable data object Profile
