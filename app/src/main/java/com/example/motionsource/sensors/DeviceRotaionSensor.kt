package com.example.motionsource.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DeviceRotationSensor(context: Context): SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var azimuth = 0f
    private var pitch = 0f
    private var roll = 0f

    private val _rotationValues = MutableStateFlow(Triple(0f, 0f, 0f))
    val rotationValues: StateFlow<Triple<Float, Float, Float>> get() = _rotationValues

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                val orientationAngles = FloatArray(3)

                // Convert rotation vector to rotation matrix
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                // Compute orientation angles (Azimuth, Pitch, Roll)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                azimuth = orientationAngles[0]
                pitch = orientationAngles[1]
                roll = orientationAngles[2]

                _rotationValues.value = Triple(azimuth, pitch, roll)
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    init {
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun getOrientationValues(): Triple<Float, Float, Float> {
        return Triple(azimuth, pitch, roll)
    }

    // Unregister the listener when it's no longer needed
    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }
}
