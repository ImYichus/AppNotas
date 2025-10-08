package com.jqlqapa.appnotas.data

import android.content.Context
import androidx.room.Room
import com.jqlqapa.appnotas.data.model.MediaDao
import com.jqlqapa.appnotas.data.model.NoteDao
import com.jqlqapa.appnotas.data.model.ReminderDao

/**
 * Contenedor de dependencias simple (Service Locator) para toda la aplicación.
 * Provee la instancia de la base de datos, DAOs y Repositorios.
 */
interface AppContainer {
    val noteRepository: NoteRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    // 1. Configuración de la Base de Datos Room (Singleton)
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context = context.applicationContext,
            klass = AppDatabase::class.java,
            name = "notes_database"
        ).build()
    }

    // 2. Provisión de DAOs
    private val noteDao: NoteDao by lazy { database.noteDao() }
    private val mediaDao: MediaDao by lazy { database.mediaDao() }
    private val reminderDao: ReminderDao by lazy { database.reminderDao() }

    // 3. Provisión del Repository
    override val noteRepository: NoteRepository by lazy {
        NoteRepository(noteDao, mediaDao, reminderDao)
    }
}