package com.example.motionsource.sensors

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.motionsource.udpsender.UdpSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class OrientationAngleService: Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_IP = "EXTRA_IP"
        const val EXTRA_PORT = "EXTRA_PORT"
    }

    private lateinit var context: Context
    private lateinit var serverIp: String
    private var serverPort: Int = 0
    private lateinit var sensor: DeviceRotationSensor
    private lateinit var udpSender: UdpSender
    private var serviceJob: Job? = null
    private var isPaused = true
    private var isCreated = false

    override fun onCreate() {
        if (!isCreated) {
            super.onCreate()
            try {
            } catch (e: Exception) {
                println("onCreate exception: " + e.message + e.stackTrace)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!isCreated) {
                    println("Action Start Called")
                    this.serverIp = intent.getStringExtra(EXTRA_IP) ?: "0.0.0.0"
                    this.serverPort = intent.getIntExtra(EXTRA_PORT, 42069)
                    this.context = this
                    isPaused = false
                }
            }
            ACTION_PAUSE -> {
                println("Action Pause")
                isPaused = true
            }
            ACTION_RESUME -> {
                println("Action Resume")
                isPaused = false
            }
            ACTION_STOP -> {
                sensor.unregisterListener()
                serviceJob?.cancel()
                udpSender.close()
                stopSelf()
                println("Action stop done")
            }
        }

        try {
            startSendingData()
        } catch (e: Exception) {
            println("Starting Foreground : " + e.message + e.stackTrace)
        }

        return START_STICKY
    }

    private fun startSendingData() {
        if (!isCreated) {
            try {
                sensor = DeviceRotationSensor(context)
                udpSender = UdpSender(serverIp, serverPort)
            } catch (e: Exception) {
                println("sensor/udp message : " + e.message + e.stackTrace)
            }
            isCreated = true
            println("Service Created")
        }

        if (serviceJob == null) {
            serviceJob = CoroutineScope(Dispatchers.IO).launch {
                sendRotationData()
            }
        }
    }

    private suspend fun sendRotationData() {
        sensor.rotationValues.collect { (azimuth, pitch, roll) ->
            val message = String.format(Locale.US, "%+1.6f,%+1.6f,%+1.6f", azimuth, pitch, roll)

            if (!isPaused) {
                udpSender.sendData(message)
            }

            // Add a delay to control the frequency (optional)
            // delay(10)
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

fun startOrientationAngleService(context: Context, ip: String, port: String) {
    println("Inside start service function")
        val serviceIntent = Intent(context, OrientationAngleService::class.java).apply {
            try {
                this.action = OrientationAngleService.ACTION_START
                putExtra(OrientationAngleService.EXTRA_IP, ip)
                putExtra(OrientationAngleService.EXTRA_PORT, port.toInt())
            } catch (e: Exception) {
                println("serviceIntent exception: " + e.message + e.stackTrace)
            }
        }

    try {
        context.startService(serviceIntent)
    } catch (e: Exception) {
        println("startForegroundService exception: " + e.message + e.stackTrace)
    }
}

fun pauseOrientationAngleService(context: Context) {
    val serviceIntent = Intent(context, OrientationAngleService::class.java).apply {
        this.action = OrientationAngleService.ACTION_PAUSE
    }

    context.startService(serviceIntent)
}

fun resumeOrientationAngleService(context: Context) {
    val serviceIntent = Intent(context, OrientationAngleService::class.java).apply {
        this.action = OrientationAngleService.ACTION_RESUME
    }

    context.startService(serviceIntent)
}

fun stopOrientationAngleService(context: Context) {
    val serviceIntent = Intent(context, OrientationAngleService::class.java).apply {
        this.action = OrientationAngleService.ACTION_STOP
    }

    context.startService(serviceIntent)
}
