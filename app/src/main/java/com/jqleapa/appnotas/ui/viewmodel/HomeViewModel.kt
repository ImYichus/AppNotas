package com.jqlqapa.appnotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jqlqapa.appnotas.data.NoteRepository
import com.jqlqapa.appnotas.data.model.MediaEntity
import com.jqlqapa.appnotas.data.model.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- 1. ENUM Y DATA CLASSES PARA EL ESTADO ---

// Enum para manejar las pestañas de la UI
enum class NoteTab {
    NOTES, TASKS
}

// Estado que la Home/UI observará
data class HomeUiState(
    val currentList: List<NoteEntity> = emptyList(),
    val selectedTab: NoteTab = NoteTab.NOTES,
    val isLoading: Boolean = false,
    val error: String? = null
)

// --- 2. VIEW MODEL IMPLEMENTACIÓN ---

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    // 1. Estado para la Pestaña/Cejilla seleccionada
    private val _selectedTab = MutableStateFlow(NoteTab.NOTES)

    // 2. Flujo de datos del Repository
    private val notesFlow = repository.getAllNotes()
    private val tasksFlow = repository.getAllTasks()

    // 3. Estado Combinado que la UI va a observar
    val uiState: StateFlow<HomeUiState> = combine(
        _selectedTab,
        notesFlow,
        tasksFlow
    ) { tab, notes, tasks ->
        // Decide qué lista exponer basándose en la pestaña seleccionada
        val currentList = when (tab) {
            NoteTab.NOTES -> notes
            NoteTab.TASKS -> tasks
        }
        HomeUiState(
            currentList = currentList,
            selectedTab = tab,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        // Mantener activo por 5 segundos después de que el último observador desaparezca
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true) // Estado inicial de carga
    )

    // Función para cambiar la pestaña (llamada desde la UI)
    fun selectTab(tab: NoteTab) {
        _selectedTab.value = tab
    }

    // Función para marcar una tarea como cumplida (Requisito)
    fun toggleTaskCompletion(task: NoteEntity) {
        // Solo aplica la lógica si realmente es una tarea
        if (task.isTask) {
            viewModelScope.launch {
                // Crea una copia de la tarea con el estado de completado invertido
                val updatedTask = task.copy(isCompleted = !task.isCompleted)
                repository.saveNote(updatedTask)
            }
        }
    }

    // Función para eliminar una nota/tarea
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // CORRECCIÓN 1: Cambiado de propiedad a función (repository.allMedia -> repository.getAllMedia())
    val allMedia = repository.getAllMedia()

    // CORRECCIÓN 2: Cambiado el nombre de la función (repository.insertMedia -> repository.addMedia)
    fun addMedia(media: MediaEntity) {
        viewModelScope.launch {
            repository.addMedia(media)
        }
    }
}
