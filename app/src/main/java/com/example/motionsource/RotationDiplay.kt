package com.example.motionsource

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.motionsource.sensors.deviceRotationValues

/**
 * A composable that displays the current rotation values (Azimuth, Pitch, Roll).
 */
@Composable
fun RotationDisplay(
    context: Context,
    serverIp: String,
    serverPort: Int
) {
    if (context == null) {
        Text(
            text = "Preview context",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
        return
    }

    val (azimuth, pitch, roll) = deviceRotationValues(context, serverIp, serverPort)

    Text(
        text = "Azimuth (X): $azimuth\nPitch (Y): $pitch\nRoll (Z): $roll",
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
    )
}

