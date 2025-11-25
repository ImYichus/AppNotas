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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jqlqapa.appnotas.ui.navigation.AppScreens
import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModel
import com.jqlqapa.appnotas.ui.viewmodel.NoteTab
import com.jqlqapa.appnotas.data.model.NoteEntity
import com.jqlqapa.appnotas.data.AppDataContainer
import com.jqlqapa.appnotas.ui.viewmodel.HomeUiState
import java.text.SimpleDateFormat
import com.jqlqapa.appnotas.ui.screens.*
import java.util.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.jqleapa.appnotas.ui.screens.NoteDetailContent

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

// Definición de colores para adaptabilidad
val phonePrimary = Color(0xFF4CAF50) // Verde
val tabletPrimary = Color(0xFF9C27B0) // Morado

// ----------------------------------------------------
// LÓGICA DE ADAPTACIÓN DE PANTALLA
// ----------------------------------------------------

enum class WindowType { Phone, Tablet }

@Composable
fun rememberWindowType(): WindowType {
    val configuration = LocalConfiguration.current
    // Un ancho mayor o igual a 600dp se considera tablet (diseño Maestro-Detalle)
    return if (configuration.screenWidthDp >= 600) WindowType.Tablet else WindowType.Phone
}

@Composable
fun getAdaptiveColorScheme(windowType: WindowType): ColorScheme {
    val baseColorScheme = MaterialTheme.colorScheme

    return remember(windowType) {
        when (windowType) {
            // Usa el verde para móvil
            WindowType.Phone -> baseColorScheme.copy(primary = phonePrimary, primaryContainer = phonePrimary.copy(alpha = 0.8f))
            // Usa el morado para tablet
            WindowType.Tablet -> baseColorScheme.copy(primary = tabletPrimary, primaryContainer = tabletPrimary.copy(alpha = 0.8f))
        }
    }
}

//
// COMPONENTE PRINCIPAL DE PANTALLA
//

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    val factory = AppDataContainer.homeViewModelFactory
    val viewModel: HomeViewModel = viewModel(factory = factory)
    // uiState ahora contiene selectedNoteId
    val uiState by viewModel.uiState.collectAsState()

    val windowType = rememberWindowType()
    val adaptiveColorScheme = getAdaptiveColorScheme(windowType)

    MaterialTheme(colorScheme = adaptiveColorScheme) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onSearchClick = { navController.navigate(AppScreens.Search.route) }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    // Lógica del FAB: Cierra el detalle en tablet, agrega nota en móvil/tablet sin detalle
                    onClick = {
                        if (windowType == WindowType.Tablet && uiState.selectedNoteId != null) {
                            // Cierra el panel de detalle en modo tablet
                            viewModel.clearSelection()
                        } else {
                            // Navega a AddNote
                            navController.navigate(AppScreens.AddNote.route)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    // Icono Adaptativo: 'Close' si hay detalle, 'Add' si no
                    val icon = if (windowType == WindowType.Tablet && uiState.selectedNoteId != null)
                        Icons.Default.Close else Icons.Default.Add

                    Icon(icon, contentDescription = "Acción flotante", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        ) { padding ->

            // Lógica de adaptación de contenido (Panel Único vs. Maestro-Detalle)
            if (windowType == WindowType.Tablet) {
                // Modo Tablet: Diseño Maestro-Detalle
                TabletLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    // En tablet, hacer click selecciona la nota
                    onNoteClick = { id -> viewModel.selectNote(id) },
                    onToggleCompletion = viewModel::toggleTaskCompletion,
                    onDelete = viewModel::deleteNote,
                    // La edición siempre navega a una pantalla completa de edición
                    onEditClick = { id -> navController.navigate(AppScreens.EditNote.withArgs(id.toString())) },
                    modifier = Modifier.padding(padding)
                )
            } else {
                // Modo Teléfono (Diseño de Panel Único)
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
                            // Navegación directa a NoteDetailScreen en modo móvil
                            onNoteClick = { id ->
                                navController.navigate(AppScreens.NoteDetail.withArgs(id.toString()))
                            },
                            onToggleCompletion = viewModel::toggleTaskCompletion,
                            onDelete = viewModel::deleteNote
                            // No se usa selectedNoteId en móvil
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// NUEVO COMPONENTE: Diseño Maestro-Detalle para Tablets
// ----------------------------------------------------

@Composable
fun TabletLayout(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onNoteClick: (Long) -> Unit,
    onToggleCompletion: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {

        // PANEL MAESTRO (Lista de Notas/Tareas) - Ocupa un 40% del ancho
        Column(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
            HomeTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { tab ->
                    viewModel.selectTab(tab)
                    // Limpiar selección al cambiar de pestaña
                    viewModel.clearSelection()
                }
            )

            when {
                uiState.isLoading -> LoadingScreen()
                uiState.currentList.isEmpty() -> EmptyState(uiState.selectedTab)
                else -> NoteTaskList(
                    notes = uiState.currentList,
                    onNoteClick = onNoteClick,
                    onToggleCompletion = onToggleCompletion,
                    onDelete = onDelete,
                    // Pasa el ID seleccionado para destacar la tarjeta
                    selectedNoteId = uiState.selectedNoteId
                )
            }
        }

        // Separador visual
        VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp)

        // PANEL DETALLE (Contenido de la Nota) - Ocupa un 60% del ancho
        Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            val selectedNoteId = uiState.selectedNoteId

            if (selectedNoteId != null) {
                // Usamos el composable NoteDetailContent
                NoteDetailContent(
                    noteId = selectedNoteId,
                    modifier = Modifier.fillMaxSize(),
                    onEditClick = { onEditClick(selectedNoteId) },
                    // Limpia el panel después de eliminar
                    onDeleteConfirmed = { viewModel.clearSelection() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Selecciona una nota o tarea de la izquierda para ver los detalles.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


// ----------------------------------------------------
// COMPONENTES REUTILIZABLES (NoteTaskList y NoteCard)
// ----------------------------------------------------

// MODIFICACIÓN: NoteTaskList simplificado para este diseño y con selectedNoteId
@Composable
fun NoteTaskList(
    notes: List<NoteEntity>,
    onNoteClick: (Long) -> Unit,
    onToggleCompletion: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    selectedNoteId: Long? = null // Nuevo parámetro para destacar
) {
    // La lista MAESTRA es siempre una columna vertical.
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
                onDelete = { onDelete(note) },
                isSelected = note.id == selectedNoteId
            )
        }
    }
}


// MODIFICACIÓN: NoteCard ahora acepta 'isSelected'
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    isSelected: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            // Usa primaryContainer si está seleccionado, surfaceVariant en caso contrario
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val titleStyle = if (note.isTask && note.isCompleted) {
                    TextStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)
                } else {
                    LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = note.title,
                    style = titleStyle.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note.description.take(50) + if (note.description.length > 50) "..." else "",
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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


// ----------------------------------------------------
// COMPONENTES DE BARRAS Y ESTADOS (Se mantienen)
// ----------------------------------------------------

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
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

@Composable
fun HomeTabRow(selectedTab: NoteTab, onTabSelected: (NoteTab) -> Unit) {
    val tabs = listOf(
        Triple(NoteTab.NOTES, "Notas", Icons.Filled.Description),
        Triple(NoteTab.TASKS, "Tareas", Icons.Filled.Checklist)
    )

    TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }) {
        tabs.forEach { (tab, title, icon) ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                icon = { Icon(imageVector = icon, contentDescription = title) },
                text = { Text(title, fontWeight = FontWeight.Bold) }
            )
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