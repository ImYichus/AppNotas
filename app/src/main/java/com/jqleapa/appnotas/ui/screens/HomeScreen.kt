package com.jqlqapa.appnotas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.jqlqapa.appnotas.ui.navigation.AppScreens
import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModel
import com.jqlqapa.appnotas.ui.viewmodel.NoteTab
import com.jqlqapa.appnotas.data.model.NoteEntity
import com.jqlqapa.appnotas.data.AppDataContainer
import com.jqlqapa.appnotas.ui.navigation.NOTE_ID_ARG
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
// Importar androidx.compose.material3.ColorScheme para la función de adaptabilidad
import androidx.compose.material3.ColorScheme

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

// Definición de colores para adaptabilidad
val phonePrimary = Color(0xFF4CAF50) // Verde
val tabletPrimary = Color(0xFF9C27B0) // Morado

// ----------------------------------------------------
// NUEVA FUNCIÓN: Determina el tipo de dispositivo
// ----------------------------------------------------
enum class WindowType { Phone, Tablet }

@Composable
fun rememberWindowType(): WindowType {
    val configuration = LocalConfiguration.current
    // Un ancho mayor o igual a 600dp se considera tablet
    return if (configuration.screenWidthDp >= 600) WindowType.Tablet else WindowType.Phone
}

// ----------------------------------------------------
// NUEVA FUNCIÓN: Genera el ColorScheme adaptable
// ----------------------------------------------------
@Composable
fun getAdaptiveColorScheme(windowType: WindowType): ColorScheme {
    // Usamos el ColorScheme actual para heredar todos los demás colores (secundario, fondo, etc.)
    val baseColorScheme = MaterialTheme.colorScheme

    return remember(windowType) {
        when (windowType) {
            WindowType.Phone -> baseColorScheme.copy(primary = phonePrimary, primaryContainer = phonePrimary.copy(alpha = 0.8f))
            WindowType.Tablet -> baseColorScheme.copy(primary = tabletPrimary, primaryContainer = tabletPrimary.copy(alpha = 0.8f))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val factory = AppDataContainer.homeViewModelFactory
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    // Obtener el tipo de dispositivo y el esquema de color adaptable
    val windowType = rememberWindowType()
    val adaptiveColorScheme = getAdaptiveColorScheme(windowType)

    // ⬇ CAMBIO: Aplicar un MaterialTheme anidado para cambiar los colores localmente
    MaterialTheme(colorScheme = adaptiveColorScheme) {
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
                    Icon(Icons.Default.Add, contentDescription = "Agregar nota", tint = MaterialTheme.colorScheme.onPrimary)
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
                            // Corregido: La función replace toma old/new values.
                            val routeWithId = AppScreens.EditNote.route.replace("{$NOTE_ID_ARG}", id.toString())
                            navController.navigate(routeWithId)
                        },
                        onToggleCompletion = viewModel::toggleTaskCompletion,
                        onDelete = viewModel::deleteNote
                    )
                }
            }
        }
    } // Cierre del MaterialTheme anidado
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
                // Usar colorScheme.onPrimary para el ícono (color que contrasta)
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

// ----------------------------------------------------
// MODIFICACIÓN: HomeTabRow con Íconos
// ----------------------------------------------------
@Composable
fun HomeTabRow(selectedTab: NoteTab, onTabSelected: (NoteTab) -> Unit) {
    // Definición de las pestañas con su título, su clave y su icono
    val tabs = listOf(
        Triple(NoteTab.NOTES, "Notas", Icons.Filled.Description), // Descripción para Notas
        Triple(NoteTab.TASKS, "Tareas", Icons.Filled.Checklist) // Checklist o Task para Tareas
    )

    TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }) {
        tabs.forEach { (tab, title, icon) ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                // El color seleccionado y no seleccionado se adapta al primary
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface,

                // ⬇ CAMBIO: Contenido de la pestaña con Icono y Texto
                icon = { Icon(imageVector = icon, contentDescription = title) },
                text = { Text(title, fontWeight = FontWeight.Bold) }
            )
        }
    }
}

// ----------------------------------------------------
// FUNCIONES RESTANTES (para resolver referencias)
// ----------------------------------------------------

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
        // Color de la tarjeta usará el scheme actual (surfaceVariant adaptado si lo tienes)
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