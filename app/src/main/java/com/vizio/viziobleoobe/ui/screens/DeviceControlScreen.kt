package com.vizio.viziobleoobe.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.vizio.viziobleoobe.ble.BleTvDpadSelection
import com.vizio.viziobleoobe.ble.BleViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlScreen(viewModel: BleViewModel, navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Control", fontSize = 20.sp) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // D-Pad UI
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    handleDpadCommand(viewModel, BleTvDpadSelection.UP)
                }) {
                    Text("Up")
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        handleDpadCommand(viewModel, BleTvDpadSelection.LEFT)
                    }) {
                        Text("Left")
                    }
                    Button(onClick = {
                        handleDpadCommand(viewModel, BleTvDpadSelection.OK)
                    }) {
                        Text("Ok")
                    }
                    Button(onClick = {
                        handleDpadCommand(viewModel, BleTvDpadSelection.RIGHT)
                    }) {
                        Text("Right")
                    }
                }
                Button(onClick = {
                    handleDpadCommand(viewModel, BleTvDpadSelection.DOWN)
                }) {
                    Text("Down")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Keyboard Button
            Button(onClick = { /* Handle Keyboard action */ }) {
                Text("Keyboard")
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun handleDpadCommand(viewModel: BleViewModel, command: BleTvDpadSelection) {
    Log.d("DeviceControlScreen", "Sending command: ${command.name}, value: ${command.value}")
    val success = viewModel.writeDpadCommand(command)
    if (success) {
        Log.d("DeviceControlScreen", "Command ${command.name} sent successfully")
    } else {
        Log.e("DeviceControlScreen", "Failed to send command ${command.name}")
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Device Control Screen Preview")
@Composable
fun PreviewDeviceControlScreen() {
    val mockViewModel = BleViewModel(
        application = TODO()/* Pass required dependencies or mocks */
    )
    val mockNavController = rememberNavController()

    DeviceControlScreen(viewModel = mockViewModel, navController = mockNavController)
}