package com.example.dado

// =============================================================================
// IMPORTACIONES
// =============================================================================

// --- Android Core ---
import android.os.Bundle
import android.util.Log

// --- AndroidX Activity ---
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// --- Jetpack Compose Core ---
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Kotlin Coroutines ---
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =============================================================================
// CONSTANTES
// =============================================================================

private const val TAG = "MainActivity"

private const val MIN_STAT = 1
private const val MAX_STAT = 20

private const val ANIMATION_ITERATIONS = 15
private const val ANIMATION_DELAY_MS = 80L

// Colores semánticos
private val GoldColor = Color(0xFFFFD700)
private val ErrorRed = Color(0xFFD32F2F)
private val PinkButton = Color(0xFFFF80AB)

// =============================================================================
// MAIN ACTIVITY
// =============================================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: RPG Character Sheet - Sofía Dávila")

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CharacterSheetScreen()
                }
            }
        }
    }
}

// =============================================================================
// COMPOSABLE PRINCIPAL: HOJA DE PERSONAJE
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetScreen() {

    // =========================================================================
    // STATE HOISTING (PASO 2)
    // =========================================================================

    var vit by rememberSaveable { mutableIntStateOf(10) }
    var dex by rememberSaveable { mutableIntStateOf(10) }
    var wis by rememberSaveable { mutableIntStateOf(10) }

    val total = vit + dex + wis

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RPG Character Creator by Sofía Dávila - 24008903",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // -----------------------------------------------------------------
            // STAT ROWS (PASO 1)
            // -----------------------------------------------------------------

            StatRow(
                name = "Vitality",
                value = vit,
                onRollFinished = { vit = it }
            )

            StatRow(
                name = "Dexterity",
                value = dex,
                onRollFinished = { dex = it }
            )

            StatRow(
                name = "Wisdom",
                value = wis,
                onRollFinished = { wis = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -----------------------------------------------------------------
            // TOTAL (PASO 3)
            // -----------------------------------------------------------------

            Text(
                text = "TOTAL: $total",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // -----------------------------------------------------------------
            // VALIDACIÓN
            // -----------------------------------------------------------------

            when {
                total < 30 -> {
                    Text(
                        text = "Re-roll recommended!",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                total >= 50 -> {
                    Text(
                        text = "Godlike!",
                        color = GoldColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =============================================================================
// COMPOSABLE REUTILIZABLE: FILA DE ESTADÍSTICA
// =============================================================================

@Composable
fun StatRow(
    name: String,
    value: Int,
    onRollFinished: (Int) -> Unit
) {

    var displayValue by remember { mutableIntStateOf(value) }
    var isRolling by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun rollStat() {
        coroutineScope.launch {
            isRolling = true

            repeat(ANIMATION_ITERATIONS) {
                displayValue = (MIN_STAT..MAX_STAT).random()
                delay(ANIMATION_DELAY_MS)
            }

            val finalValue = (MIN_STAT..MAX_STAT).random()
            displayValue = finalValue
            onRollFinished(finalValue)

            isRolling = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = displayValue.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRolling) Color.Gray else Color.Black
            )

            Button(
                onClick = { rollStat() },
                enabled = !isRolling,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PinkButton,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Roll"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Roll")
            }
        }
    }
}

// =============================================================================
// PREVIEW
// =============================================================================

@Preview(showBackground = true)
@Composable
fun CharacterSheetPreview() {
    MaterialTheme {
        CharacterSheetScreen()
    }
}
