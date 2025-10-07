package com.jqleapa.appnotas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jqleapa.appnotas.ui.screens.*

sealed class AppScreens(val route: String) {
    object Home : AppScreens("home")
    object AddNote : AppScreens("add_note")
    object NoteDetail : AppScreens("note_detail")
    object EditNote : AppScreens("edit_note")
    object Reminder : AppScreens("reminder")
    object Search : AppScreens("search")
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
            AddNoteScreen()
        }
        composable(AppScreens.NoteDetail.route) {
            NoteDetailScreen()
        }
        composable(AppScreens.EditNote.route) {
            EditNoteScreen()
        }
        composable(AppScreens.Reminder.route) {
            ReminderScreen()
        }
        composable(AppScreens.Search.route) {
            SearchScreen()
        }
    }
}

