package com.vizio.viziobleoobe.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.util.Log

class BleManager(private val context: Context) {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    fun startScan(scanCallback: ScanCallback) {
        Log.d("BleManager", "Starting scan")
        scanner.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan(scanCallback: ScanCallback) {
        Log.d("BleManager", "Stopping scan")
        scanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(
        device: BluetoothDevice,
        gattCallback: BluetoothGattCallback
    ) {
        if (gattCallback == null) {
            Log.e("BleManager", "GattCallback is null. Cannot connect to device.")
            return
        }
        Log.d("BleManager", "Connecting to device: ${device.name} - ${device.address}")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        Log.d("BleManager", "Disconnecting from device")
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    fun getDeviceByAddress(address: String): BluetoothDevice? {
        Log.d("BleManager", "Getting device by address: $address")
        return bluetoothAdapter.getRemoteDevice(address)
    }

    fun getConnectedGatt(): BluetoothGatt? {
        Log.d("BleManager", "Getting connected GATT instance")
        return bluetoothGatt
    }
}