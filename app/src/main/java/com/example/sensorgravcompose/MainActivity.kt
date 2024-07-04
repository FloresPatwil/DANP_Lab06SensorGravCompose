package com.example.sensorgravcompose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
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
    val typeSensor = Sensor.TYPE_GAME_ROTATION_VECTOR; // TODO: AQUI SE CAMBIA EL TIPO DE SENSOR

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var orientation by remember { mutableStateOf(0f) }
    var fixedOrientation by remember { mutableStateOf<Float?>(null) }

    val sensorEventListener = remember {
        // Definición de un objeto anónimo que implementa la interfaz SensorEventListener
        object : SensorEventListener {
            // Método que se llama cuando hay un cambio en los datos del sensor
            override fun onSensorChanged(event: SensorEvent?) {
                // Verifica que el evento no sea nulo
                event?.let {
                    // Verifica que el vector de valores del evento tenga al menos 4 componentes
                    if (event.values.size >= 4) {
                        val rotationMatrix = FloatArray(9)
                        // Obtiene la matriz de rotación a partir del vector de rotación
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientationValues = FloatArray(3)
                        // Calcula los valores de orientación (azimuth, pitch, roll) a partir de la matriz de rotación
                        SensorManager.getOrientation(rotationMatrix, orientationValues)

                        // Convierte los valores de orientación de radianes a grados
                        val roll1 = Math.toDegrees(orientationValues[0].toDouble()).toFloat() // Azimuth (yaw)
                        val roll2 = Math.toDegrees(orientationValues[1].toDouble()).toFloat() // Pitch
                        val roll3 = Math.toDegrees(orientationValues[2].toDouble()).toFloat() // Roll

                        // Registra los valores de orientación en los logs para depuración
                        Log.d("VALOR1:", "" + roll1)
                        Log.d("VALOR2:", "" + roll2)
                        Log.d("VALOR3:", "" + roll3)

                        // Asigna el ángulo de rotación (yaw) a la variable 'orientation'
                        orientation = roll1 // Ángulo de giro (yaw)
                    }
                }
            }

            // Método que se llama cuando cambia la precisión del sensor (no utilizado aquí)
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(typeSensor),
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
