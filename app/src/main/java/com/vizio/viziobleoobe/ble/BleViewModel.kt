package com.vizio.viziobleoobe.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel

class BleViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application.applicationContext)
    val scannedDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevice = mutableStateOf<BluetoothDevice?>(null)
    val gattServices = mutableStateOf<List<BluetoothGattService>>(emptyList())

    private val sharedPreferences = application.getSharedPreferences("BlePreferences", Application.MODE_PRIVATE)

    init {
        // Reload saved device state on initialization
        autoConnectToSavedDevice()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BleViewModel", "Connection state changed: status=$status, newState=$newState for device: ${gatt.device.address}")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BleViewModel", "Connected to device: ${gatt.device.address}")
                connectedDevice.value = gatt.device
                saveConnectedDevice(gatt.device)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BleViewModel", "Disconnected from device: ${gatt.device.address}")
                connectedDevice.value = null
                gattServices.value = emptyList()
                clearConnectedDevice()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("BleViewModel", "Services discovered for device: ${gatt.device.address}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattServices.value = gatt.services
            } else {
                Log.e("BleViewModel", "Failed to discover services: status=$status")
            }
        }
    }

    fun startScan() {
        if (hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            Log.d("BleViewModel", "Starting BLE scan")
            bleManager.startScan(scanCallback)
        } else {
            Log.e("BleViewModel", "Missing BLUETOOTH_SCAN permission")
        }
    }

    fun stopScan() {
        if (hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            Log.d("BleViewModel", "Stopping BLE scan")
            bleManager.stopScan(scanCallback)
        } else {
            Log.e("BleViewModel", "Missing BLUETOOTH_SCAN permission")
        }
    }

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            if (!scannedDevices.any { it.address == device.address }) {
                Log.d("BleViewModel", "Device found: ${device.address}")
                scannedDevices.add(device)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: List<android.bluetooth.le.ScanResult>) {
            results.forEach { result ->
                val device = result.device
                if (!scannedDevices.any { it.address == device.address }) {
                    Log.d("BleViewModel", "Batch device found: ${device.address}")
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
        Log.d("BleViewModel", "Connecting to device: ${device.address}")
        bleManager.connectToDevice(device, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromDevice() {
        Log.d("BleViewModel", "Disconnecting from device: ${connectedDevice.value?.address}")
        bleManager.disconnect()
        connectedDevice.value = null
        gattServices.value = emptyList()
        clearConnectedDevice()
    }

    @SuppressLint("MissingPermission")
    private fun saveConnectedDevice(device: BluetoothDevice) {
        Log.d("BleViewModel", "Saving connected device: ${device.address}")
        sharedPreferences.edit().apply {
            putString("device_address", device.address)
            apply()
        }
    }

    private fun clearConnectedDevice() {
        Log.d("BleViewModel", "Clearing saved connected device")
        sharedPreferences.edit().clear().apply()
    }

    fun autoConnectToSavedDevice() {
        val deviceAddress = sharedPreferences.getString("device_address", null)
        if (deviceAddress != null) {
            val bluetoothDevice = bleManager.getDeviceByAddress(deviceAddress)
            if (bluetoothDevice != null) {
                Log.d("BleViewModel", "Auto-connecting to saved device: $deviceAddress")
                if (hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                    if (gattCallback != null) {
                        bleManager.connectToDevice(bluetoothDevice, gattCallback)
                    } else {
                        Log.e("BleViewModel", "gattCallback is null, cannot connect to device")
                    }
                } else {
                    Log.e("BleViewModel", "Missing BLUETOOTH_CONNECT permission")
                }
            } else {
                Log.e("BleViewModel", "Saved device not found: $deviceAddress")
            }
        } else {
            Log.d("BleViewModel", "No saved device to auto-connect")
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED
    }
}