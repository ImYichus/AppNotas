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

enum class NoteTab {
    NOTES, TASKS
}

// ESTADO ACTUALIZADO para añadir la selección de detalle (selectedNoteId)
data class HomeUiState(
    val currentList: List<NoteEntity> = emptyList(),
    val selectedTab: NoteTab = NoteTab.NOTES,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedNoteId: Long? = null // AÑADIDO para el diseño Maestro-Detalle
)

// --- 2. VIEW MODEL IMPLEMENTACIÓN ---

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(NoteTab.NOTES)
    private val _selectedNoteId = MutableStateFlow<Long?>(null) // AÑADIDO

    // Usamos un único flujo con todas las notas/tareas
    private val allNotesFlow = repository.getAllNotes()

    // 3. Estado Combinado que la UI va a observar (ACTUALIZADO para incluir _selectedNoteId)
    val uiState: StateFlow<HomeUiState> = combine(
        allNotesFlow,
        _selectedTab,
        _selectedNoteId
    ) { allNotes, selectedTab, selectedId ->

        // 1. Filtrar la lista según la pestaña seleccionada
        val filteredList = when (selectedTab) {
            NoteTab.NOTES -> allNotes.filter { !it.isTask }
            NoteTab.TASKS -> allNotes.filter { it.isTask }
        }

        // 2. Validar el selectedId: si la nota seleccionada ya no existe en la lista, la deseleccionamos.
        val validSelectedId = if (filteredList.any { it.id == selectedId }) selectedId else null


        HomeUiState(
            currentList = filteredList,
            selectedTab = selectedTab,
            isLoading = false,
            selectedNoteId = validSelectedId // ENVIADO al estado
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    // Función para cambiar la pestaña
    fun selectTab(tab: NoteTab) {
        _selectedTab.value = tab
        clearSelection() // Opcional: limpiar la selección al cambiar de pestaña
    }

    // AÑADIDO: Función para seleccionar una nota (llamada desde HomeScreen)
    fun selectNote(noteId: Long) {
        _selectedNoteId.value = noteId
    }

    // AÑADIDO: Función para cerrar el panel de detalle (llamada desde HomeScreen)
    fun clearSelection() {
        _selectedNoteId.value = null
    }

    // Función para marcar una tarea como cumplida (se mantiene)
    fun toggleTaskCompletion(task: NoteEntity) {
        if (task.isTask) {
            viewModelScope.launch {
                val updatedTask = task.copy(isCompleted = !task.isCompleted)
                repository.saveNote(updatedTask)
            }
        }
    }

    // Función para eliminar una nota/tarea (se mantiene, con lógica de limpieza añadida)
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            if (_selectedNoteId.value == note.id) {
                clearSelection() // Limpiar selección si se elimina la nota de detalle
            }
        }
    }

    // Flujo de todos los archivos multimedia (se mantiene)
    val allMedia = repository.getAllMedia()

    // Función para añadir media (se mantiene)
    fun addMedia(media: MediaEntity) {
        viewModelScope.launch {
            repository.addMedia(media)
        }
    }
}