package com.jqleapa.appnotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.jqlqapa.appnotas.data.NoteRepository
import com.jqlqapa.appnotas.data.model.NoteEntity
import com.jqlqapa.appnotas.data.model.MediaEntity
import com.jqlqapa.appnotas.data.model.ReminderEntity
import kotlinx.coroutines.flow.asStateFlow

// --- Data Classes (UiState) ---

data class AddEditUiState(
    val noteId: Long? = null,
    val title: String = "",
    val description: String = "",
    val isTask: Boolean = false,
    val isCompleted: Boolean = false,
    val taskDueDate: Long? = null,
    val mediaFiles: List<MediaItem> = emptyList(),
    val reminders: List<ReminderItem> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccessful: Boolean = false,
    val error: String? = null
)

data class ReminderItem(
    val id: Long = 0L, // ID del recordatorio
    val timeInMillis: Long,
    val description: String = ""
)

data class MediaItem(
    val id: Long = 0L, // CORRECCIÓN CLAVE: Agregado el ID para MediaItem
    val uri: String,
    val description: String = "",
    val mediaType: String
)

// --- ViewModel ---

class AddEditNoteViewModel(
    private val repository: NoteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    // --------------------------------------------------------------------------------
    // --- LÓGICA DE CARGA (Mapeo corregido) ---
    // --------------------------------------------------------------------------------

    fun loadNote(id: Long) {
        if (_uiState.value.noteId == id && id != 0L) return

        viewModelScope.launch {
            try {
                val noteDetails = repository.getNoteDetails(id).first()

                _uiState.update { currentState ->
                    currentState.copy(
                        noteId = id,
                        title = noteDetails.note.title,
                        description = noteDetails.note.description,
                        isTask = noteDetails.note.isTask,
                        isCompleted = noteDetails.note.isCompleted,
                        taskDueDate = noteDetails.note.taskDueDate,

                        reminders = noteDetails.reminders.map { reminderEntity ->
                            ReminderItem(
                                id = reminderEntity.id,
                                timeInMillis = reminderEntity.reminderDateTime
                            )
                        },

                        mediaFiles = noteDetails.media.map { mediaEntity ->
                            MediaItem(
                                id = mediaEntity.id, //  Mapeo del ID de la entidad
                                uri = mediaEntity.filePath,
                                description = mediaEntity.description ?: "",
                                mediaType = mediaEntity.mediaType ?: "UNKNOWN"
                            )
                        }
                    )
                }

            } catch (e: Exception) {
                println("Error al cargar la nota $id: ${e.message}")
                _uiState.update { it.copy(error = "Error al cargar la nota: ${e.message}") }
            }
        }
    }


    // --------------------------------------------------------------------------------
    // --- LÓGICA DE GUARDADO (Constructores corregidos) ---
    // --------------------------------------------------------------------------------

    fun saveNote() {
        _uiState.update { it.copy(isSaving = true, error = null) }
        val current = _uiState.value

        if (current.title.isBlank()) {
            _uiState.update { it.copy(isSaving = false, error = "El título no puede estar vacío.") }
            return
        }

        viewModelScope.launch {
            try {
                val noteEntity = NoteEntity(
                    id = current.noteId ?: 0L,
                    title = current.title,
                    description = current.description,
                    isTask = current.isTask,
                    isCompleted = current.isCompleted,
                    taskDueDate = current.taskDueDate,
                    registrationDate = System.currentTimeMillis()
                )

                val resultingId = repository.saveNote(noteEntity)

                if (current.noteId == null || current.noteId == 0L) {
                    _uiState.update { it.copy(noteId = resultingId) }
                }

                // Guardar Medios. Solo inserta los que tienen ID = 0 (nuevos).
                current.mediaFiles.filter { it.id == 0L }.forEach { mediaItem ->
                    val mediaEntity = MediaEntity(
                        id = 0,
                        noteId = resultingId,
                        filePath = mediaItem.uri,
                        mediaType = mediaItem.mediaType,
                        description = mediaItem.description,
                        thumbnailPath = mediaItem.uri
                    )
                    repository.addMedia(mediaEntity)
                }

                // Guardar Recordatorios. Solo inserta los que tienen ID = 0 (nuevos).
                current.reminders.filter { it.id == 0L }.forEach { reminderItem ->
                    val reminderEntity = ReminderEntity(
                        id = 0,
                        noteId = resultingId,
                        reminderDateTime = reminderItem.timeInMillis
                    )
                    repository.addReminder(reminderEntity)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccessful = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }

    // --------------------------------------------------------------------------------
    // --- LÓGICA DE ELIMINACIÓN DE ELEMENTOS ---
    // --------------------------------------------------------------------------------

    fun deleteNote() {
        val noteId = _uiState.value.noteId
        if (noteId != null && noteId != 0L) {
            val noteToDelete = NoteEntity(
                id = noteId,
                title = _uiState.value.title,
                description = _uiState.value.description,
                isTask = _uiState.value.isTask,
                isCompleted = _uiState.value.isCompleted,
                taskDueDate = _uiState.value.taskDueDate,
                registrationDate = 0L
            )

            viewModelScope.launch {
                try {
                    repository.deleteNote(noteToDelete)
                    _uiState.update { AddEditUiState(saveSuccessful = true) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Error al eliminar: ${e.message}") }
                }
            }
        }
    }

    /**
     * Elimina un archivo adjunto del estado local (UI) y de la base de datos si ya existe.
     */
    fun deleteMedia(mediaItem: MediaItem) {
        _uiState.update { currentState ->
            currentState.copy(
                mediaFiles = currentState.mediaFiles.filter { it.id != mediaItem.id }
            )
        }

        // Si el elemento ya estaba en la DB (id != 0), lo eliminamos de la DB.
        if (mediaItem.id != 0L) {
            viewModelScope.launch {
                try {
                    // Nota: Esto requiere que el repositorio tenga un método para eliminar MediaEntity
                    val mediaToDelete = MediaEntity(
                        id = mediaItem.id,
                        noteId = _uiState.value.noteId ?: 0L,
                        filePath = mediaItem.uri,
                        mediaType = mediaItem.mediaType,
                        description = mediaItem.description
                    )
                    repository.deleteMedia(mediaToDelete)
                } catch (e: Exception) {
                    println("Error al eliminar media de DB: ${e.message}")
                    // No se actualiza el estado de error para no interrumpir la edición,
                    // ya que el elemento se eliminó del estado local.
                }
            }
        }
    }


    // --- Métodos de Interacción con la UI (Se mantienen) ---
    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun updateIsTask(isTask: Boolean) {
        _uiState.update { it.copy(isTask = isTask) }
    }

    fun toggleCompletion(isCompleted: Boolean) {
        _uiState.update { it.copy(isCompleted = isCompleted) }
    }

    fun updateTaskDueDate(time: Long?) {
        _uiState.update { it.copy(taskDueDate = time) }
    }

    fun saveComplete() {
        _uiState.update { it.copy(saveSuccessful = false) }
    }

    fun addReminder() {
        val newReminder = ReminderItem(
            id = 0L, // 0L indica que es nuevo
            timeInMillis = System.currentTimeMillis() + 3600000
        )
        _uiState.update {
            it.copy(reminders = it.reminders + newReminder)
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        _uiState.update {
            it.copy(reminders = it.reminders.filter { r -> r.id != reminder.id })
        }
        // Lógica de eliminación de DB si reminder.id != 0L. (similar a deleteMedia)
        if (reminder.id != 0L) {
            viewModelScope.launch {
                try {
                    val reminderToDelete = ReminderEntity(
                        id = reminder.id,
                        noteId = _uiState.value.noteId ?: 0L,
                        reminderDateTime = reminder.timeInMillis
                    )
                    repository.deleteReminder(reminderToDelete)
                } catch (e: Exception) {
                    println("Error al eliminar recordatorio de DB: ${e.message}")
                }
            }
        }
    }

    // --------------------------------------------------------------------------------
    // --- Companion Object y Factory ---
    // --------------------------------------------------------------------------------
    companion object {
        class AddEditViewModelFactory(
            private val noteRepository: NoteRepository,
            private val editId: Long? = null
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AddEditNoteViewModel::class.java)) {
                    val viewModel = AddEditNoteViewModel(noteRepository)
                    if (editId != null && editId != 0L) {
                        viewModel.loadNote(editId)
                    }
                    return viewModel as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}