package com.jqleapa.appnotas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jqleapa.appnotas.ui.navigation.AppScreens
import com.jqleapa.appnotas.ui.viewmodel.HomeViewModel // <-- Importar el ViewModel
import com.jqleapa.appnotas.ui.viewmodel.NoteTab // <-- Importar el Enum
import com.jqlqapa.appnotas.data.model.NoteEntity // <-- Importar la entidad de datos

// NOTA: Debes asegurarte de tener un AppViewModelProvider o usar Hilt/Koin para inyectar HomeViewModel
// Para fines de este código, se asume un provider básico de Compose.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    // El ViewModel se obtiene automáticamente en el ámbito del Composable
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // 1. Recoger el estado de la UI del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                canNavigateBack = false, // En Home, siempre es false
                onBackClick = { /* No hay acción de regreso */ },
                onSearchClick = { navController.navigate(AppScreens.Search.route) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(AppScreens.AddNote.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar nota")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 2. Componente de Cejillas (Tabs)
            HomeTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab
            )

            // 3. Contenido Principal (Lista o Mensaje de Carga/Vacío)
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.currentList.isEmpty() -> EmptyState(uiState.selectedTab)
                else -> NoteTaskList(
                    notes = uiState.currentList,
                    onNoteClick = { id ->
                        // Navega al detalle/edición, pasando el ID de la nota
                        navController.navigate("${AppScreens.EditNote.route}/$id")
                    },
                    onToggleCompletion = viewModel::toggleTaskCompletion,
                    onDelete = viewModel::deleteNote
                )
            }
        }
    }
}

// --------------------------------------------------------------------------------
// --- COMPONENTES REUTILIZABLES ---
// --------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    canNavigateBack: Boolean,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Mis Notas y Tareas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
            }
        }
    )
}

@Composable
fun HomeTabRow(selectedTab: NoteTab, onTabSelected: (NoteTab) -> Unit) {
    val tabs = listOf(NoteTab.NOTES to "Notas", NoteTab.TASKS to "Tareas")

    TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }) {
        tabs.forEach { (tab, title) ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = { Text(title, fontWeight = FontWeight.Bold) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NoteTaskList(
    notes: List<NoteEntity>,
    onNoteClick: (Long) -> Unit,
    onToggleCompletion: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note.id) },
                onToggleCompletion = { onToggleCompletion(note) },
                onDelete = { onDelete(note) }
            )
        }
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Columna de contenido (Título y Descripción)
            Column(modifier = Modifier.weight(1f)) {
                val titleStyle = if (note.isTask && note.isCompleted) {
                    TextStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)
                } else {
                    LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = note.title,
                    style = titleStyle.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.description.take(50) + if (note.description.length > 50) "..." else "",
                    color = Color.DarkGray,
                    maxLines = 2
                )

                if (note.isTask && note.taskDueDate != null) {
                    Text(
                        text = "Vence: ${formatDate(note.taskDueDate)}", // <- Función de formato necesaria
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Acciones de Tarea y Eliminación
            if (note.isTask) {
                Checkbox(
                    checked = note.isCompleted,
                    onCheckedChange = { onToggleCompletion() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

// Placeholder para el estado vacío
@Composable
fun EmptyState(tab: NoteTab) {
    val message = when (tab) {
        NoteTab.NOTES -> "¡No tienes notas! Toca '+' para crear una."
        NoteTab.TASKS -> "¡No tienes tareas pendientes! Toca '+' para crear una."
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

// Placeholder para el estado de carga
@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// NOTA: Se requiere una función de formato de fecha.
// Por ahora, usamos un placeholder. Esta debería ir en una clase utilitaria.
fun formatDate(timestamp: Long): String {
    // Implementar SimpleDateFormat aquí o usar java.time si el API es suficiente
    return "Fecha Formateada"
}

// NOTA: Se requiere la definición de AppViewModelProvider para que viewModel() funcione
// class AppViewModelProvider { ... }