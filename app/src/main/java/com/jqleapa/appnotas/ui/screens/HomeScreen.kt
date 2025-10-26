package com.jqlqapa.appnotas.ui.screens // ⬅️ CORRECCIÓN 1: Paquete cambiado a 'jqlqapa'

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jqlqapa.appnotas.ui.navigation.AppScreens // ⬅️ CORRECCIÓN: Usando 'jqlqapa'
import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModel // ⬅️ CORRECCIÓN: Usando 'jqlqapa'
//import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModelFactory // ⬅️ Importación necesaria
import com.jqlqapa.appnotas.ui.viewmodel.NoteTab // ⬅️ CORRECCIÓN: Usando 'jqlqapa'
import com.jqlqapa.appnotas.data.model.NoteEntity
import com.jqlqapa.appnotas.data.AppDataContainer
import com.jqlqapa.appnotas.ui.navigation.NOTE_ID_ARG // ⬅️ CORRECCIÓN: Usando 'jqlqapa'
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    // ✅ INYECTAMOS EL VIEWMODEL (más limpio) o lo resolvemos en el Composable
) {
    // ⬇️ CORRECCIÓN 2: Obtener ViewModel de forma correcta usando el Singleton.
    // Asumimos que AppDataContainer ya está inicializado en AppNotasApplication.kt
    val context = LocalContext.current

    // Obtener la instancia de la fábrica del Singleton ya inicializado
    // Usamos el mismo patrón de AppNotasApplication.kt, asumiendo que el Singleton funciona
    val factory = AppDataContainer.homeViewModelFactory
    val viewModel: HomeViewModel = viewModel(factory = factory)

    // El resto del código usa el uiState de forma correcta
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
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
            HomeTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab
            )

            when {
                uiState.isLoading -> LoadingScreen()
                uiState.currentList.isEmpty() -> EmptyState(uiState.selectedTab)
                else -> NoteTaskList(
                    notes = uiState.currentList,
                    onNoteClick = { id ->
                        val routeWithId = AppScreens.EditNote.route.replace("{$NOTE_ID_ARG}", id.toString())
                        navController.navigate(routeWithId)
                    },
                    onToggleCompletion = viewModel::toggleTaskCompletion,
                    onDelete = viewModel::deleteNote
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onSearchClick: () -> Unit) {
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
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    if (isTablet) {
        // Para tablets: Grid de 2 columnas
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
    } else {
        // Para móviles: lista vertical normal
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
                    // ✅ CORRECCIÓN: Usar SimpleDateFormat para formatear el timestamp
                    Text(
                        text = "Vence: ${dateFormatter.format(Date(note.taskDueDate))}",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (note.isTask) {
                Checkbox(
                    checked = note.isCompleted,
                    onCheckedChange = { onToggleCompletion() },
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

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

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
