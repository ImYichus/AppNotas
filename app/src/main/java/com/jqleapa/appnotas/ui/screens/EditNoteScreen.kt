package com.jqlqapa.appnotas.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.jqleapa.appnotas.ui.viewmodel.AddEditNoteViewModel
import com.jqleapa.appnotas.ui.viewmodel.MediaItem
import com.jqleapa.appnotas.ui.viewmodel.ReminderItem
import com.jqlqapa.appnotas.data.AppDataContainer
import com.jqlqapa.appnotas.data.model.MediaEntity
import com.jqlqapa.appnotas.ui.components.AudioRecorderDialog
import com.jqlqapa.appnotas.ui.viewmodel.AddEditViewModelFactory
import com.jqlqapa.appnotas.utils.AndroidAudioRecorder
import com.jqlqapa.appnotas.utils.createMediaFile
import com.jqlqapa.appnotas.utils.getMediaUri
import java.text.SimpleDateFormat
import java.util.*

private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    noteId: Long,
    navController: NavHostController,
    // Se usa la factory importada de AddEditViewModelFactory.kt
    factory: AddEditViewModelFactory
) {
    // Obtenemos el ViewModel usando la factory inyectada
    val viewModel: AddEditNoteViewModel = viewModel(factory = factory)

    LaunchedEffect(key1 = noteId) {
        viewModel.loadNote(noteId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    // --- VARIABLES Y ESTADO PARA MULTIMEDIA ---
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tempVideoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showAudioDialog by remember { mutableStateOf(false) }

    // --- LANZADORES DE ACTIVIDAD ---
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            viewModel.addMedia(MediaEntity(
                noteId = noteId,
                filePath = tempImageUri.toString(),
                mediaType = "IMAGE",
                description = "Foto tomada"
            ))
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && tempVideoUri != null) {
            viewModel.addMedia(MediaEntity(
                noteId = noteId,
                filePath = tempVideoUri.toString(),
                mediaType = "VIDEO",
                description = "Video grabado"
            ))
        }
    }

    // --- NAVEGACIÓN Y GUARDADO ---
    if (uiState.saveSuccessful) {
        LaunchedEffect(Unit) {
            viewModel.saveComplete()
            navController.popBackStack()
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

    // --- DIÁLOGO DE AUDIO ---
    if (showAudioDialog) {
        AudioRecorderDialog(
            onDismiss = { showAudioDialog = false },
            onSave = { file ->
                val uri = getMediaUri(context, file)
                viewModel.addMedia(MediaEntity(
                    noteId = noteId,
                    filePath = uri.toString(),
                    mediaType = "AUDIO",
                    description = "Audio grabado"
                ))
            },
            audioRecorder = remember { AndroidAudioRecorder(context) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar Nota o Tarea",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
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

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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

            // --- SECCIÓN DE TAREAS Y RECORDATORIOS ---
            if (uiState.isTask) {
                item {
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
                        onClick = { showDatePicker = true }
                    ) {
                        Text("Posponer Tarea")
                    }
                }

                item {
                    Text("Recordatorios:", fontWeight = FontWeight.Medium)
                }

                items(uiState.reminders, key = { it.id }) { reminderItem ->
                    ReminderItemComponent(
                        reminderItem = reminderItem,
                        onDelete = { viewModel.deleteReminder(reminderItem) }
                    )
                }

                // BOTÓN: AGREGAR RECORDATORIO/ALARMA
                item {
                    Button(
                        onClick = { viewModel.addReminder() }, // Llama a la lógica del ViewModel para añadir un recordatorio nuevo
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar Alarma")
                    }
                }
            }

            // --- SECCIÓN MULTIMEDIA ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Multimedia:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista de Adjuntos existentes
            items(uiState.mediaFiles, key = { it.uri }) { mediaItem ->
                AttachmentItemComponent(
                    mediaItem = mediaItem,
                    onDelete = { viewModel.deleteMedia(mediaItem) }
                )
            }

            // BARRA DE BOTONES (FOTO, VIDEO, AUDIO)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // BOTÓN FOTO
                    IconButton(
                        onClick = {
                            val file = createMediaFile(context, "IMAGE")
                            val uri = getMediaUri(context, file)
                            tempImageUri = uri
                            takePictureLauncher.launch(uri)
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Tomar Foto", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    // BOTÓN VIDEO
                    IconButton(
                        onClick = {
                            val file = createMediaFile(context, "VIDEO")
                            val uri = getMediaUri(context, file)
                            tempVideoUri = uri
                            captureVideoLauncher.launch(uri)
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Grabar Video", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    // BOTÓN AUDIO
                    IconButton(
                        onClick = { showAudioDialog = true },
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Grabar Audio", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

private fun AddEditNoteViewModel.addMedia(mediaEntity: MediaEntity) {}

// --- COMPONENTES AUXILIARES ---

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
                // Muestra solo el nombre del archivo para que sea más limpio
                Text(mediaItem.uri.substringAfterLast('/'), fontWeight = FontWeight.Medium)
                if (mediaItem.description.isNotEmpty()) {
                    Text(
                        mediaItem.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Muestra el tipo de medio
                Text(
                    text = mediaItem.mediaType,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
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