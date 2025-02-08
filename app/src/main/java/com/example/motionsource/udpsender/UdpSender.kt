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
            socket?.soTimeout = 100
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

//    suspend fun receiveData(): String {
//        val packet = DatagramPacket(ByteArray(1024), 1024)
//        withContext(Dispatchers.IO) {
//            try {
//                socket?.receive(packet)
//            } catch (e: Exception) {
//                println("Error receiving data: ${e.printStackTrace()}")
//            }
//        }
//        return  packet.data.toString()
//    }

    fun close() {
        socket?.close()
    }
}
