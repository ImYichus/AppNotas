package com.jqlqapa.appnotas.data // Paquete correcto: jqlqapa

import android.content.Context
import androidx.room.Room
import com.jqleapa.appnotas.ui.viewmodel.HomeViewModelFactory
import com.jqlqapa.appnotas.data.model.MediaDao // Correcto
import com.jqlqapa.appnotas.data.model.NoteDao // Correcto
import com.jqlqapa.appnotas.data.model.ReminderDao // Correcto

// ⬅️ CORRECCIÓN: Usar el paquete correcto 'jqlqapa'
//import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModelFactory

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

    // Propiedades de la interfaz. Esto lanza el error si no se llama a initialize.
    override val noteRepository: NoteRepository
        get() = if (isInitialized) internalNoteRepository else throw IllegalStateException("AppContainer not initialized. Call initialize(context) first.")

    override val homeViewModelFactory: HomeViewModelFactory
        get() = if (isInitialized) internalHomeViewModelFactory else throw IllegalStateException("AppContainer not initialized. Call initialize(context) first.")

    // Función de inicialización
    fun initialize(context: Context) {
        if (isInitialized) return

        val applicationContext = context.applicationContext

        // 1. Base de datos
        // NOTA: Si aún da error aquí, es posible que necesites añadir .allowMainThreadQueries() para debug
        // pero NO es recomendado para producción.
        val database: AppDatabase = Room.databaseBuilder(
            context = applicationContext,
            klass = AppDatabase::class.java,
            name = "notes_database"
        )
            // Agregamos .fallbackToDestructiveMigration() como red de seguridad temporal
            // si hay problemas con la versión de la DB.
            .fallbackToDestructiveMigration()
            .build()

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
