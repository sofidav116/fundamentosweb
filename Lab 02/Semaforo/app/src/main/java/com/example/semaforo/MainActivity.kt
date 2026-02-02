package com.example.semaforo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------
// ENUM
// ---------------------------------------------------------------------
enum class Light { Red, Yellow, Green }

// ---------------------------------------------------------------------
// MAIN ACTIVITY
// ---------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrafficLightScreen()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// UI PRINCIPAL
// ---------------------------------------------------------------------
@Composable
fun TrafficLightScreen() {

    var currentLight by remember { mutableStateOf(Light.Red) }

    LaunchedEffect(Unit) {
        while (true) {
            currentLight = Light.Red
            delay(2000)

            currentLight = Light.Green
            delay(2000)

            currentLight = Light.Yellow
            delay(1000)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Semáforo",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // CONTENEDOR SEMÁFORO + PALO
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // CABEZA DEL SEMÁFORO
                Box(
                    modifier = Modifier
                        .shadow(16.dp, RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF2C2C2C), Color.Black)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .width(200.dp)
                        .height(460.dp)
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TrafficCircle(Light.Red, currentLight)
                        TrafficCircle(Light.Yellow, currentLight)
                        TrafficCircle(Light.Green, currentLight)
                    }
                }

                // PALO DEL SEMÁFORO
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(220.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color.DarkGray, Color.Black)
                            ),
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                        )
                )

                // BASE
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// CÍRCULO CON PROFUNDIDAD
// ---------------------------------------------------------------------
@Composable
fun TrafficCircle(light: Light, current: Light) {

    val active = light == current

    val baseColor = when (light) {
        Light.Red -> Color(0xFFD32F2F)
        Light.Yellow -> Color(0xFFFBC02D)
        Light.Green -> Color(0xFF388E3C)
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .shadow(if (active) 20.dp else 6.dp, CircleShape)
            .clip(CircleShape)
            .background(
                if (active)
                    Brush.radialGradient(
                        colors = listOf(Color.White, baseColor),
                        radius = 180f
                    )
                else
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF555555), Color(0xFF222222)),
                        radius = 180f
                    )
            )
    )
}

// ---------------------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun TrafficLightPreview() {
    MaterialTheme {
        TrafficLightScreen()
    }
}
