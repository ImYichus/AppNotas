// AppContainer.kt - MODIFICACIÓN COMPLETA

package com.jqlqapa.appnotas.data

import android.content.Context
import androidx.room.Room // ¡Necesario para Room.databaseBuilder!
import com.jqlqapa.appnotas.data.model.MediaDao
import com.jqlqapa.appnotas.data.model.NoteDao
import com.jqlqapa.appnotas.data.model.ReminderDao
import com.jqleapa.appnotas.ui.viewmodel.HomeViewModelFactory

/**
 * Contenedor de dependencias simple (Service Locator) para toda la aplicación.
 * Provee la instancia de la base de datos, DAOs y Repositorios.
 */
interface AppContainer {
    val noteRepository: NoteRepository
    val homeViewModelFactory: HomeViewModelFactory
}

// *** MODIFICACIÓN CLAVE: Convertimos la clase en un objeto Singleton/Service Locator ***
object AppDataContainer : AppContainer { // ¡Cambiamos 'class' por 'object'!

    // Usamos lateinit para las dependencias, que se inicializarán en initialize()
    private lateinit var internalNoteRepository: NoteRepository
    private lateinit var internalHomeViewModelFactory: HomeViewModelFactory
    private var isInitialized = false

    // Propiedades de la interfaz
    override val noteRepository: NoteRepository
        get() = if (isInitialized) internalNoteRepository else throw IllegalStateException("AppContainer no ha sido inicializado. Llame a initialize(Context).")

    override val homeViewModelFactory: HomeViewModelFactory
        get() = if (isInitialized) internalHomeViewModelFactory else throw IllegalStateException("AppContainer no ha sido inicializado. Llame a initialize(Context).")

    // Función de inicialización
    fun initialize(context: Context) {
        if (isInitialized) return

        val applicationContext = context.applicationContext

        // 1. Base de datos
        val database: AppDatabase = Room.databaseBuilder(
            context = applicationContext,
            klass = AppDatabase::class.java,
            name = "notes_database"
        ).build()

        // 2. Provisión de DAOs
        val noteDao: NoteDao = database.noteDao()
        val mediaDao: MediaDao = database.mediaDao()
        val reminderDao: ReminderDao = database.reminderDao()

        // 3. Provisión de Repositorio
        internalNoteRepository = NoteRepository(noteDao, mediaDao, reminderDao)

        // 4. Provisión de la Factory de ViewModel
        internalHomeViewModelFactory = HomeViewModelFactory(noteRepository = internalNoteRepository)

        isInitialized = true
    }
}

// NOTA: Si su archivo original tenía una clase llamada AppDataContainer,
// asegúrese de que ha sido reemplazada por el 'object' de arriba.