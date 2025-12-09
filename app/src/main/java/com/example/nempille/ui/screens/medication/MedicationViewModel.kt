package com.example.nempille.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nempille.domain.model.Medication
import com.example.nempille.domain.repository.AuthenticationRepository
import com.example.nempille.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//Medication screen ViewModel now reacts to logged-in user instead of a hardcoded ID
@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val authRepository: AuthenticationRepository
) : ViewModel() {

    //observe the current user from AuthenticationRepository
    //if he logs out - null
    //if logs in - correct userId
    private val currentUserFlow = authRepository
        .getCurrentUser()              // Flow<User?>
        .filterNotNull()               // Ignore null values
        .map { it.id }                 // Convert User → userId (Int)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null                        // initial state: no user yet
        )

    //medications updates when db changes, logged user changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val medications: StateFlow<List<Medication>> =
        currentUserFlow
            .filterNotNull()                       // Wait until we have a userId
            .flatMapLatest { userId ->
                // Load medications for this user
                medicationRepository.getMedicationsForUser(userId)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    //a test medication for currently logged in
    fun addSampleMedication() {
        viewModelScope.launch {
            val userId = currentUserFlow.value ?: return@launch  // No user logged in

            val sample = Medication(
                id = 0,                     //Room auto-generates the ID
                userId = userId,            //The REAL logged-in user
                name = "Ibuprofen",
                dosage = "1 pill",
                frequencyPerDay = 3,
                notes = "After meals"
            )

            medicationRepository.addMedication(sample)
        }
    }
}