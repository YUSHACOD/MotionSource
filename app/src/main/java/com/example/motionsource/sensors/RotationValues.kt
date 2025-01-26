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
    val rotationValues = sensor.rotationValues.value

    DisposableEffect(Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        val job = coroutineScope.launch {
            while (true) {
                val (azimuth, pitch, roll) = rotationValues
                val message = "Azimuth: $azimuth, Pitch: $pitch, Roll: $roll"
                udpSender.sendData(message)
                kotlinx.coroutines.delay(100) // Send every 100ms
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
