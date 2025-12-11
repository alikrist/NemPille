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
import kotlinx.coroutines.flow.filterNotNull

//Medication screen ViewModel now reacts to logged-in user instead of a hardcoded ID
@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val authRepository: AuthenticationRepository
) : ViewModel() {

    //observe the current user from AuthenticationRepository
    //if he logs out - null
    //if logs in - user.id (Int)
    private val currentUserFlow: StateFlow<Int?> =
        authRepository
            .getCurrentUser()              // Flow<User?>
            .map { user -> user?.id }      //Flow<Int?>, ? for ignoring null values
            .stateIn(
                scope = viewModelScope,
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
                scope = viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    //CREATE MEDICATION FOR CURRENT LOGGED IN USER
    fun addMedication(
        name: String,
        dosage: String,
        frequencyPerDay: Int,
        notes: String?
    ) {
        viewModelScope.launch{
            // Wait until we get a non-null userId from the flow
            val userId: Int = currentUserFlow
                .filterNotNull()         // ignore null values
                .firstOrNull()           // suspend until we have a value
                ?: return@launch         // still null? abort safely

            val medication = Medication(
                id = 0,            //0-let room autogenerate
                userId = userId,   //link medication to current user
                name = name,
                dosage = dosage,
                frequencyPerDay = frequencyPerDay,
                notes = notes
            )
            medicationRepository.addMedication(medication)
        }
    }
    //a test helper to insert hardcoded medication
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

    // LOAD ONE MEDICATION (for editing)
    suspend fun getMedication(medicationId: Int): Medication? {
        return medicationRepository.getMedicationById(medicationId)
    }

    // UPDATE MEDICATION
    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.updateMedication(medication)
        }
    }

    //DELETE MEDICATION THAT BELONGS TO LOGGED IN USER
    //view-model receives medication objects from UI
    //launches coroutine
    //calls repository-dao-room-deletes the row
    //bc of the flow, medication deletes automatically

    fun deleteMedication(medication: Medication){
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)
        }
    }
}