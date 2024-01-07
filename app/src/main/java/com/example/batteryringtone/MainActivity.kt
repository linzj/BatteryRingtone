package com.example.batteryringtone

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.batteryringtone.ui.theme.BatteryRingtoneTheme

class MainActivity : ComponentActivity() {

    companion object {
        val permissionsRequired = arrayOf(
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.FOREGROUND_SERVICE_CAMERA,
            android.Manifest.permission.CAMERA
        )
    }

    private val requestPermissions =
        this.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle permission requests results
            // See the permission example in the Android platform samples: https://github.com/android/platform-samples
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                startBatteryService()
            } else {
                permissions.forEach { (permission, granted) ->
                    if (!granted) {
                        Toast.makeText(
                            this, "You shall grant the permission: $permission", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    // Somewhere in your code when you need to start the service
    private fun checkAndStartBatteryService() {
        when {
            permissionsRequired.all { permission ->
                ContextCompat.checkSelfPermission(
                    this, permission
                ) == PackageManager.PERMISSION_GRANTED
            } -> {
                // Permission is already granted, start the service
                startBatteryService()
            }

            else -> {
                // Permission is not granted, request it
                requestPermissions.launch(
                    permissionsRequired
                )
            }
        }
    }

    private fun startBatteryService() {
        val serviceIntent = Intent(this, BatteryService::class.java)
        // You might want to add extras or flags to the intent
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BatteryRingtoneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkAndStartBatteryService();
        } else {
            val serviceIntent = Intent(this, BatteryService::class.java)
            startService(serviceIntent)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BatteryRingtoneTheme {
        Greeting("Android")
    }
}