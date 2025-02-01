package com.example.motionsource.sensors

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.motionsource.udpsender.UdpSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
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
                val message = String.format("Azimuth(X): %+3.6f Pitch(Y): %+3.6f Roll(Z): %+3.6f", azimuth, pitch, roll)
                udpSender.sendData(message)
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
