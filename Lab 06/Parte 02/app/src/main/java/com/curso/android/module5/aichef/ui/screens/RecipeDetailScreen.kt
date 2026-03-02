package com.curso.android.module5.aichef.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.curso.android.module5.aichef.domain.model.UiState
import com.curso.android.module5.aichef.ui.viewmodel.ChefViewModel
import com.curso.android.module5.aichef.util.ShareUtils
import kotlinx.coroutines.launch

/**
 * =============================================================================
 * RecipeDetailScreen - Pantalla de detalle de receta con imagen generada por IA
 * =============================================================================
 *
 * CONCEPTO: Generación de Imágenes con IA y Cache
 * Esta pantalla demuestra el uso de Firebase AI Logic para generar imágenes
 * del plato terminado usando el modelo Gemini con capacidades de generación
 * de imágenes (gemini-3-pro-image-preview).
 *
 * CONCEPTO: Cache de Imágenes con Firebase Storage
 * Para evitar consumir cuota de API en cada visualización, implementamos
 * un sistema de cache:
 * 1. Primera visita: Genera imagen con IA, sube a Storage, guarda URL en Firestore
 * 2. Visitas posteriores: Usa la URL guardada y Coil para cargar eficientemente
 *
 * FLUJO DE CACHE:
 * ┌─────────────┐     ┌──────────────────┐     ┌─────────────┐
 * │ ¿Tiene URL? │─Sí─▶│ Cargar con Coil  │────▶│   Mostrar   │
 * └──────┬──────┘     └──────────────────┘     └─────────────┘
 *        │No
 *        ▼
 * ┌─────────────┐     ┌──────────────────┐     ┌─────────────┐
 * │ Generar IA  │────▶│ Subir a Storage  │────▶│ Guardar URL │
 * └─────────────┘     └──────────────────┘     └─────────────┘
 *
 * ARQUITECTURA DE LA PANTALLA:
 * ┌─────────────────────────────────────────┐
 * │            TopAppBar                     │
 * │  ← Volver          Título de Receta  ♥  │
 * ├─────────────────────────────────────────┤
 * │     ┌─────────────────────────────┐     │
 * │     │   Imagen Generada por IA    │     │
 * │     │   (cache en Storage)        │     │
 * │     └─────────────────────────────┘     │
 * │     ┌─────────────────────────────┐     │
 * │     │      INGREDIENTES           │     │
 * │     │  ✓ Ingrediente 1            │     │
 * │     └─────────────────────────────┘     │
 * │     ┌─────────────────────────────┐     │
 * │     │      PREPARACIÓN            │     │
 * │     │  1. Paso uno                │     │
 * │     └─────────────────────────────┘     │
 * └─────────────────────────────────────────┘
 *
 * CONCEPTOS CLAVE DEMOSTRADOS:
 *
 * 1. LaunchedEffect para Side Effects:
 *    - Dispara la verificación de cache o generación de imagen
 *    - Se ejecuta una vez por receta (key = recipe)
 *
 * 2. Manejo de Estados UI:
 *    - Idle: Esperando iniciar
 *    - Loading: Generando/cargando imagen
 *    - Success: URL lista, Coil carga la imagen
 *    - Error: Fallo con opción de reintentar
 *
 * 3. Coil AsyncImage para carga de URLs:
 *    - Carga asíncrona desde URL de Firebase Storage
 *    - Cache automático en memoria y disco
 *    - Manejo de estados de carga internos
 *
 * 4. Scroll vertical con rememberScrollState:
 *    - Permite scroll cuando el contenido excede la pantalla
 *    - Mantiene posición durante recomposiciones
 *
 * =============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: ChefViewModel,
    recipeId: String,
    onNavigateBack: () -> Unit
) {
    // =========================================================================
    // OBSERVACIÓN DE ESTADOS
    // =========================================================================
    // Obtenemos la receta buscándola en la lista del ViewModel
    // Esto evita una llamada adicional a Firestore
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val recipe = recipes.find { it.id == recipeId }

    // Estado de la generación de imagen
    val imageState by viewModel.imageGenerationState.collectAsStateWithLifecycle()

    // Context y scope para compartir
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado de carga del share
    var isSharing by remember { mutableStateOf(false) }

    // =========================================================================
    // SIDE EFFECT: Verificar Cache o Generar Imagen
    // =========================================================================
    // LaunchedEffect se ejecuta cuando 'recipe' cambia
    // Esto verifica si existe imagen cacheada o genera una nueva
    //
    // CONCEPTO: LaunchedEffect
    // - Se ejecuta en un CoroutineScope ligado al ciclo de vida del composable
    // - Se cancela automáticamente si el composable sale de composición
    // - La key (recipe) determina cuándo re-ejecutar el efecto
    //
    // CONCEPTO: Cache-First Strategy
    // - Si recipe.generatedImageUrl no está vacía, se usa directamente
    // - Si está vacía, se genera con IA, sube a Storage, y guarda URL
    LaunchedEffect(recipe) {
        recipe?.let {
            viewModel.generateRecipeImage(
                recipeId = it.id,
                existingImageUrl = it.generatedImageUrl,
                recipeTitle = it.title,
                ingredients = it.ingredients
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "Detalle de Receta") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Limpiamos el estado de imagen al salir para evitar
                        // mostrar la imagen anterior en la próxima receta
                        viewModel.clearImageState()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // =========================================================
                    // ÍCONO DE FAVORITO EN EL TOPAPPBAR
                    // =========================================================
                    // Muestra corazón relleno si es favorito, delineado si no.
                    // Al pulsar llama a toggleFavorite con UI optimista.
                    recipe?.let {
                        IconButton(onClick = { viewModel.toggleFavorite(it) }) {
                            Icon(
                                imageVector = if (it.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (it.isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                                tint = if (it.isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // =========================================================
                    // BOTÓN COMPARTIR
                    // =========================================================
                    // Comparte la imagen del plato generada por IA junto con
                    // el texto completo de la receta (título, ingredientes,
                    // pasos y link de la foto).
                    IconButton(
                        onClick = {
                            recipe?.let { currentRecipe ->
                                scope.launch {
                                    isSharing = true
                                    try {
                                        // Obtener URL de la imagen - primero del estado, luego de la receta guardada
                                        val imageUrl = when {
                                            imageState is UiState.Success -> (imageState as UiState.Success).data
                                            currentRecipe.generatedImageUrl.isNotBlank() -> currentRecipe.generatedImageUrl
                                            else -> ""
                                        }

                                        // Construir texto completo de la receta
                                        val ingredientesTexto = currentRecipe.ingredients.joinToString("\n") { "• $it" }
                                        val pasosTexto = currentRecipe.steps.mapIndexed { i, step -> "${i + 1}. $step" }.joinToString("\n")
                                        val fotoTexto = if (imageUrl.isNotBlank()) "\n\n🖼️ FOTO DEL PLATO:\n$imageUrl" else ""

                                        val shareText = """
🍳 RECETA: ${currentRecipe.title}

🥗 INGREDIENTES:
$ingredientesTexto

👨‍🍳 PREPARACIÓN:
$pasosTexto$fotoTexto

Compartido desde AI Chef 🚀
                                        """.trimIndent()

                                        // Si hay imagen del plato, compartir imagen + texto
                                        if (imageUrl.isNotBlank()) {
                                            val request = ImageRequest.Builder(context)
                                                .data(imageUrl)
                                                .build()
                                            val result = context.imageLoader.execute(request)
                                            val dishBitmap = if (result is SuccessResult) {
                                                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                                            } else null

                                            if (dishBitmap != null) {
                                                // Guardar imagen del plato en caché y compartir
                                                val imageUri = ShareUtils.saveBitmapToCache(context, dishBitmap)
                                                ShareUtils.launchShareIntentWithText(context, imageUri, shareText)
                                            } else {
                                                // No se pudo cargar la imagen, compartir solo texto
                                                ShareUtils.launchShareTextOnly(context, shareText)
                                            }
                                        } else {
                                            // Sin imagen, compartir solo texto
                                            ShareUtils.launchShareTextOnly(context, shareText)
                                        }

                                    } finally {
                                        isSharing = false
                                    }
                                }
                            }
                        },
                        enabled = !isSharing
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir receta",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (recipe == null) {
            // Estado de error: receta no encontrada
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Receta no encontrada")
            }
        } else {
            // ================================================================
            // CONTENIDO SCROLLEABLE
            // ================================================================
            // verticalScroll permite scroll cuando el contenido es largo
            // rememberScrollState mantiene la posición durante recomposiciones
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Sección de imagen generada por IA con cache
                RecipeImageSection(
                    imageState = imageState,
                    recipeTitle = recipe.title,
                    onRetry = {
                        viewModel.generateRecipeImage(
                            recipeId = recipe.id,
                            existingImageUrl = "", // Forzar regeneración
                            recipeTitle = recipe.title,
                            ingredients = recipe.ingredients
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de ingredientes
                IngredientsSection(ingredients = recipe.ingredients)

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de pasos de preparación
                StepsSection(steps = recipe.steps)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * =============================================================================
 * RecipeImageSection - Sección de imagen generada con IA y cache
 * =============================================================================
 *
 * CONCEPTO: Manejo de Estados Asíncronos en UI
 * Esta sección demuestra cómo manejar los diferentes estados de una
 * operación asíncrona (generación/carga de imagen) en la UI:
 *
 * - Idle: Estado inicial, esperando acción
 * - Loading: Generando imagen con IA o cargando desde cache
 * - Success: URL disponible, Coil carga la imagen
 * - Error: Fallo con opción de reintentar
 *
 * CONCEPTO: Sealed Class para Estados
 * UiState<T> es una sealed class que garantiza manejo exhaustivo
 * de todos los estados posibles con 'when'.
 *
 * CONCEPTO: Coil AsyncImage
 * AsyncImage es un composable de Coil que:
 * - Carga imágenes de forma asíncrona desde URLs
 * - Implementa cache en memoria y disco automáticamente
 * - Maneja estados de carga/error internamente
 * - Soporta placeholders y transformaciones
 *
 * =============================================================================
 */
@Composable
private fun RecipeImageSection(
    imageState: UiState<String>,
    recipeTitle: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        // ====================================================================
        // ASPECT RATIO
        // ====================================================================
        // aspectRatio(16f / 9f) mantiene proporción 16:9 (widescreen)
        // Esto asegura que la imagen siempre tenga la misma forma
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            contentAlignment = Alignment.Center
        ) {
            // ================================================================
            // MANEJO EXHAUSTIVO DE ESTADOS
            // ================================================================
            // 'when' con sealed class garantiza que manejamos todos los casos
            when (imageState) {
                is UiState.Idle -> {
                    // Estado inicial - esperando que inicie la generación
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Preparando imagen...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is UiState.Loading -> {
                    // Estado de carga - la IA está generando la imagen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Icono de "magia" para indicar IA
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = imageState.message ?: "Generando imagen con IA...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    // ========================================================
                    // ÉXITO: Cargar imagen desde URL con Coil
                    // ========================================================
                    // AsyncImage de Coil carga imágenes desde URLs de forma
                    // asíncrona con cache automático en memoria y disco.
                    // La URL puede ser de Firebase Storage (cache) o cualquier
                    // otra fuente compatible.
                    //
                    // CONCEPTO: ContentScale.Crop
                    // Recorta la imagen para llenar completamente el espacio
                    // disponible, manteniendo la proporción del aspecto
                    AsyncImage(
                        model = imageState.data,
                        contentDescription = "Imagen de $recipeTitle",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                is UiState.Error -> {
                    // Estado de error - mostrar mensaje y botón de reintentar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = imageState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * =============================================================================
 * IngredientsSection - Sección de ingredientes
 * =============================================================================
 *
 * CONCEPTO: Composición de Listas
 * En Compose, para listas pequeñas (< 20 items) podemos usar forEach
 * directamente en un Column. Para listas grandes, usar LazyColumn.
 *
 * Esta sección itera sobre los ingredientes y los muestra con un
 * ícono de check para indicar que son items de una lista.
 *
 * =============================================================================
 */
@Composable
private fun IngredientsSection(ingredients: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de la sección
            Text(
                text = "Ingredientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de ingredientes con íconos
            ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * =============================================================================
 * StepsSection - Sección de pasos de preparación
 * =============================================================================
 *
 * CONCEPTO: forEachIndexed para Listas Numeradas
 * Cuando necesitamos el índice además del elemento, usamos forEachIndexed.
 * Esto nos permite numerar los pasos automáticamente.
 *
 * CONCEPTO: Badges Circulares
 * El número del paso se muestra en un Card circular usando:
 * - shape = RoundedCornerShape(50) para hacerlo circular
 * - padding simétrico para mantener forma
 *
 * =============================================================================
 */
@Composable
private fun StepsSection(steps: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de la sección
            Text(
                text = "Preparación",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista numerada de pasos
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Badge circular con número del paso
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(50) // Forma circular
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Texto del paso
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}