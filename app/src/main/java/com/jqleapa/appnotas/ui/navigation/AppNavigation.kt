package com.jqleapa.appnotas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jqleapa.appnotas.ui.screens.*

// Nombre del argumento para evitar errores de escritura
const val NOTE_ID_ARG = "noteId"

sealed class AppScreens(val route: String) {
    object Home : AppScreens("home")
    object AddNote : AppScreens("add_note")
    object NoteDetail : AppScreens("note_detail/{$NOTE_ID_ARG}")
    object EditNote : AppScreens("edit_note/{$NOTE_ID_ARG}")
    object Reminder : AppScreens("reminder")
    object Search : AppScreens("search")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Home.route
    ) {
        composable(AppScreens.Home.route) {
            HomeScreen(navController)
        }
        composable(AppScreens.AddNote.route) {
            AddNoteScreen(navController = navController)
        }
        composable(
            route = AppScreens.NoteDetail.route,
            arguments = listOf(navArgument(NOTE_ID_ARG) {
                type = NavType.LongType // ✅ DEBE SER LONG TYPE
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong(NOTE_ID_ARG)
            requireNotNull(noteId) { "El ID de la nota no puede ser nulo" }

            // Llama a la pantalla de detalle pasándole el ID
            NoteDetailScreen(noteId = noteId, navController = navController)
        }

        composable(
            route = AppScreens.EditNote.route,
            arguments = listOf(navArgument(NOTE_ID_ARG) {
                // El ID de la nota debe ser Long
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong(NOTE_ID_ARG)
            requireNotNull(noteId) { "El ID de la nota no puede ser nulo" }

            EditNoteScreen(noteId = noteId, navController = navController)
        }

        composable(
            route = AppScreens.NoteDetail.route,
            arguments = listOf(navArgument(NOTE_ID_ARG) {
                type = NavType.LongType // ✅ DEBE SER LONG TYPE
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong(NOTE_ID_ARG)
            requireNotNull(noteId) { "El ID de la nota no puede ser nulo" }

            // Llama a la pantalla de detalle pasándole el ID
            NoteDetailScreen(noteId = noteId, navController = navController)
        }

        composable(AppScreens.Search.route) {
            SearchScreen(navController = navController) // ✅ CORRECCIÓN CLAVE: Pasarle el NavController
        }
    }
}