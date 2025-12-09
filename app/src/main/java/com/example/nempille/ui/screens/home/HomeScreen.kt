package com.example.nempille.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nempille.domain.model.UserRole
import com.example.nempille.ui.auth.AuthViewModel
import com.example.nempille.ui.navigation.Screen

//home screen shown after LOgin
//nav demo - added buttons to go to other screens
@Composable
fun HomeScreen (
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    //only redirect when auth state is initialized AND user is not logged in
    LaunchedEffect(uiState.isAuthInitialized, uiState.isLoggedIn) {
        if (uiState.isAuthInitialized && !uiState.isLoggedIn) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Welcome, ${uiState.currentUser?.name ?: ""}",
        )

        Text(
            text = "Role: ${uiState.currentUser?.role ?: ""}",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Different UI based on role
        when (uiState.currentUser?.role) {
            UserRole.PATIENT -> {
                Text("Patient Dashboard")

                Button(onClick = { navController.navigate(Screen.MedicationList.route) }) {
                    Text("Your Medications")
                }
            }

            UserRole.CAREGIVER -> {
                Text("Caregiver Dashboard")

                Button(onClick = { navController.navigate(Screen.Caregiver.route) }) {
                    Text("Your Patients")
                }
            }

            null -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate(Screen.Settings.route) }) {
            Text(text = "Go to Settings")
        }

        Button(onClick = { navController.navigate(Screen.Notifications.route) }) {
            Text(text = "Go to Notifications")
        }

        Spacer(modifier = Modifier.height(32.dp))

        //LOGOUT BUTTON
        Button(onClick = { viewModel.logout() }) {
            Text("Log out")
        }
    }
}