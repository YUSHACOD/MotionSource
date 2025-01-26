package com.example.motionsource

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.motionsource.ui.theme.MotionSourceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotionSourceTheme {
                MotionSourceUi()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MotionSourcePreview() {
    MotionSourceTheme {
        MotionSourceUi()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
        color = Color.White
    )
}

/*
@Composable
fun DropdownMenuExample(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            Text(text = selectedItem.ifEmpty { "Select IP Address" })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
 */

/*
fun getAvailableIpAddresses(): List<String> {
    val ipAddresses = mutableListOf<String>()
    try {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()

            // Check if the interface is up, not a loopback, and not a virtual interface
            if (networkInterface.isUp && !networkInterface.isLoopback && !networkInterface.isVirtual) {
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()

                    // Check if the address is not a loopback and is an IPv4 address
                    if (!inetAddress.isLoopbackAddress && inetAddress.address.size == 4) {
                        val hostAddress = inetAddress.hostAddress
                        if (hostAddress != null) {
                            ipAddresses.add(hostAddress)
                        }
                    }
                }
            }
        }
    } catch (e: SocketException) {
        Log.e("getAvailableIpAddresses", "Error getting network interfaces: ${e.message}", e)
    } catch (e: SecurityException) {
        Log.e("getAvailableIpAddresses", "Security error getting IP addresses: ${e.message}", e)
    }
    return ipAddresses.distinct()
}
 */

@Composable
fun ButtonStateSwitch(isSending: Boolean) {
    if (isSending) {
        Text("Stop Sending Data")
    } else {
        Text("Start Sending Data")
    }
}

@Composable
fun MotionSourceUi() {
    //var selectedIp by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    //val udpSender: UdpSender? = null
    //val availableIps = getAvailableIpAddresses()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Greeting(
                name = stringResource(R.string.user_name),
                modifier = Modifier.padding(all = 24.dp)
            )

            /*
            Text("Select IP Address:")
            DropdownMenuExample(
                items = availableIps,
                selectedItem = selectedIp,
                onItemSelected = { selectedIp = it }
            )
             */

            Text("Enter Ip:")
            BasicTextField(
                value = ip,
                onValueChange = { ip= it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Enter Port:")
            BasicTextField(
                value = port,
                onValueChange = { port = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isSending = !isSending
                    println("Button is Clicked bitch!!")
                          },
                //enabled = selectedIp.isNotEmpty() && port.isNotEmpty()
                enabled = ip.isNotEmpty() && port.isNotEmpty()
            ) {
                ButtonStateSwitch(isSending)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSending) {
                RotationDisplay(
                    context = LocalContext.current,
                    //serverIp = selectedIp,
                    serverIp = ip,
                    serverPort = port.toInt()
                )
            }



            /*
            if (isSending) {
                LaunchedEffect(Unit) {
                    udpSender = UdpSender(ip, port.toInt())
                    withContext(Dispatchers.IO) {
                        try {
                            while (isSending) {
                                udpSender!!.sendData("hello from motion source")
                                println("Sending data")
                                delay(50) // Sending data every second
                            }
                        } catch (e: Exception) {
                            println("Error: ${e.message}")
                        } finally {
                            udpSender?.close()
                        }
                    }
                }
            } else {
                udpSender?.close()
            }
            */
        }
    }
}