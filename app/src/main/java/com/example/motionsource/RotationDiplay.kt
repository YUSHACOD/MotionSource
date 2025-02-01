package com.example.motionsource

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.motionsource.sensors.deviceRotationValues

/**
 * A composable that displays the current rotation values (Azimuth, Pitch, Roll).
 */
@SuppressLint("DefaultLocale")
@Composable
fun RotationDisplay(
    context: Context,
    serverIp: String,
    serverPort: Int
) {
    val (azimuth, pitch, roll) = deviceRotationValues(context, serverIp, serverPort)

    Text(
        text = String.format("Azimuth(X)\t:\t%+3.6f\nPitch(Y)\t:\t%+3.6f\nRoll(Z)\t:\t%+3.6f", azimuth, pitch, roll),
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
    )
}

