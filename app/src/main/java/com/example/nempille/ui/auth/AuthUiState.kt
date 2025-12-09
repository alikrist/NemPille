package com.example.nempille.ui.auth

////UI state for the auth screen: what UI needs to show / know

import com.example.nempille.domain.model.User
import com.example.nempille.domain.model.UserRole

data class AuthUiState(
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.PATIENT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,

    //tells whether loading is finished from DataStore
    val isAuthInitialized: Boolean = false
)
