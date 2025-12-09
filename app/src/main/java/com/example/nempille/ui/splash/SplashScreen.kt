package com.example.nempille.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nempille.ui.common.collectAsStateWithLifecycleSafe
import com.example.nempille.ui.navigation.Screen

// SplashScreen:
//1.Shows a simple loading UI
//2. observes SplashViewModel.targetRoute
//3. When targetRoute is decided, navigates there and removes splash from back stack
@Composable
fun SplashScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    //Observe the target route (null while deciding)
    val targetRoute by viewModel.targetRoute.collectAsStateWithLifecycleSafe()

    //When targetRoute becomes non-null, navigate once
    LaunchedEffect(targetRoute) {
        val route = targetRoute
        if (route != null) {
            navController.navigate(route) {
                // Remove Splash from back stack so user cannot go back to it
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    //Simple centered splash UI
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "NemPille",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Preparing your pills…",
                style = MaterialTheme.typography.bodyMedium
            )
            CircularProgressIndicator()
        }
    }
}