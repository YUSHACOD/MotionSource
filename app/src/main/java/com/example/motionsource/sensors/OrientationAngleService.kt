package com.example.motionsource.sensors

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.motionsource.udpsender.UdpSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val channelId = "ForegroundServiceChannel"
    private lateinit var context: Context
    private lateinit var serverIp: String
    private var serverPort: Int = 0
    private lateinit var sensor: DeviceRotationSensor
    private lateinit var udpSender: UdpSender
    private var serviceJob: Job? = null
    private var isPaused = true
    private var isCreated = false

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
        } catch (e: Exception) {
            println("onCreate exception: " + e.message + e.stackTrace)
        }
        println("Service Created: onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                // Read IP and Port from Intent extras
                println("Action Start Called")
                this.serverIp = intent.getStringExtra(EXTRA_IP) ?: "0.0.0.0"
                this.serverPort = intent.getIntExtra(EXTRA_PORT, 42069)
                this.context = this
                isPaused = false
                startSendingData()
            }
            ACTION_PAUSE -> pauseSendingData()
            ACTION_RESUME -> resumeSendingData()
            ACTION_STOP -> {
                sensor.unregisterListener()
                serviceJob?.cancel()
                udpSender.close()
                stopSelf()
            }
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Foreground Service")
            .setContentText("Your service is running")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .build()

        try {
            startForeground(1, notification)
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

    private fun pauseSendingData() {
        isPaused = true
    }

    private fun resumeSendingData() {
        if (isPaused) {
            isPaused = false
        }
    }

    private suspend fun sendRotationData() {

        sensor.rotationValues.collect { (azimuth, pitch, roll) ->
            val message = String.format(Locale.US, "%+1.6f,%+1.6f,%+1.6f", azimuth, pitch, roll)

            udpSender.sendData(message)

            // Add a delay to control the frequency (optional)
            delay(100) // Send data every 100 milliseconds
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
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
        context.stopService(serviceIntent)
    } catch (e: Exception) {
        println("startForegroundService exception: " + e.message + e.stackTrace)
    }
}

fun pauseOrientationAngleService(context: Context) {
    val serviceIntent = Intent(context, OrientationAngleService::class.java).apply {
        this.action = OrientationAngleService.ACTION_PAUSE
    }

    context.stopService(serviceIntent)
    println("Action Pause Done")
}

fun resumeOrientationAngleService(context: Context) {
    val serviceIntent = Intent(context, OrientationAngleService::class.java).apply {
        this.action = OrientationAngleService.ACTION_RESUME
    }

    context.stopService(serviceIntent)
    println("Action resume Done")
}

fun stopOrientationAngleService(context: Context) {
    val serviceIntent = Intent(context, OrientationAngleService::class.java).apply {
        this.action = OrientationAngleService.ACTION_STOP
    }

    context.stopService(serviceIntent)
    println("Action Stop Done")
}
