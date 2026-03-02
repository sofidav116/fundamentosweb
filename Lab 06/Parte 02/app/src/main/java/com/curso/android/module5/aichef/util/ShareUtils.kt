package com.curso.android.module5.aichef.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * =============================================================================
 * ShareUtils - Utilidades para compartir recetas como imagen
 * =============================================================================
 *
 * CONCEPTO: Compartir archivos en Android
 * Android no permite que las apps compartan archivos directamente.
 * El flujo correcto es:
 * 1. Guardar el archivo en caché de la app
 * 2. Generar una URI segura con FileProvider
 * 3. Lanzar el Intent con la URI
 * 4. Android muestra el selector de apps nativo
 *
 * CONCEPTO: FileProvider
 * FileProvider es un ContentProvider especial que genera URIs
 * content:// en lugar de file://, lo cual es requerido desde Android 7.0.
 *
 * =============================================================================
 */
object ShareUtils {

    /**
     * Crea un bitmap con la información de la receta para compartir
     *
     * CONCEPTO: Canvas y Paint
     * Dibujamos manualmente sobre un Bitmap usando Canvas y Paint,
     * similar a como se dibuja en una vista personalizada.
     *
     * @param recipeTitle Título de la receta
     * @param ingredients Lista de ingredientes
     * @param dishImageBitmap Imagen del plato generada por IA (opcional)
     * @return Bitmap con la tarjeta de receta lista para compartir
     */
    suspend fun createShareableBitmap(
        recipeTitle: String,
        ingredients: List<String>,
        dishImageBitmap: Bitmap?
    ): Bitmap = withContext(Dispatchers.Default) {

        // Dimensiones de la tarjeta
        val width = 1080
        val imageHeight = if (dishImageBitmap != null) 600 else 0
        val headerHeight = 160
        val ingredientLineHeight = 50
        val ingredientsHeight = (ingredients.take(6).size * ingredientLineHeight) + 120
        val footerHeight = 80
        val totalHeight = headerHeight + imageHeight + ingredientsHeight + footerHeight

        // Crear bitmap base
        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // =====================================================================
        // FONDO
        // =====================================================================
        val bgPaint = Paint().apply { color = Color.parseColor("#1A1A2E") }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // =====================================================================
        // FRANJA SUPERIOR DE COLOR
        // =====================================================================
        val accentPaint = Paint().apply { color = Color.parseColor("#E91E8C") }
        canvas.drawRect(0f, 0f, width.toFloat(), 12f, accentPaint)

        var currentY = 12f

        // =====================================================================
        // TÍTULO DE LA RECETA
        // =====================================================================
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        currentY += 90f
        canvas.drawText(
            if (recipeTitle.length > 30) recipeTitle.take(27) + "..." else recipeTitle,
            40f, currentY, titlePaint
        )

        // Subtítulo "AI Chef"
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#E91E8C")
            textSize = 32f
            isAntiAlias = true
        }
        currentY += 50f
        canvas.drawText("✨ Generado con AI Chef", 40f, currentY, subtitlePaint)
        currentY += 20f

        // =====================================================================
        // IMAGEN DEL PLATO (si existe)
        // =====================================================================
        if (dishImageBitmap != null) {
            val scaledImage = Bitmap.createScaledBitmap(dishImageBitmap, width, imageHeight, true)
            canvas.drawBitmap(scaledImage, 0f, currentY, null)
            currentY += imageHeight.toFloat()
        }

        // =====================================================================
        // SECCIÓN DE INGREDIENTES
        // =====================================================================
        currentY += 40f
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#BB86FC")
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("INGREDIENTES", 40f, currentY, sectionPaint)
        currentY += 20f

        // Línea separadora
        val linePaint = Paint().apply {
            color = Color.parseColor("#BB86FC")
            strokeWidth = 2f
        }
        canvas.drawLine(40f, currentY, width - 40f, currentY, linePaint)
        currentY += 40f

        // Lista de ingredientes
        val ingredientPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            isAntiAlias = true
        }
        ingredients.take(6).forEach { ingredient ->
            canvas.drawText(
                "• ${if (ingredient.length > 45) ingredient.take(42) + "..." else ingredient}",
                40f, currentY, ingredientPaint
            )
            currentY += ingredientLineHeight.toFloat()
        }
        if (ingredients.size > 6) {
            canvas.drawText("• y ${ingredients.size - 6} ingredientes más...", 40f, currentY, ingredientPaint)
        }

        // =====================================================================
        // FOOTER
        // =====================================================================
        val footerPaint = Paint().apply {
            color = Color.parseColor("#888888")
            textSize = 28f
            isAntiAlias = true
        }
        canvas.drawText("AI Chef - Recetas generadas con Gemini AI", 40f, totalHeight - 30f, footerPaint)

        bitmap
    }

    /**
     * Guarda el bitmap en caché y devuelve una URI compartible
     *
     * CONCEPTO: Cache Directory
     * getCacheDir() devuelve el directorio de caché privado de la app.
     * Los archivos aquí se eliminan automáticamente cuando el sistema
     * necesita espacio, ideal para archivos temporales.
     *
     * @param context Contexto de la app
     * @param bitmap Bitmap a guardar
     * @return URI segura para compartir con FileProvider
     */
    suspend fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri =
        withContext(Dispatchers.IO) {
            // Crear directorio de imágenes en caché
            val imagesDir = File(context.cacheDir, "images")
            imagesDir.mkdirs()

            // Crear archivo temporal en formato JPEG para mayor compatibilidad
            val imageFile = File(imagesDir, "recipe_share_${System.currentTimeMillis()}.jpg")

            // Guardar bitmap como JPEG
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            // Generar URI segura con FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        }

    /**
     * Lanza el selector nativo de Android para compartir una imagen
     *
     * CONCEPTO: Intent.ACTION_SEND
     * Este intent lanza el selector de apps nativo de Android,
     * mostrando todas las apps que pueden recibir una imagen:
     * WhatsApp, Telegram, Gmail, Instagram, etc.
     *
     * CONCEPTO: FLAG_GRANT_READ_URI_PERMISSION
     * Le da permiso temporal a la app receptora para leer el archivo
     * a través de la URI de FileProvider.
     *
     * @param context Contexto de la app
     * @param imageUri URI de la imagen a compartir
     * @param recipeTitle Título para el texto del share
     */
    fun launchShareIntent(context: Context, imageUri: Uri, recipeTitle: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "¡Mira esta receta que generé con AI Chef: $recipeTitle! 🍽️✨")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        // Dar permisos explícitos a TODAS las apps que puedan recibir el intent
        val chooser = Intent.createChooser(shareIntent, "Compartir receta via...")
        val resInfoList = context.packageManager.queryIntentActivities(chooser, 0)
        resInfoList.forEach { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }

        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Lanza el selector compartiendo imagen del plato + texto con la receta completa
     *
     * CONCEPTO: Compartir imagen real del plato generado por IA
     * En lugar de una tarjeta dibujada, compartimos directamente la imagen
     * generada por Gemini junto con el texto completo de la receta.
     *
     * @param context Contexto de la app
     * @param imageUri URI de la imagen del plato a compartir
     * @param text Texto completo de la receta
     */
    fun launchShareIntentWithText(context: Context, imageUri: Uri, text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        // Dar permisos explícitos a todas las apps receptoras
        val chooser = Intent.createChooser(shareIntent, "Compartir receta via...")
        val resInfoList = context.packageManager.queryIntentActivities(chooser, 0)
        resInfoList.forEach { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Lanza el selector compartiendo solo texto (cuando no hay imagen disponible)
     *
     * @param context Contexto de la app
     * @param text Texto completo de la receta
     */
    fun launchShareTextOnly(context: Context, text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir receta via...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Limpia archivos temporales de imágenes compartidas
     *
     * CONCEPTO: Limpieza de Archivos Temporales
     * Aunque Android limpia el caché automáticamente, es buena práctica
     * limpiar manualmente después de compartir.
     *
     * @param context Contexto de la app
     */
    suspend fun cleanupSharedImages(context: Context) = withContext(Dispatchers.IO) {
        val imagesDir = File(context.cacheDir, "images")
        imagesDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("recipe_share_")) {
                file.delete()
            }
        }
    }
}