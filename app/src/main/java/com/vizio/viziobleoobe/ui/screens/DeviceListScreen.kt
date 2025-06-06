package com.vizio.viziobleoobe.ui.screens

import android.annotation.SuppressLint
import com.vizio.viziobleoobe.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.vizio.viziobleoobe.ble.BleViewModel
import com.vizio.viziobleoobe.navigation.Screen
import com.vizio.viziobleoobe.ui.components.DeviceItem
import com.vizio.viziobleoobe.util.PermissionsUtil

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(viewModel: BleViewModel, navController: NavHostController) {
    val devices = viewModel.scannedDevices
    val connectedDevice = viewModel.connectedDevice.value
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (PermissionsUtil.hasPermissions(context)) {
            viewModel.autoConnectToSavedDevice()
            viewModel.startScan()
        } else {
            // Handle missing permission (e.g., show a message or request permission)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.ble_device_scanner)) })
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        isScanning = !isScanning
                        if (isScanning) {
                            viewModel.startScan()
                        } else {
                            viewModel.stopScan()
                        }
                    }
                ) {
                    Text(
                        text = if (isScanning) "Stop Scanning" else "Start Scanning",
                        fontSize = 18.sp
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(devices.size) { index ->
                val device = devices[index]
                DeviceItem(
                    device = device,
                    isConnected = connectedDevice?.address == device.address,
                    onConnect = {
                        viewModel.connectToDevice(device)
                        navController.navigate(Screen.DeviceDetail.route)
                    },
                    onDisconnect = {
                        viewModel.disconnectFromDevice()
                    },
                    onItemClick = {
                        if (connectedDevice?.address == device.address) {
                            navController.navigate(Screen.DeviceDetail.route)
                        }
                    }
                )

                Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            }
        }
    }
}