package com.jqleapa.appnotas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.jqlqapa.appnotas.data.AppDataContainer
import com.jqleapa.appnotas.ui.viewmodel.AddEditNoteViewModel
import com.jqleapa.appnotas.ui.viewmodel.MediaItem
import com.jqleapa.appnotas.ui.viewmodel.ReminderItem
import java.text.SimpleDateFormat
import java.util.*

// Se mantiene la Factory simplificada (que debe estar en EditNoteScreen.kt o un archivo común)
// Se redefinen las funciones auxiliares aquí para asegurar la compilación, o se asume su importación.
private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

// Función principal de la pantalla
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen( // ✅ La función debe estar en este archivo para que la referencia se resuelva
    navController: NavHostController,
    // Usamos la Factory definida en EditNoteScreen.kt
    viewModel: AddEditNoteViewModel = viewModel(
        factory = AddEditViewModelFactory(AppDataContainer.noteRepository)
    )
) {
    // 1. Recoger el estado de la UI del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Estados para controlar diálogos
    var showDatePicker by remember { mutableStateOf(false) }

    // --- DIÁLOGOS Y NAVEGACIÓN ---

    if (uiState.saveSuccessful) {
        LaunchedEffect(Unit) {
            viewModel.saveComplete() // Resetear el estado
            navController.popBackStack() // Regresa a la pantalla anterior
        }
    }

    if (showDatePicker) {
        SimulatedDateTimePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { time ->
                viewModel.updateTaskDueDate(time)
                showDatePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Nueva Nota o Tarea",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Llama a saveNote(). Como uiState.noteId es nulo, insertará.
                    if (!uiState.isSaving && uiState.title.isNotBlank()) {
                        viewModel.saveNote()
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                } else {
                    Text("Guardar", modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    ) { padding ->

        // El resto del contenido de la pantalla (Inputs, Checkboxes, etc.) es igual al de EditNoteScreen
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Selección Nota/Tarea
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tipo:", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !uiState.isTask,
                            onClick = { viewModel.updateIsTask(false) }
                        )
                        Text("Nota")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(
                            selected = uiState.isTask,
                            onClick = { viewModel.updateIsTask(true) }
                        )
                        Text("Tarea")
                    }
                }
            }

            item {
                // Título y Descripción
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.title.isBlank() && !uiState.isSaving
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // --- Lógica de Tarea ---
            if (uiState.isTask) {
                item {
                    // Fecha y hora
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fecha de Cumplimiento:", fontWeight = FontWeight.Medium)
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                uiState.taskDueDate?.let { dateFormatter.format(Date(it)) }
                                    ?: "Seleccionar Fecha y Hora"
                            )
                        }
                    }
                }

                item {
                    // Requisito: Marcar como cumplida
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Tarea Cumplida:", fontWeight = FontWeight.Medium)
                        Checkbox(
                            checked = uiState.isCompleted,
                            onCheckedChange = viewModel::toggleCompletion
                        )
                    }
                    Button(
                        onClick = { showDatePicker = true } // Posponer es re-seleccionar fecha
                    ) {
                        Text("Posponer Tarea")
                    }
                }

                item {
                    // Recordatorios dinámicos
                    Text("Recordatorios:", fontWeight = FontWeight.Medium)
                }

                // Lista de Recordatorios
                items(uiState.reminders, key = { it.id }) { reminderItem ->
                    ReminderItemComponent(
                        reminderItem = reminderItem,
                        onDelete = { viewModel.deleteReminder(reminderItem) }
                    )
                }

                item {
                    Button(
                        onClick = { viewModel.addReminder() },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("Agregar Recordatorio")
                    }
                }
            }
            // --- Fin Lógica de Tarea ---

            // --- Adjuntos Multimedia ---
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Archivos Adjuntos:", fontWeight = FontWeight.Medium)
            }

            // Lista de Adjuntos
            items(uiState.mediaFiles, key = { it.uri }) { mediaItem ->
                AttachmentItemComponent(
                    mediaItem = mediaItem,
                    onDelete = { /* Lógica de eliminación en ViewModel: deleteMedia(mediaItem) */ }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { /* Lógica para Adjuntar multimedia */ }) {
                        Text("Adjuntar Multimedia")
                    }
                    Button(onClick = { /* Lógica para Grabar audio */ }) {
                        Text("Grabar Audio")
                    }
                }
            }
        }
    }
}

// Las funciones auxiliares deben estar definidas aquí o ser importables.
@Composable
private fun ReminderItemComponent(reminderItem: ReminderItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = reminderItem.timeInMillis.let { dateFormatter.format(Date(it)) },
                fontWeight = FontWeight.Normal
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar Recordatorio",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AttachmentItemComponent(mediaItem: MediaItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(mediaItem.uri.substringAfterLast('/'), fontWeight = FontWeight.Medium)
                if (mediaItem.description.isNotEmpty()) {
                    Text(
                        mediaItem.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar Adjunto",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SimulatedDateTimePickerDialog(onDismiss: () -> Unit, onDateSelected: (Long) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Fecha y Hora") },
        text = { Text("Simulación: La fecha se establecerá a mañana a las 10:00 AM.") },
        confirmButton = {
            Button(onClick = {
                val tomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                onDateSelected(tomorrow)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}