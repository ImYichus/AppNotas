package com.jqlqapa.appnotas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jqleapa.appnotas.ui.screens.AddNoteScreen
import com.jqleapa.appnotas.ui.screens.CameraCaptureScreen
import com.jqleapa.appnotas.ui.screens.EditNoteScreen
import com.jqleapa.appnotas.ui.screens.GalleryScreen
import com.jqleapa.appnotas.ui.screens.NoteDetailScreen
import com.jqleapa.appnotas.ui.screens.SearchScreen
// Importar ReminderScreen que se ve en la estructura
import com.jqleapa.appnotas.ui.screens.ReminderScreen
import com.jqleapa.appnotas.ui.viewmodel.HomeViewModelFactory
import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModel
import com.jqlqapa.appnotas.data.NoteRepository
import com.jqlqapa.appnotas.ui.screens.HomeScreen

// Nombre del argumento para evitar errores de escritura
const val NOTE_ID_ARG = "noteId"

// 1. MODIFICACIÓN: Definición de las Pantallas
sealed class AppScreens(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    // Pantallas del Menú Inferior (SOLO DOS)
    // CAMBIO 1: Home ahora es "Notas" y usa el icono Description
    object Home : AppScreens("home", "Notas", Icons.Filled.Description)
    // CAMBIO 2: Reminder ahora es "Pendientes" y usa el icono Checklist
    object Reminder : AppScreens("reminder", "Pendientes", Icons.Filled.Checklist)

    // Pantallas que NO estarán en el menú inferior, pero existen en la app
    object Search : AppScreens("search", "Buscar", Icons.Filled.Search)
    object Camera : AppScreens("camera", "Cámara", Icons.Filled.CameraAlt)
    object Gallery : AppScreens("gallery", "Galería", Icons.Filled.PhotoLibrary)

    // Pantallas secundarias
    object AddNote : AppScreens("add_note")
    object NoteDetail : AppScreens("note_detail/{$NOTE_ID_ARG}")
    object EditNote : AppScreens("edit_note/{$NOTE_ID_ARG}")

    fun withArgs(vararg args: String): String {
        var finalRoute = this.route

        // La lógica asume que para NoteDetail y EditNote, solo se espera el Note ID.
        if (args.isNotEmpty()) {
            if (finalRoute.contains("{$NOTE_ID_ARG}")) {
                // Reemplaza el placeholder con el primer argumento (el ID)
                finalRoute = finalRoute.replace("{$NOTE_ID_ARG}", args[0])
            }
            // Si hay más argumentos (o la ruta no usa {noteId}), se concatenan.
            // Para rutas como "camera" o "gallery", esto no debería tener efecto.
            if (args.size > 1) {
                // Junta los argumentos restantes con '/'
                val additionalArgs = args.drop(1).joinToString("/")
                finalRoute += "/$additionalArgs"
            }
        }
        return finalRoute
    }
}

// Lista de elementos que aparecerán en la barra de navegación inferior
// CAMBIO 3: La lista ahora solo contiene Home (Notas) y Reminder (Pendientes)
val bottomNavItems = listOf(
    AppScreens.Home,
    AppScreens.Reminder
)

// 2. COMPONENTE: La Barra de Navegación Inferior (SIN CAMBIOS RELEVANTES AQUÍ)
@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        // Obtiene la ruta actual, ignorando argumentos como "{noteId}"
        val currentRoute = navBackStackEntry?.destination?.route?.substringBefore('/')

        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            // Los elementos del menú SIEMPRE tienen ícono y etiqueta (por definición en bottomNavItems)
            val icon = screen.icon!!
            val label = screen.label!!

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = { Text(label) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Configuración Estándar para Navegación Inferior:
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}


// 3. INTEGRACIÓN: Scaffold y NavHost
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    noteRepository: NoteRepository
) {
    // Crea la instancia de la Fábrica con la dependencia.
    val homeViewModelFactory = remember {
        HomeViewModelFactory(noteRepository = noteRepository)
    }

    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppScreens.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- PANTALLAS DEL MENÚ INFERIOR (Solo Home y Reminder) ---
            composable(AppScreens.Home.route) {
                HomeScreen(navController)
            }
            composable(AppScreens.Reminder.route) {
                // CAMBIO 4: Usamos ReminderScreen, que se ve en la estructura de tu proyecto
                ReminderScreen(navController = navController)
            }

            // --- PANTALLAS QUE NO ESTÁN EN EL MENÚ INFERIOR, pero son rutas válidas ---
            composable(AppScreens.Search.route) {
                SearchScreen(navController = navController)
            }
            composable(AppScreens.Camera.route) {
                CameraCaptureScreen(
                    viewModel = homeViewModelFactory.create(HomeViewModel::class.java) as HomeViewModel
                )
            }
            composable(AppScreens.Gallery.route) {
                GalleryScreen(
                    viewModel = homeViewModelFactory.create(HomeViewModel::class.java) as HomeViewModel
                )
            }


            // --- PANTALLAS SECUNDARIAS ---
            composable(AppScreens.AddNote.route) {
                AddNoteScreen(navController = navController)
            }
            composable(
                route = AppScreens.NoteDetail.route,
                arguments = listOf(navArgument(NOTE_ID_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong(NOTE_ID_ARG)
                requireNotNull(noteId) { "El ID de la nota no puede ser nulo" }

                NoteDetailScreen(noteId = noteId, navController = navController)
            }

            composable(
                route = AppScreens.EditNote.route,
                arguments = listOf(navArgument(NOTE_ID_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong(NOTE_ID_ARG)
                requireNotNull(noteId) { "El ID de la nota no puede ser nulo" }

                EditNoteScreen(noteId = noteId, navController = navController)
            }
        }
    }
}