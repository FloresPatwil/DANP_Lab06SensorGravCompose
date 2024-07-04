package com.example.sensorgravcompose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensorBasedTriangleScreen()
        }
    }
}

@Composable
fun SensorBasedTriangleScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var orientation by remember { mutableStateOf(0f) }
    var fixedOrientation by remember { mutableStateOf<Float?>(null) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_ORIENTATION) {
                        orientation = it.values[0] // Yaw angle
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangleEquilateral(fixedOrientation ?: orientation)
            Button(
                onClick = { fixedOrientation = if (fixedOrientation == null) orientation else null },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(text = if (fixedOrientation == null) "Fijar" else "Liberar")
            }
        }
    }
}

@Composable
fun TriangleEquilateral(
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = this.center
        val radius = min(size.width, size.height) / 3

        rotate(-rotation, Offset(center.x, center.y)) {
            drawTriangleEquilateral(center, radius)
        }
    }
}

fun DrawScope.drawTriangleEquilateral(
    center: Offset,
    radius: Float
) {
    val a = center + Offset(-radius, radius * sqrt(3f) / 3)
    val b = center + Offset(radius, radius * sqrt(3f) / 3)
    val c = center + Offset(0f, -2 * radius * sqrt(3f) / 3)

    drawPath(
        path = Path().apply {
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            close()
        },
        color = Color.Blue
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    var orientation by remember { mutableStateOf(10f) }
    var fixedOrientation by remember { mutableStateOf<Float?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            TriangleEquilateral(fixedOrientation ?: orientation)
            Button(
                onClick = { fixedOrientation = if (fixedOrientation == null) orientation else null },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(text = if (fixedOrientation == null) "Fijar" else "Liberar")
            }
        }
    }
}
