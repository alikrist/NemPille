package com.example.nempille.ui.auth

//ViewModel only talks to use cases + repository, not directly to ROOM/DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nempille.domain.model.User
import com.example.nempille.domain.model.UserRole
import com.example.nempille.domain.repository.AuthenticationRepository
import com.example.nempille.domain.usecase.LoginUseCase
import com.example.nempille.domain.usecase.LogoutUseCase
import com.example.nempille.domain.usecase.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//UI state for the auth screen: what UI needs to show / know
data class AuthUiState(
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.PATIENT,  // default role
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null
)

//ViewModel that sits between UI and auth backend (use cases + repository)
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthenticationRepository
) : ViewModel() {

    //Backing state flow (mutable only inside ViewModel)
    private val _uiState = MutableStateFlow(AuthUiState())
    // Public state seen by UI
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        //Observe current user from repository -> update UI state automatically
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _uiState.update { state ->
                    state.copy(
                        currentUser = user,
                        isLoggedIn = user != null
                    )
                }
            }
        }
    }

    //Called when user types email in TextField
    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    //Called when user types name (for signup)
    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName, errorMessage = null) }
    }

    //Called when user types phone (for signup)
    fun onPhoneChanged(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone, errorMessage = null) }
    }

    //Called when user selects role (patient / caregiver)
    fun onRoleChanged(newRole: UserRole) {
        _uiState.update { it.copy(role = newRole, errorMessage = null) }
    }

    //Handle login button click
    // onSuccess is a callback that LoginScreen can use to navigate
    // only if login succeeded
    fun login(onSuccess: () -> Unit = {}) {
        val email = _uiState.value.email.trim()

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Email cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = loginUseCase(email)

            result
                .onSuccess {
                    // We don't need to manually update currentUser/isLoggedIn here,
                    // because the Flow in init{} will emit the new user.
                    _uiState.update { state ->
                        state.copy(isLoading = false, errorMessage = null)
                    }
                    // tell the screen that login succeeded
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unknown login error"
                        )
                    }
                }
        }
    }

    //Handle signup button click (for future SignupScreen)
    fun signup() {
        val state = _uiState.value

        if (state.name.isBlank() || state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name and email are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = signupUseCase(
                name = state.name.trim(),
                email = state.email.trim(),
                role = state.role,
                phone = state.phone.ifBlank { null }
            )

            result
                .onSuccess {
                    _uiState.update { s ->
                        s.copy(isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unknown signup error"
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}