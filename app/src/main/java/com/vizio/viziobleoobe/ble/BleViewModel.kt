package com.vizio.viziobleoobe.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
class BleViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application.applicationContext)
    val scannedDevices = mutableStateListOf<BluetoothDevice>()
    val connectedDevice = mutableStateOf<BluetoothDevice?>(null)
    val gattServices = mutableStateOf<List<BluetoothGattService>>(emptyList())
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!scannedDevices.any { it.address == device.address }) {
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
                connectedDevice.value = gatt.device
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice.value = null
                gattServices.value = emptyList()
            }
        }
        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattServices.value = gatt.services
            }
        }
    }
    fun startScan() {
        bleManager.startScan(scanCallback)
    }
    fun stopScan() {
        bleManager.stopScan(scanCallback)
    }
    fun connectToDevice(device: BluetoothDevice) {
        bleManager.connectToDevice(device, gattCallback)
        stopScan()
    }
    fun disconnectFromDevice() {
        bleManager.disconnect()
        connectedDevice.value = null
        gattServices.value = emptyList()
    }
}