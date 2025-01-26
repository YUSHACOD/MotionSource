package com.example.motionsource.udpsender

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpSender(
    private val serverIp: String,
    private val serverPort: Int
) {
    private var socket: DatagramSocket? = null

    init {
        socket = DatagramSocket()
    }

    suspend fun sendData(message: String) {
        withContext(Dispatchers.IO) {
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
                e.printStackTrace()
            }
        }
    }

    fun close() {
        socket?.close()
    }
}
