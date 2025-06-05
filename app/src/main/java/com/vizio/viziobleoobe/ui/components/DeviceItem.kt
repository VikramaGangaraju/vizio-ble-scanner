package com.vizio.viziobleoobe.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = device.name ?: "Unnamed Device", style = MaterialTheme.typography.bodyMedium)
            Text(text = device.address ?: "Unknown Address", style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = {
                if (isConnected) {
                    onDisconnect()
                } else {
                    onConnect()
                }
            }
        ) {
            Text(text = if (isConnected) "Disconnect" else "Connect")
        }
    }
}