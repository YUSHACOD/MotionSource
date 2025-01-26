package com.example.motionsource.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A class that handles device rotation sensor data and provides azimuth (X), pitch (Y), and roll (Z) values.
 */
class DeviceRotationSensor(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _rotationValues = MutableStateFlow(Triple(0f, 0f, 0f))
    val rotationValues: StateFlow<Triple<Float, Float, Float>> get() = _rotationValues

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    val orientationAngles = FloatArray(3)

                    // Convert rotation vector to rotation matrix
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                    // Compute orientation angles (Azimuth, Pitch, Roll)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                    val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                    _rotationValues.value = Triple(azimuth, pitch, roll)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle accuracy changes if needed
        }
    }

    init {
        rotationVectorSensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Unregister the listener when it's no longer needed
    fun unregisterListener() {
        sensorManager.unregisterListener(sensorEventListener)
    }
}
