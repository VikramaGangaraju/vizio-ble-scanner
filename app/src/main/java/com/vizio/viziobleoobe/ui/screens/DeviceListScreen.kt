package com.vizio.viziobleoobe.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vizio.viziobleoobe.ble.BleViewModel
import com.vizio.viziobleoobe.navigation.Screen
import com.vizio.viziobleoobe.util.PermissionsUtil

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(viewModel: BleViewModel, navController: NavHostController) {
    val devices = viewModel.scannedDevices
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (PermissionsUtil.hasPermissions(context)) {
            viewModel.startScan()
        } else {
            // Handle missing permission (e.g., show a message or request permission)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("BLE Device Scanner") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(devices.size) { index ->
                val device = devices[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.connectToDevice(device)
                            navController.navigate(Screen.DeviceDetail.route)
                        }
                        .padding(16.dp)
                ) {
                    Text(text = device.name ?: "Unnamed Device")
                }
            }
        }
    }
}