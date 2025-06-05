package com.vizio.viziobleoobe.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vizio.viziobleoobe.ble.BleViewModel
import com.vizio.viziobleoobe.navigation.Screen

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(viewModel: BleViewModel, navController: NavHostController) {
    val device = viewModel.connectedDevice.value
    val services = viewModel.gattServices.value
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.disconnectFromDevice()
                        navController.navigate(Screen.DeviceList.route)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            Text(text = "Connected to: ${device?.name ?: "Unnamed Device"}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(services.size) { index ->
                    val service = services[index]
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = "Service: ${service.uuid}")
                        service.characteristics.forEach { characteristic ->
                            Text(text = "Characteristic: ${characteristic.uuid}", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}