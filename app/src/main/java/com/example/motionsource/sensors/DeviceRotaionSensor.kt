package com.example.motionsource.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Quaternion(val w: Float, val x: Float, val y: Float, val z: Float)

class DeviceRotationSensor(context: Context): SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var w = 0f
    private var x = 0f
    private var y = 0f
    private var z = 0f

    private val _rotationValues = MutableStateFlow(Quaternion(0f, 0f, 0f, 0f))
    val rotationValues: StateFlow<Quaternion> get() = _rotationValues

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val quaternionArray = FloatArray(4)


                // get Orientation Quaternion from rotation vector( it.values )
                SensorManager.getQuaternionFromVector(quaternionArray, it.values)

                w = quaternionArray[0]
                x = quaternionArray[1]
                y = quaternionArray[2]
                z = quaternionArray[3]

                _rotationValues.value = Quaternion(w, x, y, z)
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
        println("Accuracy changed: $accuracy")
    }

    init {
        rotationVectorSensor?.let {
            try {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            } catch (e: Exception) {
                println("[register listener: " + e.message + e.stackTrace)
            }
        }
    }

    fun test() {
        try {
            val s: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            println("[TESTING : " + s.toString())
        } catch (e: Exception) {
            println("test: " + e.message + e.stackTrace)
        }
        println("[AVAILABLE SENSORS:" + sensorManager.getSensorList(Sensor.TYPE_ALL))
        println("[AVAILABLE SENSORS DYNAMIC:" + sensorManager.getDynamicSensorList(Sensor.TYPE_ALL))
    }

    fun getOrientationValues(): Quaternion {
        return Quaternion(w, x, y, z)
    }

    // Unregister the listener when it's no longer needed
    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }
}
