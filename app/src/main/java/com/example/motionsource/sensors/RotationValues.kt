package com.example.motionsource.sensors

import android.content.Context
import androidx.compose.runtime.*
import com.example.motionsource.udpsender.UdpSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun deviceRotationValues(
    context: Context,
    serverIp: String,
    serverPort: Int
): Triple<Float, Float, Float> {
    val sensor = remember { DeviceRotationSensor(context) }
    val udpSender = remember { UdpSender(serverIp, serverPort) }
    val rotationValues by sensor.rotationValues.collectAsState() // Dynamically observe changes

    DisposableEffect(Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        val job = coroutineScope.launch {
            sensor.rotationValues.collect { (azimuth, pitch, roll) ->
                val message = "Azimuth(x): $azimuth, Pitch(y): $pitch, Roll(z): $roll"
                udpSender.sendData(message)
                kotlinx.coroutines.delay(50) // Send every 100ms
            }
        }

        onDispose {
            job.cancel()
            sensor.unregisterListener()
            udpSender.close()
        }
    }

    return rotationValues
}
