package com.jqleapa.appnotas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.jqlqapa.appnotas.data.AppDataContainer
//import com.jqleapa.appnotas.ui.navigation.AppScreens
import com.jqleapa.appnotas.ui.viewmodel.AddEditNoteViewModel
import com.jqleapa.appnotas.ui.viewmodel.AddEditUiState
import com.jqleapa.appnotas.ui.viewmodel.MediaItem
import com.jqleapa.appnotas.ui.viewmodel.ReminderItem
import com.jqlqapa.appnotas.ui.navigation.AppScreens
import java.text.SimpleDateFormat
import java.util.*

private val AddEditUiState.isDeleted: Boolean
    get() = this.saveSuccessful && this.noteId == null
/*private val AddEditUiState.isDeleted: Boolean*/
private val simpleDateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val fullDateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(noteId: Long, navController: NavHostController) {

    val viewModel: AddEditNoteViewModel = viewModel(
        factory = AddEditNoteViewModel.Companion.AddEditViewModelFactory(
            noteRepository = AppDataContainer.noteRepository,
            editId = noteId
        )
    )

    val uiState by viewModel.uiState.collectAsState()


    LaunchedEffect(uiState.saveSuccessful) {
        if (uiState.saveSuccessful) {

            navController.popBackStack(AppScreens.Home.route, inclusive = false)
            viewModel.saveComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.title.takeIf { it.isNotBlank() } ?: "Detalle de Nota/Tarea",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                item {
                    Text(
                        uiState.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Descripción
                item {
                    Text(
                        uiState.description,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Tipo y Estado de la Tarea
                if (uiState.isTask) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Tipo: Tarea",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            uiState.taskDueDate?.let { dueDate ->
                                Text(
                                    "Vencimiento: ${fullDateFormatter.format(Date(dueDate))}",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                if (uiState.isCompleted) "Estado: Completada " else "Estado: Pendiente 🕒",
                                fontWeight = FontWeight.Medium,
                                color = if (uiState.isCompleted) Color.Green else Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.toggleCompletion(!uiState.isCompleted) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (uiState.isCompleted) "Marcar como Pendiente" else "Marcar como Completada")
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            "Tipo: Nota",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Recordatorios
                if (uiState.reminders.isNotEmpty()) {
                    item {
                        Text("Recordatorios Programados:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.reminders) { reminder ->
                                ReminderDisplay(reminder = reminder)
                            }
                        }
                    }
                }

                // Archivos Adjuntos (Media)
                if (uiState.mediaFiles.isNotEmpty()) {
                    item {
                        Text("Archivos Adjuntos:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.mediaFiles) { media ->
                                AttachmentDisplay(media = media)
                            }
                        }
                    }
                }

                // Botones Editar y Eliminar
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón Editar
                        Button(
                            onClick = {
                                // Navegar a la pantalla de edición
                                navController.navigate(AppScreens.EditNote.withArgs(noteId.toString()))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Editar", color = MaterialTheme.colorScheme.onSecondary)
                        }

                        // Botón Eliminar
                        Button(
                            onClick = { viewModel.deleteNote() }, // Llama a la función de eliminación
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }

                // Mensaje de Error
                uiState.error?.let { errorMsg ->
                    item {
                        Text(
                            "Error: $errorMsg",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    )
}
@Composable
fun NoteDetailContent(
    noteId: Long,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {

    // El ViewModel se crea aquí y se mantiene en el ciclo de vida del composable.
    val viewModel: AddEditNoteViewModel = viewModel(
        factory = AddEditNoteViewModel.Companion.AddEditViewModelFactory(
            noteRepository = AppDataContainer.noteRepository,
            editId = noteId
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    // Maneja la acción después de guardar/actualizar (e.g., toggle completion)
    LaunchedEffect(uiState.saveSuccessful) {
        if (uiState.saveSuccessful) {
            viewModel.saveComplete()
        }
    }

    // Maneja la acción después de la eliminación exitosa.
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleteConfirmed() // Ejecuta el callback para la navegación/limpieza de selección.
        }
    }

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        item {
            Text(
                uiState.title.takeIf { it.isNotBlank() } ?: "Nota Sin Título",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Descripción
        item {
            Text(
                uiState.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tipo y Estado de la Tarea
        if (uiState.isTask) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Tipo: Tarea",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    uiState.taskDueDate?.let { dueDate ->
                        Text(
                            "Vencimiento: ${fullDateFormatter.format(Date(dueDate))}",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Text(
                        // Símbolo actualizado para mejor visualización
                        if (uiState.isCompleted) "Estado: Completada ✅" else "Estado: Pendiente 🕒",
                        fontWeight = FontWeight.Medium,
                        color = if (uiState.isCompleted) Color(0xFF4CAF50) else Color(0xFFFF5722)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.toggleCompletion(!uiState.isCompleted) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (uiState.isCompleted) "Marcar como Pendiente" else "Marcar como Completada")
                    }
                }
            }
        } else {
            item {
                Text(
                    "Tipo: Nota",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Recordatorios
        if (uiState.reminders.isNotEmpty()) {
            item {
                Text("Recordatorios Programados:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.reminders) { reminder ->
                        ReminderDisplay(reminder = reminder)
                    }
                }
            }
        }

        // Archivos Adjuntos (Media)
        if (uiState.mediaFiles.isNotEmpty()) {
            item {
                Text("Archivos Adjuntos:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.mediaFiles) { media ->
                        AttachmentDisplay(media = media)
                    }
                }
            }
        }

        // Botones Editar y Eliminar
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botón Editar
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Editar", color = MaterialTheme.colorScheme.onSecondary)
                }

                // Botón Eliminar
                Button(
                    onClick = { viewModel.deleteNote() }, // Llama a la función de eliminación
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }
            }
        }

        // Mensaje de Error
        uiState.error?.let { errorMsg ->
            item {
                Text(
                    "Error: $errorMsg",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        }
    }
}
@Composable
private fun ReminderDisplay(reminder: ReminderItem) {
    Card(
        modifier = Modifier.width(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Recordatorio:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                // Usa 'timeInMillis' de ReminderItem
                fullDateFormatter.format(Date(reminder.timeInMillis)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AttachmentDisplay(media: MediaItem) {
    Card(
        modifier = Modifier.width(150.dp).height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                // Usa 'uri' de MediaItem
                media.uri.substringAfterLast('/'),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                fontSize = 12.sp
            )
            if (media.description.isNotBlank()) {
                Text(
                    media.description,
                    fontSize = 10.sp,
                    maxLines = 1,
                    color = Color.Gray
                )
            }
            Text(
                media.mediaType,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}