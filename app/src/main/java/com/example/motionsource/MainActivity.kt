package com.example.motionsource

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun MotionSourceUi() {
    var ip by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("42069") }
    var isSending by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
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
                modifier = Modifier
                    .padding(all = 24.dp)
            )

            Row {
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier
                        .width(200.dp),
                    label = {
                        Text(
                            text = "Enter IP",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = { port = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = {
                        Text(
                            text = "42069",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    label = {
                        Text(
                            text = "Enter Port",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    isSending = !isSending
                    println("Button is Clicked bitch!!")
                          },
                shape = RoundedCornerShape(10.dp),
                enabled = ip.isNotEmpty() && port.isNotEmpty(),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface,
                    disabledContentColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .width(200.dp)
            ) {
                if (isSending) {
                    Text("Stop Server")
                } else {
                    Text("Start Server")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSending) {
                RotationDisplay(
                    context = LocalContext.current,
                    serverIp = ip,
                    serverPort = port.toInt()
                )
            }
        }
    }
}