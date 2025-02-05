package com.example.motionsource

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.motionsource.sensors.deviceRotationValues
import java.util.Locale

@Composable
fun RotationDisplay(
    context: Context,
    serverIp: String,
    serverPort: Int
) {
    val (azimuth, pitch, roll) = deviceRotationValues(context, serverIp, serverPort)

    Text(
        text = String.format(Locale.US, "X: %+3.6f\nY: %+3.6f\nZ: %+3.6f", azimuth, pitch, roll),
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
    )
}

