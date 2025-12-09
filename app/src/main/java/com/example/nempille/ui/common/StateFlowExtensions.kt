package com.example.nempille.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

// Simple wrapper so we don't worry about lifecycle-runtime-compose for now.
// Later you can switch to collectAsStateWithLifecycle from androidx.lifecycle.
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycleSafe(): State<T> = collectAsState()
