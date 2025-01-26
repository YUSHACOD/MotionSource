package com.example.motionsource.udpsender

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.system.exitProcess

class UdpSender(
    private val serverIp: String,
    private val serverPort: Int
) {
    private var socket: DatagramSocket? = null

    init {
        try {
            socket = DatagramSocket()
        } catch (e: java.net.SocketException) {
            println("hey bitch what the fuck is this socket crash: " + e.printStackTrace().toString())
        }
    }

    /*suspend*/ fun sendData(message: String) {
        //withContext(Dispatchers.IO) {
            try {
                val buffer = message.toByteArray()
                val packet = DatagramPacket(
                    buffer,
                    buffer.size,
                    InetAddress.getByName(serverIp),
                    serverPort
                )
                socket?.send(packet)
            } catch (e: Exception) {
                Log.e("UdpSender", "Error sending data: ${e.printStackTrace().toString()}")
                socket?.close()
                exitProcess(0)
            }
        //}
    }

    fun close() {
        socket?.close()
    }
}
