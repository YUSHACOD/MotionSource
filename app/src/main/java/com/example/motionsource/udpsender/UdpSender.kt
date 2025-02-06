package com.example.motionsource.udpsender

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
            println("Socket creation crash: " + e.printStackTrace().toString())
        }
    }

    suspend fun sendData(buffer: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                val packet = DatagramPacket(
                    buffer,
                    buffer.size,
                    InetAddress.getByName(serverIp),
                    serverPort
                )
                socket?.send(packet)
            } catch (e: Exception) {
                println("Error sending data: ${e.printStackTrace()}")
                println("Ending Process")
                socket?.close()
                exitProcess(0)
            }
        }
    }

    fun close() {
        socket?.close()
    }
}
