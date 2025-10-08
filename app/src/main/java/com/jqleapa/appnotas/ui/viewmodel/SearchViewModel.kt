package com.jqleapa.appnotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jqlqapa.appnotas.data.NoteRepository
import com.jqlqapa.appnotas.data.model.NoteEntity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// --- 1. DATA CLASS PARA EL ESTADO ---

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false
)

// --- 2. IMPLEMENTACIÓN DEL VIEW MODEL ---

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: NoteRepository) : ViewModel() {

    // Estado interno para el texto de búsqueda ingresado por el usuario
    private val _searchQuery = MutableStateFlow("")

    // Estado que la UI observará
    val uiState: StateFlow<SearchUiState> = _searchQuery
        // Aplica debounce para evitar consultas excesivas a la base de datos con cada pulsación de tecla
        .debounce(300L)
        // Cuando el query cambia, ejecuta la búsqueda en el repositorio
        .flatMapLatest { query ->
            // Si el query está vacío o solo tiene espacios, retorna un flujo vacío para no buscar
            if (query.isBlank()) {
                MutableStateFlow(emptyList())
            } else {
                // Llama al método del repositorio que busca en título y descripción
                repository.searchNotesAndTasks(query)
            }
        }
        // Combina el flujo de resultados con el query y otros estados
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        // Mapea los resultados reactivos al UiState final
        .combine(_searchQuery) { results, query ->
            SearchUiState(
                searchQuery = query,
                searchResults = results,
                isLoading = false, // La búsqueda es rápida (Room), no necesita estado de carga visible
                isSearching = query.isNotBlank() // Indica si el usuario ha iniciado la búsqueda
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState(isLoading = true) // Estado inicial
        )

    // Función llamada desde la UI cuando el usuario escribe en el campo de búsqueda
    fun updateSearchQuery(newQuery: String) {
        _searchQuery.update { newQuery }
    }
}