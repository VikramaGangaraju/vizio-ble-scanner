package com.vizio.viziobleoobe.ble

import java.util.UUID

// TV BLE OOBEs Service
val SERVICE_INFO            : UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA100")

// TV BLE OOBEs Characteristics
val DPAD_INFO                : UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA101")
val WIFI_PASSWORD            : UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA102")
val TV_STATUS                : UUID = UUID.fromString("ea1979cf-5313-4152-a056-37619c1dA103")

object BleCharacteristicsUtil {
    private val supportedUUIDs = setOf(
        DPAD_INFO,
        WIFI_PASSWORD,
        TV_STATUS
    )

    fun isCharacteristicSupported(uuid: UUID): Boolean {
        return supportedUUIDs.contains(uuid)
    }
}

