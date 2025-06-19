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
import java.util.UUID

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
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BleViewModel", "Connected to device: ${gatt.device.address}")
                connectedDevice.value = gatt.device
                saveConnectedDevice(gatt.device)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BleViewModel", "Disconnected from device")
                connectedDevice.value = null
                gattServices.value = emptyList()
                clearConnectedDevice()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattServices.value = gatt.services
                gatt.services.forEach { service ->
                    Log.d("BleViewModel", "Service discovered: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.d("BleViewModel", "Characteristic discovered: ${characteristic.uuid}")
                    }
                }
                readAllCharacteristics()
            } else {
                Log.e("BleViewModel", "Service discovery failed with status: $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value?.let { String(it) } ?: "null"
                Log.d("BleViewModel", "Characteristic read: ${characteristic.uuid}, value: $value")
                _lastReadCharacteristicValue.value = value
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

    @SuppressLint("MissingPermission")
    private fun readAllCharacteristics() {
        val dpadValue = readCharacteristic(DPAD_INFO)
        if (dpadValue != null) {
            Log.d("BleViewModel", "DPAD_INFO Value: ${String(dpadValue)}")
        } else {
            Log.e("BleViewModel", "Failed to read DPAD_INFO")
        }

        val wifiPasswordValue = readCharacteristic(WIFI_PASSWORD)
        if (wifiPasswordValue != null) {
            Log.d("BleViewModel", "WIFI_PASSWORD Value: ${String(wifiPasswordValue)}")
        } else {
            Log.e("BleViewModel", "Failed to read WIFI_PASSWORD")
        }

        val tvStatusValue = readCharacteristic(TV_STATUS)
        if (tvStatusValue != null) {
            Log.d("BleViewModel", "TV_STATUS Value: ${String(tvStatusValue)}")
        } else {
            Log.e("BleViewModel", "Failed to read TV_STATUS")
        }
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic(uuid: UUID): ByteArray? {
        val gatt = bleManager.getConnectedGatt()
        if (gatt == null) {
            Log.e("BleViewModel", "BluetoothGatt is null. Cannot read characteristic.")
            return null
        }

        val service = gatt.getService(SERVICE_INFO)
        if (service == null) {
            Log.e("BleViewModel", "Service $SERVICE_INFO not found on device.")
            return null
        }

        val characteristic = service.getCharacteristic(uuid)
        if (characteristic == null) {
            Log.e("BleViewModel", "Characteristic $uuid not found in service $SERVICE_INFO.")
            return null
        }

        return if (gatt.readCharacteristic(characteristic)) {
            characteristic.value
        } else {
            Log.e("BleViewModel", "Failed to initiate read for characteristic $uuid.")
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(uuid: UUID, value: ByteArray): Boolean {
        val gatt = bleManager.getConnectedGatt()
        if (gatt == null) {
            Log.e("BleViewModel", "BluetoothGatt is null. Cannot write characteristic.")
            return false
        }

        val service = gatt.getService(SERVICE_INFO)
        if (service == null) {
            Log.e("BleViewModel", "Service $SERVICE_INFO not found on device.")
            return false
        }

        val characteristic = service.getCharacteristic(uuid)
        if (characteristic == null) {
            Log.e("BleViewModel", "Characteristic $uuid not found in service $SERVICE_INFO.")
            return false
        }

        characteristic.value = value
        return if (gatt.writeCharacteristic(characteristic)) {
            true
        } else {
            Log.e("BleViewModel", "Failed to write characteristic $uuid.")
            false
        }
    }

    fun writeDpadCommand(dpadSelection: BleTvDpadSelection): Boolean {
        return writeCharacteristic(DPAD_INFO, byteArrayOf(dpadSelection.value.toByte()))
    }

    fun startScan() {
        if (hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            bleManager.startScan(scanCallback)
        }
    }

    fun stopScan() {
        if (hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            bleManager.stopScan(scanCallback)
        }
    }

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            if (!device.name.isNullOrEmpty() && !scannedDevices.any { it.address == device.address }) {
                scannedDevices.add(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleViewModel", "Scan failed with error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        bleManager.connectToDevice(device, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromDevice() {
        bleManager.disconnect()
        connectedDevice.value = null
        gattServices.value = emptyList()
        clearConnectedDevice()
    }

    private fun saveConnectedDevice(device: BluetoothDevice) {
        sharedPreferences.edit().putString("device_address", device.address).apply()
    }

    private fun clearConnectedDevice() {
        sharedPreferences.edit().clear().apply()
    }

    fun autoConnectToSavedDevice() {
        val deviceAddress = sharedPreferences.getString("device_address", null)
        if (deviceAddress != null) {
            val bluetoothDevice = bleManager.getDeviceByAddress(deviceAddress)
            if (bluetoothDevice != null && hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                bleManager.connectToDevice(bluetoothDevice, gattCallback)
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val SERVICE_INFO: UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA100")
        val DPAD_INFO: UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA101")
        val WIFI_PASSWORD: UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA102")
        val TV_STATUS: UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA103")
    }
}


