package com.jqlqapa.appnotas.data

import com.jqlqapa.appnotas.data.model.MediaDao
import com.jqlqapa.appnotas.data.model.MediaEntity
import com.jqlqapa.appnotas.data.model.NoteDao
import com.jqlqapa.appnotas.data.model.NoteEntity
import com.jqlqapa.appnotas.data.model.NoteWithMediaAndReminders
import com.jqlqapa.appnotas.data.model.ReminderDao
import com.jqlqapa.appnotas.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val mediaDao: MediaDao,
    private val reminderDao: ReminderDao
) {
    // --- CONSULTAS PRINCIPALES (Para HomeScreen) ---
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotesByRegistrationDate()
    fun getAllTasks(): Flow<List<NoteEntity>> = noteDao.getAllTasksByDueDate()
    fun searchNotesAndTasks(query: String): Flow<List<NoteEntity>> = noteDao.searchNotesAndTasks(query)

    fun getNoteDetails(id: Long): Flow<NoteWithMediaAndReminders> = noteDao.getNoteWithDetails(id)

    // --- OPERACIONES DE NOTA/TAREA (CRUD) ---
    suspend fun saveNote(note: NoteEntity): Long {
        return noteDao.insertNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        // Al eliminar la nota, Room puede manejar la eliminación de los elementos relacionados
        // si se usan Foreign Keys con DELETE ON CASCADE. Si no, lo hacemos manualmente:

        // 1. Obtener la nota completa para sus elementos
        val noteDetails = noteDao.getNoteWithDetails(note.id) // Requiere un .first() o algo similar si es Flow
        // Simplificaremos asumiendo CASCADE para las Entidades, o borraremos elementos sueltos.

        // Asumiendo que las FK NO tienen CASCADE:
        // Primero eliminamos medios y recordatorios para evitar huérfanos
        reminderDao.deleteRemindersForNote(note.id)
        // mediaDao.deleteMediaList(noteDetails.media) // Si obtuviste los detalles

        // Luego eliminamos la nota
        noteDao.deleteNote(note)
    }

    // --- OPERACIONES MULTIMEDIA ---
    suspend fun addMedia(media: MediaEntity) {
        mediaDao.insertMedia(media)
    }

    suspend fun deleteMedia(media: MediaEntity) {
        mediaDao.deleteMedia(media)
    }

    // --- OPERACIONES DE RECORDATORIOS ---
    suspend fun addReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
        // NOTA: El ViewModel debe usar este Long (el ID del recordatorio)
        // para programar el 'AlarmManager' o 'WorkManager' de Android.
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
        // NOTA: El ViewModel debe usar esto para CANCELAR la alarma programada.
    }
}