package com.example.motionsource.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.motionsource.R
import com.example.motionsource.sensors.DeviceRotationSensor
import com.example.motionsource.udpsender.UdpSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

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
//    private var isConnected = false

    override fun onCreate() {
        if (!isCreated) {
            super.onCreate()
            try {
            } catch (e: Exception) {
                println("onCreate exception: " + e.message + e.stackTrace)
            }
            createNotificationChannel()
            startForeground(1, buildNotification())
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
//                connectToPeer()
                sendRotationData()
            }
        }
    }

//    private suspend fun connectToPeer() {
//        val message = "connect"
//        while (!isConnected) {
//            udpSender.sendData(message.toByteArray())
//            println(message.toByteArray().toString())
//            if ("connected" == udpSender.receiveData()) {
//                isConnected = true
//            }
//        }
//    }

    private suspend fun sendRotationData() {
        val buffer = ByteBuffer.allocate(12)
        while (true) {
            if (!isPaused) {
                val (azimuth, pitch, roll) = sensor.getOrientationValues()

                buffer.clear()
                buffer.putFloat(azimuth)
                buffer.putFloat(pitch)
                buffer.putFloat(roll)

                udpSender.sendData(buffer.array())

                delay(1)
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "ORIENTATION_SERVICE_CHANNEL")
            .setContentTitle("Motion Source")
            .setContentText("Orientation service is running")
            .setSmallIcon(R.mipmap.motion_source_ico)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "ORIENTATION_SERVICE_CHANNEL",
            "Orientation service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
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
