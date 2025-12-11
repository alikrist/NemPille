package com.example.nempille.domain.repository

import com.example.nempille.domain.model.Medication
import kotlinx.coroutines.flow.Flow

// Repository interface that describes WHAT operations we need
// for medications, without saying HOW they are implemented
interface MedicationRepository {

    // Observe all medications for one specific user (patient)
    // Flow means: whenever database changes, the UI can react
    fun getMedicationsForUser(userId: Int): Flow<List<Medication>>

    // Add a new medication to the database
    suspend fun addMedication(medication: Medication)

    // Update an existing medication
    suspend fun updateMedication(medication: Medication)

    // Delete a medication
    suspend fun deleteMedication(medication: Medication)

    //Get medication by ID
    suspend fun getMedicationById(id: Int): Medication?
}
