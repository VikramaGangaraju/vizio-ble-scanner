package com.vizio.viziobleoobe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vizio.viziobleoobe.ble.BleViewModel
import com.vizio.viziobleoobe.ui.screens.DeviceControlScreen
//import com.vizio.viziobleoobe.ui.screens.DeviceDetailScreen
import com.vizio.viziobleoobe.ui.screens.DeviceListScreen

@Composable
fun AppNavGraph(
    viewModel: BleViewModel,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Screen.DeviceList.route) {
        composable(Screen.DeviceList.route) {
            DeviceListScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.DeviceDetail.route) {
            DeviceControlScreen(viewModel = viewModel, navController = navController)
        }
    }
}