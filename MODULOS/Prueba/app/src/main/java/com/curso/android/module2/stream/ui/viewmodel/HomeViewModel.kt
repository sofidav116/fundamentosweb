package com.curso.android.module2.stream.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.curso.android.module2.stream.data.model.Category
import com.curso.android.module2.stream.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ================================================================================
 * HOME VIEW MODEL - Lógica de Presentación
 * ================================================================================
 *
 * PATRÓN MVVM (Model-View-ViewModel)
 * ----------------------------------
 * MVVM separa la aplicación en tres capas:
 *
 * 1. MODEL (data/):
 *    - Datos y lógica de negocio
 *    - Repository, Data Sources, Entities
 *    - No conoce la UI
 *
 * 2. VIEW (ui/screens/):
 *    - Composables que renderizan la UI
 *    - Observa el estado del ViewModel
 *    - Envía eventos de usuario al ViewModel
 *    - NO contiene lógica de negocio
 *
 * 3. VIEWMODEL (ui/viewmodel/):
 *    - Puente entre Model y View
 *    - Expone estado observable (StateFlow)
 *    - Procesa eventos de la UI
 *    - Sobrevive cambios de configuración (rotación)
 *
 * FLUJO DE DATOS (UDF - Unidirectional Data Flow):
 * ------------------------------------------------
 *
 *     ┌─────────────────────────────────────────────┐
 *     │                                             │
 *     │    ┌──────────┐    State    ┌──────────┐   │
 *     │    │ViewModel │ ──────────▶ │   View   │   │
 *     │    └──────────┘             └──────────┘   │
 *     │         ▲                        │         │
 *     │         │       Events           │         │
 *     │         └────────────────────────┘         │
 *     │                                             │
 *     └─────────────────────────────────────────────┘
 *
 * - STATE fluye del ViewModel a la View (UI observa StateFlow)
 * - EVENTS fluyen de la View al ViewModel (clicks, inputs, etc.)
 * - NUNCA al revés: la View no modifica el estado directamente
 *
 * BENEFICIOS DE UDF:
 * 1. Predecibilidad: El estado solo cambia desde el ViewModel
 * 2. Debugging: Fácil rastrear cambios de estado
 * 3. Testing: Puedes verificar estados sin UI
 * 4. Compose: Se integra perfectamente con recomposición
 */

/**
 * Estado de la pantalla Home.
 */
sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(
        val categories: List<Category>
    ) : HomeUiState

    data class Error(
        val message: String
    ) : HomeUiState
}

/**
 * ViewModel para la pantalla Home.
 */
class HomeViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    /**
     * STATE HOLDER (MutableStateFlow)
     */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /**
     * EXPOSED STATE (StateFlow)
     */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Inicialización del ViewModel.
     */
    init {
        loadCategories()
    }

    /**
     * Carga las categorías desde el repositorio.
     */
    private fun loadCategories() {
        _uiState.value = HomeUiState.Loading

        val categories = repository.getCategories()

        _uiState.value = HomeUiState.Success(categories)
    }

    /**
     * Recarga los datos.
     */
    fun refresh() {
        loadCategories()
    }

    /**
     * Alterna el estado de favorito de una canción.
     *
     * EVENT FLOW:
     * UI (click corazón)
     *   → ViewModel (toggleFavorite)
     *   → Estado actualizado
     *   → UI se recompone automáticamente
     *
     * @param songId ID de la canción a modificar
     */
    fun toggleFavorite(songId: String) {
        val currentState = _uiState.value

        if (currentState is HomeUiState.Success) {
            val updatedCategories = currentState.categories.map { category ->
                category.copy(
                    songs = category.songs.map { song ->
                        if (song.id == songId) {
                            song.copy(isFavorite = song.isFavorite.not())
                        } else {
                            song
                        }
                    }
                )
            }

            _uiState.value = HomeUiState.Success(updatedCategories)
        }
    }
}
