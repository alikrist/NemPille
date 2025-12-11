package com.example.nempille.ui.screens.settings

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@SuppressLint("MissingPermission")
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: BluetoothViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- 1. PERMISSION LOGIC ---
    // Define which permissions we need based on Android version
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ (Target SDK 31+)
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        // Android 11 or lower
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // Create the launcher that shows the popup
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        // This runs when the user clicks "Allow" or "Deny"
        val allGranted = perms.values.all { it }
        if (allGranted) {
            // Permission granted! Now we can scan safely.
            viewModel.scanForDevices()
        } else {
            Toast.makeText(context, "Bluetooth permission is needed to scan!", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bluetooth Settings",
                style = MaterialTheme.typography.headlineSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (uiState.isScanning) "Scanning..." else "Scan")
                Spacer(modifier = Modifier.width(8.dp))

                // --- SWITCH WITH PERMISSION CHECK ---
                Switch(
                    checked = uiState.isScanning,
                    onCheckedChange = { shouldScan ->
                        if (shouldScan) {
                            // If turning ON, ask for permission first
                            permissionLauncher.launch(permissionsToRequest)
                        } else {
                            // If turning OFF, just stop
                            viewModel.scanForDevices()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isScanning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- DEVICE LIST ---
        Text(
            text = "Available Devices:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(uiState.scannedDevices) { device ->
                BluetoothDeviceItem(
                    device = device,
                    onClick = {
                        // We also need permission to connect!
                        permissionLauncher.launch(permissionsToRequest)
                        viewModel.connectToDevice(device)
                    }
                )
            }
        }

        // --- FOOTER ---
        Spacer(modifier = Modifier.height(16.dp))
        StatusCard(uiState.connectedDevice, uiState.errorMessage)
    }
}

// --- HELPER COMPONENTS ---

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceItem(device: BluetoothDevice, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Text(text = device.name ?: "Unknown Device", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = device.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun StatusCard(connectedDevice: String?, errorMessage: String?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Status", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            if (connectedDevice != null) {
                Text(text = "✅ Connected to: $connectedDevice", color = Color(0xFF006400))
            } else {
                Text(text = "❌ Not Connected")
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}