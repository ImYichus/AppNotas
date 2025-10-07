package com.jqleapa.appnotas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

data class Attachment(val name: String, val type: String, val description: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EditNoteScreen() {
    var title by remember { mutableStateOf("Título existente") }
    var description by remember { mutableStateOf("Descripción existente") }
    var isTask by remember { mutableStateOf(true) }
    var dueDate by remember { mutableStateOf<Date?>(Calendar.getInstance().time) }
    var reminders by remember { mutableStateOf(mutableListOf<Date>()) }
    var attachments by remember { mutableStateOf(mutableListOf<Attachment>()) }

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Guardar cambios */ },
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Guardar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selección Nota/Tarea
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tipo:", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !isTask, onClick = { isTask = false })
                    Text("Nota")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = isTask, onClick = { isTask = true })
                    Text("Tarea")
                }
            }

            // Título y Descripción
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // Fecha y hora (solo si es tarea)
            if (isTask) {
                Button(onClick = { /* Abrir DatePicker */ }) {
                    Text(dueDate?.toString() ?: "Seleccionar Fecha y Hora")
                }
            }

            // Recordatorios dinámicos
            if (isTask) {
                Column {
                    Text("Recordatorios:", fontWeight = FontWeight.Medium)
                    reminders.forEachIndexed { index, reminder ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(reminder.toString())
                            Button(onClick = { reminders.removeAt(index) }) {
                                Text("Eliminar")
                            }
                        }
                    }
                    Button(onClick = { /* Agregar nuevo recordatorio */ }) {
                        Text("Agregar Recordatorio")
                    }
                }
            }

            // Adjuntar multimedia y grabaciones
            LazyColumn {
                items(attachments) { attachment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(attachment.name, fontWeight = FontWeight.Medium)
                            if (attachment.description.isNotEmpty()) {
                                Text(attachment.description, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { attachments.remove(attachment) }) {
                            Text("Eliminar")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { /* Adjuntar multimedia */ }) {
                    Text("Adjuntar Multimedia")
                }
                Button(onClick = { /* Grabar audio */ }) {
                    Text("Grabar Audio")
                }
            }
        }
    }
}
