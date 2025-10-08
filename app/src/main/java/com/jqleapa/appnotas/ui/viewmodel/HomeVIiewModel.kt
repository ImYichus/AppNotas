package com.jqlqapa.appnotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jqlqapa.appnotas.data.NoteRepository
import com.jqlqapa.appnotas.data.model.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Necesitarás inyectar el NoteRepository.
class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    // 1. Estado para la Pestaña/Cejilla seleccionada (Notas o Tareas)
    private val _selectedTab = MutableStateFlow(NoteTab.NOTES)

    // 2. Flujo de datos del Repository (Ordenados según requisitos)
    private val notesFlow = repository.getAllNotes() // Ordenado por fecha de registro
    private val tasksFlow = repository.getAllTasks() // Ordenado por fecha de cumplimiento

    // 3. Estado Combinado que la UI va a observar
    val uiState: StateFlow<HomeUiState> = combine(
        _selectedTab,
        notesFlow,
        tasksFlow
    ) { tab, notes, tasks ->
        // Decide qué lista exponer en la UI
        val currentList = when (tab) {
            NoteTab.NOTES -> notes
            NoteTab.TASKS -> tasks
        }
        HomeUiState(
            currentList = currentList,
            selectedTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    // Función para cambiar la pestaña
    fun selectTab(tab: NoteTab) {
        _selectedTab.value = tab
    }

    fun toggleTaskCompletion(task: NoteEntity) {
        if (task.isTask) {
            viewModelScope.launch {
                val updatedTask = task.copy(isCompleted = !task.isCompleted)
                repository.saveNote(updatedTask)
            }
        }
    }
}

// Data class para modelar el estado de la UI
data class HomeUiState(
    val currentList: List<NoteEntity> = emptyList(),
    val selectedTab: NoteTab = NoteTab.NOTES
)

// Enum para las pestañas
enum class NoteTab {
    NOTES, TASKS
}