package com.vizio.viziobleoobe.navigation

sealed class Screen(val route: String) {
    object DeviceList : Screen("device_list")
    object DeviceDetail : Screen("device_detail")
}