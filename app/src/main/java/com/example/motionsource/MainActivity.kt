package com.example.motionsource

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.motionsource.sensors.DeviceRotationSensor
import com.example.motionsource.services.pauseOrientationAngleService
import com.example.motionsource.services.resumeOrientationAngleService
import com.example.motionsource.services.startOrientationAngleService
import com.example.motionsource.services.stopOrientationAngleService
import com.example.motionsource.ui.theme.MotionSourceTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    1.0f,
                    0.0f
                )
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 750L
                zoomX.doOnEnd { screen.remove() }

                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_Y,
                    1.0f,
                    0.0f
                )
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 750L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }


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
fun Title(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier,
        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
        fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun RotationDisplay(context: Context) {
    val sensor = remember { DeviceRotationSensor(context) }
    val rotationValues by sensor.rotationValues.collectAsState()
    val (azimuth, pitch, roll) = rotationValues

    Text(
        text = String.format(Locale.US, "X\t:\t%+3.6f\nY\t:\t%+3.6f\nZ\t:\t%+3.6f", azimuth, pitch, roll),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun ObserveAppLifecycle(owner: LifecycleOwner, onAppExit: () -> Unit) {
    val lifecycle = owner.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // App is going to the background; stop service if needed
                onAppExit()
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun MotionSourceUi() {
    var ip by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("") }
    var isServicePaused by rememberSaveable { mutableStateOf(true) }
    var isServiceCreated by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    ObserveAppLifecycle(owner = context as LifecycleOwner) { stopOrientationAngleService(context) }

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
            Title(
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
                        .width(200.dp)
                        .padding(start = 20.dp),
                    label = {
                        Text(
                            text = "Enter IP",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(end = 20.dp),
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
                    if (!isServiceCreated) {
                        startOrientationAngleService(context, ip, port)
                        isServiceCreated = true
                        println("Service Created After")
                    }

                    if (isServicePaused) {
                        resumeOrientationAngleService(context)
                        isServicePaused = false
                    } else {
                        pauseOrientationAngleService(context)
                        isServicePaused = true
                    }
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
                if (isServicePaused) {
                    Text("Start Server")
                } else {
                    Text("Stop Server")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isServicePaused) {
                RotationDisplay(context)
            }
        }
    }
}