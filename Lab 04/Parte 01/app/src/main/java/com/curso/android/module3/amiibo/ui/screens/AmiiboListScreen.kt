package com.curso.android.module3.amiibo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.curso.android.module3.amiibo.R
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.domain.error.ErrorType
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboUiState
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * ============================================================================
 * AMIIBO LIST SCREEN
 * ============================================================================
 *
 * Pantalla principal que muestra la lista de Amiibos.
 *
 * Implementa:
 * - MVVM
 * - UiState
 * - Offline First
 * - Snackbar de error no bloqueante
 * - Paginación
 * - Manejo avanzado de errores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmiiboListScreen(
    onAmiiboClick: (String) -> Unit = {},
    viewModel: AmiiboViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pageSize by viewModel.pageSize.collectAsStateWithLifecycle()
    val hasMorePages by viewModel.hasMorePages.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val paginationError by viewModel.paginationError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPageSizeDropdown by remember { mutableStateOf(false) }

    /**
     * Snackbar reactivo para errores con cache disponible
     */
    LaunchedEffect(uiState) {
        if (uiState is AmiiboUiState.Error) {
            val state = uiState as AmiiboUiState.Error

            if (state.cachedAmiibos.isNotEmpty()) {
                val result = snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = "Reintentar",
                    withDismissAction = true
                )

                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.refreshAmiibos()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {

                    Box {
                        TextButton(
                            onClick = { showPageSizeDropdown = true }
                        ) {
                            Text(text = "Página: $pageSize")
                            Icon(Icons.Default.ArrowDropDown, null)
                        }

                        DropdownMenu(
                            expanded = showPageSizeDropdown,
                            onDismissRequest = { showPageSizeDropdown = false }
                        ) {
                            viewModel.pageSizeOptions.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size por página") },
                                    onClick = {
                                        viewModel.setPageSize(size)
                                        showPageSizeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.refreshAmiibos() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.retry)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        when (val state = uiState) {

            is AmiiboUiState.Loading -> {
                LoadingContent(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                )
            }

            is AmiiboUiState.Success -> {
                AmiiboGrid(
                    amiibos = state.amiibos,
                    onAmiiboClick = onAmiiboClick,
                    hasMorePages = hasMorePages,
                    isLoadingMore = isLoadingMore,
                    paginationError = paginationError,
                    onLoadMore = { viewModel.loadNextPage() },
                    onRetryLoadMore = { viewModel.retryLoadMore() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is AmiiboUiState.Error -> {
                if (state.cachedAmiibos.isNotEmpty()) {
                    AmiiboGrid(
                        amiibos = state.cachedAmiibos,
                        onAmiiboClick = onAmiiboClick,
                        hasMorePages = false,
                        isLoadingMore = false,
                        paginationError = null,
                        onLoadMore = {},
                        onRetryLoadMore = {},
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    ErrorContent(
                        message = state.message,
                        errorType = state.errorType,
                        isRetryable = state.isRetryable,
                        onRetry = { viewModel.refreshAmiibos() },
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * ============================================================================
 * GRID DE AMIIBOS
 * ============================================================================
 */
@Composable
fun AmiiboGrid(
    amiibos: List<AmiiboEntity>,
    onAmiiboClick: (String) -> Unit,
    hasMorePages: Boolean,
    isLoadingMore: Boolean,
    paginationError: String?,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp)
    ) {

        items(amiibos) { amiibo ->
            AmiiboCard(
                amiibo = amiibo,
                onClick = { onAmiiboClick(amiibo.id) }
            )
        }

        item {
            when {
                isLoadingMore -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                paginationError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(paginationError)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onRetryLoadMore) {
                            Text("Reintentar")
                        }
                    }
                }

                hasMorePages -> {
                    LaunchedEffect(Unit) { onLoadMore() }
                }
            }
        }
    }
}

/**
 * ============================================================================
 * TARJETA DE AMIIBO
 * ============================================================================
 */
@Composable
fun AmiiboCard(
    amiibo: AmiiboEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = amiibo.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = amiibo.gameSeries,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * ============================================================================
 * LOADING CONTENT
 * ============================================================================
 */
@Composable
fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * ============================================================================
 * ERROR CONTENT
 * ============================================================================
 */
@Composable
fun ErrorContent(
    message: String,
    errorType: ErrorType,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(12.dp))

            if (isRetryable) {
                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
    }
}
