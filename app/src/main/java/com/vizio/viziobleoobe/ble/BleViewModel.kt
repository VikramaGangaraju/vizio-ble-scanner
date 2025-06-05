package com.vizio.viziobleoobe.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
class BleViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application.applicationContext)
    val scannedDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevice = mutableStateOf<BluetoothDevice?>(null)
    val gattServices = mutableStateOf<List<BluetoothGattService>>(emptyList())
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!device.name.isNullOrEmpty() && !scannedDevices.any { it.address == device.address }) {
                scannedDevices.add(device)
            }
        }
    }
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BleViewModel", "Connected to device: ${gatt.device.name} - ${gatt.device.address}")
                connectedDevice.value = gatt.device
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BleViewModel", "Disconnected from device: ${gatt.device.name} - ${gatt.device.address}")
                connectedDevice.value = null
                gattServices.value = emptyList()
            }
        }
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            Log.d("BleViewModel", "Services discovered for device: ${gatt.device.name} - ${gatt.device.address}")
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
                Log.d("BleViewModel", "Characteristic read: ${characteristic.uuid} - ${characteristic.value?.contentToString()}")
            } else {
                Log.e("BleViewModel", "Failed to read characteristic: ${characteristic.uuid}, status: $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleViewModel", "Characteristic written: ${characteristic.uuid} - ${characteristic.value?.contentToString()}")
            } else {
                Log.e("BleViewModel", "Failed to write characteristic: ${characteristic.uuid}, status: $status")
            }
        }


    }
    fun startScan() {
        bleManager.startScan(scanCallback)
    }
    fun stopScan() {
        bleManager.stopScan(scanCallback)
    }
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        Log.d("BleViewModel", "Connecting to device: ${device.name} - ${device.address}")
        bleManager.connectToDevice(device, gattCallback)
        stopScan()
    }
    @SuppressLint("MissingPermission")
    fun disconnectFromDevice() {
        Log.d("BleViewModel", "Disconnecting from device: ${connectedDevice.value?.name} - ${connectedDevice.value?.address}")
        bleManager.disconnect()
        connectedDevice.value = null
        gattServices.value = emptyList()
    }
}