package com.jqleapa.appnotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// Importaciones corregidas y alineadas con la capa de datos
import com.jqlqapa.appnotas.data.NoteRepository
import com.jqlqapa.appnotas.data.model.NoteEntity
import com.jqlqapa.appnotas.data.model.MediaEntity
import com.jqlqapa.appnotas.data.model.ReminderEntity

// --- Data Classes (UiState) ---

data class AddEditUiState(
    val noteId: Long? = null,
    val title: String = "",
    val description: String = "",
    val isTask: Boolean = false,
    val isCompleted: Boolean = false,
    val taskDueDate: Long? = null, // Timestamp de la fecha de cumplimiento
    val mediaFiles: List<MediaItem> = emptyList(),
    val reminders: List<ReminderItem> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccessful: Boolean = false,
    val error: String? = null
)

data class MediaItem(
    val uri: String, // URI del archivo (local o temporal)
    val description: String,
    val type: String, // Ej: "image", "video", "audio"
    val isTemporary: Boolean = true,
    val mediaId: Long = 0L // ID si ya existe en la BD
)

data class ReminderItem(
    val timeInMillis: Long,
    val id: Long = 0L, // ID si ya existe en la BD
    val uuid: String = UUID.randomUUID().toString() // Usado para WorkManager
)


class AddEditNoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    // 1. FUNCIÓN PARA CARGAR NOTA COMPLETA (EDICIÓN)
    fun loadNote(id: Long) {
        if (id > 0) {
            viewModelScope.launch {
                // Usamos getNoteDetails para obtener Nota, Medios y Recordatorios relacionados
                val details = repository.getNoteDetails(id).first()

                // Mapear NoteWithMediaAndReminders a UiState
                _uiState.update {
                    it.copy(
                        noteId = details.note.id,
                        title = details.note.title,
                        description = details.note.description,
                        isTask = details.note.isTask,
                        isCompleted = details.note.isCompleted,
                        taskDueDate = details.note.taskDueDate,

                        // Mapeo de MediaEntity a MediaItem
                        mediaFiles = details.media.map { media ->
                            MediaItem(
                                uri = media.filePath,
                                description = media.description ?: "",
                                type = media.mediaType,
                                isTemporary = false, // Son archivos persistentes
                                mediaId = media.id
                            )
                        },
                        // Mapeo de ReminderEntity a ReminderItem
                        reminders = details.reminders.map { reminder ->
                            ReminderItem(
                                timeInMillis = reminder.reminderDateTime,
                                id = reminder.id,
                                // Asumimos que la entidad tendrá un UUID para WorkManager
                                uuid = UUID.randomUUID().toString() // Temporal, idealmente se cargaría de la DB
                            )
                        }
                    )
                }
            }
        }
    }

    // Funciones de actualización de campos (sin cambios)
    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun toggleIsTask(isTask: Boolean) {
        _uiState.update { it.copy(isTask = isTask, taskDueDate = if (!isTask) null else it.taskDueDate) }
    }

    fun updateTaskDueDate(date: Long) {
        _uiState.update { it.copy(taskDueDate = date) }
    }

    // Función CRÍTICA para guardar/actualizar la nota y sus adjuntos
    fun saveNote() {
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Crear o actualizar la entidad principal
                val current = _uiState.value
                val noteToSave = NoteEntity(
                    id = current.noteId ?: 0L,
                    title = current.title.trim(),
                    description = current.description.trim(),
                    isTask = current.isTask,
                    isCompleted = current.isCompleted,
                    taskDueDate = current.taskDueDate,
                    registrationDate = System.currentTimeMillis()
                )

                // Guarda la nota y obtiene el ID de vuelta
                val newNoteId = repository.saveNote(noteToSave)

                // 2. Guardar archivos multimedia (¡Alineado con MediaEntity!)
                current.mediaFiles.forEach { mediaItem ->
                    val mediaEntity = MediaEntity(
                        id = mediaItem.mediaId,
                        noteId = newNoteId,
                        filePath = mediaItem.uri, // <-- CORRECCIÓN: Alineado con MediaEntity
                        mediaType = mediaItem.type, // <-- CORRECCIÓN: Alineado con MediaEntity
                        description = mediaItem.description,
                        thumbnailPath = mediaItem.uri // NOTA: La lógica de generación de miniatura va en el Repository
                    )
                    repository.addMedia(mediaEntity) // <-- CORRECCIÓN: Alineado con NoteRepository
                }

                // 3. Guardar recordatorios (¡Alineado con ReminderEntity!)
                current.reminders.forEach { reminderItem ->
                    val reminderEntity = ReminderEntity(
                        id = reminderItem.id,
                        noteId = newNoteId,
                        reminderDateTime = reminderItem.timeInMillis, // <-- CORRECCIÓN: Alineado con ReminderEntity
                        // NOTA: Si decides añadir 'uuid' y 'isActive' a ReminderEntity, descomenta y ajusta aquí:
                        // uuid = reminderItem.uuid
                    )
                    repository.addReminder(reminderEntity) // <-- CORRECCIÓN: Alineado con NoteRepository
                }

                _uiState.update { it.copy(isSaving = false, saveSuccessful = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }

    // Función necesaria para resetear el estado de navegación
    fun saveComplete() {
        _uiState.update { it.copy(saveSuccessful = false) }
    }
}