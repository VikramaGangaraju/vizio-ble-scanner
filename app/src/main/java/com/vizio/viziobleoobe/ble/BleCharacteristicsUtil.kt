package com.vizio.viziobleoobe.ble

import java.util.UUID

object BleCharacteristicsUtil {
    private val supportedUUIDs = setOf(
        BleViewModel.DPAD_INFO,
        BleViewModel.WIFI_PASSWORD,
        BleViewModel.TV_STATUS
    )

    fun isCharacteristicSupported(uuid: UUID): Boolean {
        return supportedUUIDs.contains(uuid)
    }
}