package com.vizio.viziobleoobe.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.bluetooth.BluetoothDevice
import androidx.compose.material3.MaterialTheme

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(device: BluetoothDevice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(text = device.name ?: "Unnamed Device")
            Text(text = device.address ?: "Unknown Address", style = MaterialTheme.typography.bodySmall)
        }
    }
}