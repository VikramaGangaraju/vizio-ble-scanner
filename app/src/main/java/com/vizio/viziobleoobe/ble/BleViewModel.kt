package com.vizio.viziobleoobe.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application.applicationContext)
    val scannedDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevice = mutableStateOf<BluetoothDevice?>(null)
    val gattServices = mutableStateOf<List<BluetoothGattService>>(emptyList())
    private val _lastReadCharacteristicValue = MutableStateFlow<String?>(null)
    val lastReadCharacteristicValue: StateFlow<String?> = _lastReadCharacteristicValue
    private val sharedPreferences = application.getSharedPreferences("BlePreferences", Application.MODE_PRIVATE)

    init {
        autoConnectToSavedDevice()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BleViewModel", "Connection state changed: status=$status, newState=$newState, device=${gatt.device.name} - ${gatt.device.address}")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BleViewModel", "Device connected: ${gatt.device.name} - ${gatt.device.address}")
                connectedDevice.value = gatt.device
                saveConnectedDevice(gatt.device)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BleViewModel", "Device disconnected: ${gatt.device.name} - ${gatt.device.address}")
                connectedDevice.value = null
                gattServices.value = emptyList()
                clearConnectedDevice()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattServices.value = gatt.services
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value?.let { String(it) } ?: "null"
                _lastReadCharacteristicValue.value = value
                Log.d("BleViewModel", "Characteristic read: ${characteristic.uuid}, value: $value")
            } else {
                Log.e("BleViewModel", "Failed to read characteristic: ${characteristic.uuid}, status: $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleViewModel", "Characteristic written successfully: ${characteristic.uuid}")
            } else {
                Log.e("BleViewModel", "Failed to write characteristic: ${characteristic.uuid}, status: $status")
            }
        }
    }

    fun startScan() {
        Log.d("BleViewModel", "Starting BLE scan")
        if (hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            bleManager.startScan(scanCallback)
        }
    }

    fun stopScan() {
        Log.d("BleViewModel", "Stopping BLE scan")
        if (hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            bleManager.stopScan(scanCallback)
        }
    }

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            // Only add devices that broadcast a name
            if (device.name != null && !scannedDevices.any { it.address == device.address }) {
                scannedDevices.add(device)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: List<android.bluetooth.le.ScanResult>) {
            results.forEach { result ->
                val device = result.device
                if (device.name != null && !scannedDevices.any { it.address == device.address }) {
                    scannedDevices.add(device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleViewModel", "Scan failed with error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        Log.d("BleViewModel", "Connecting to device: ${device.name} - ${device.address}")
        bleManager.connectToDevice(device, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromDevice() {
        Log.d("BleViewModel", "Disconnecting from device: ${connectedDevice.value?.name} - ${connectedDevice.value?.address}")
        bleManager.disconnect()
        connectedDevice.value = null
        gattServices.value = emptyList()
        clearConnectedDevice()
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristicValue(characteristic: BluetoothGattCharacteristic) {
        Log.d("BleViewModel", "Reading characteristic value: ${characteristic.uuid}")
        val gatt = bleManager.getConnectedGatt()
        if (gatt != null) {
            val success = gatt.readCharacteristic(characteristic)
            if (!success) {
                Log.e("BleViewModel", "Failed to initiate characteristic read: ${characteristic.uuid}")
            }
        } else {
            Log.e("BleViewModel", "No connected GATT instance available")
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveConnectedDevice(device: BluetoothDevice) {
        Log.d("BleViewModel", "Saving connected device to preferences: ${device.name} - ${device.address}")
        sharedPreferences.edit().apply {
            putString("device_address", device.address)
            apply()
        }
    }

    private fun clearConnectedDevice() {
        Log.d("BleViewModel", "Clearing connected device from preferences")
        sharedPreferences.edit().clear().apply()
    }

    fun autoConnectToSavedDevice() {
        Log.d("BleViewModel", "Attempting to auto-connect to saved device")
        val deviceAddress = sharedPreferences.getString("device_address", null)
        if (deviceAddress != null) {
            val bluetoothDevice = bleManager.getDeviceByAddress(deviceAddress)
            if (bluetoothDevice != null && hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                if (gattCallback != null) { // Ensure gattCallback is not null
                    bleManager.connectToDevice(bluetoothDevice, gattCallback)
                } else {
                    Log.e("BleViewModel", "gattCallback is null, cannot connect to device")
                }
            } else {
                Log.e("BleViewModel", "Bluetooth device is null or missing permission")
            }
        } else {
            Log.d("BleViewModel", "No saved device to auto-connect")
        }
    }

    private fun hasPermission(permission: String): Boolean {
        Log.d("BleViewModel", "Checking permission: $permission")
        return ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED
    }
}