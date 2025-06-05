package com.vizio.viziobleoobe

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.vizio.viziobleoobe.ble.BleViewModel
import com.vizio.viziobleoobe.navigation.AppNavGraph
import com.vizio.viziobleoobe.util.PermissionsUtil

class MainActivity : ComponentActivity() {
    private val bleViewModel: BleViewModel by lazy {
        BleViewModel(application)
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the permission launcher before the LifecycleOwner reaches STARTED state
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle the result map: permission -> granted
        }

        setContent {
            val context = this
            val navController = rememberNavController()

            // Request permissions at startup if not granted
            LaunchedEffect(Unit) {
                if (!PermissionsUtil.hasPermissions(context)) {
                    PermissionsUtil.requestPermissions(context as Activity, permissionLauncher)
                }
            }

            // Launch your navigation graph
            AppNavGraph(navController = navController, viewModel = bleViewModel)
        }
    }
}