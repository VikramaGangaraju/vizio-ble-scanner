//package com.vizio.viziobleoobe.ui.screens
//
//import android.annotation.SuppressLint
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import com.vizio.viziobleoobe.ble.BleViewModel
//import com.vizio.viziobleoobe.navigation.Screen
//
//@SuppressLint("MissingPermission")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DeviceDetailScreen(viewModel: BleViewModel, navController: NavHostController) {
//    val device = viewModel.connectedDevice.value
//    val services = viewModel.gattServices.value
//    val context = LocalContext.current
//    val lastReadValue by viewModel.lastReadCharacteristicValue.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Device Details", fontSize = 20.sp) },
//                navigationIcon = {
//                    IconButton(onClick = {
//                        navController.navigate(Screen.DeviceList.route)
//                    }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .padding(16.dp)
//                .fillMaxSize()
//        ) {
//            Text(
//                text = "Connected to: ${device?.name ?: "Unnamed Device"}",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Log.d("DeviceDetailScreen", "Displaying ${services.size} services for device: ${device?.name ?: "Unnamed Device"}")
//                items(services.size) { index ->
//                    val service = services[index]
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "Service: ${service.uuid}",
//                                style = MaterialTheme.typography.titleSmall,
//                                color = Color(0xFF1976D2),
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            )
//                            service.characteristics.forEachIndexed { charIndex, characteristic ->
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .background(
//                                            if (charIndex % 2 == 0) Color(0xFFBBDEFB) else Color(0xFF90CAF9)
//                                        )
//                                        .padding(8.dp)
//                                        .clickable {
//                                            val value = viewModel.readCharacteristic(characteristic.uuid)
//                                            if (value != null) {
//                                                Log.d("DeviceDetailScreen", "Read value: ${String(value)}")
//                                            } else {
//                                                Log.e("DeviceDetailScreen", "Failed to read characteristic: ${characteristic.uuid}")
//                                            }
//                                        },
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Characteristic: ${characteristic.uuid}",
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        color = Color.Black
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Display toast for the last read characteristic value
//        LaunchedEffect(lastReadValue) {
//            lastReadValue?.let {
//                Toast.makeText(
//                    context,
//                    "Value: $it",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//}