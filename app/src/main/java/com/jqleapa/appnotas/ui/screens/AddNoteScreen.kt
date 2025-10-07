package com.jqleapa.appnotas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AddNoteScreen() {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isTask by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var reminders by remember { mutableStateOf(mutableListOf<Date>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agregar Nota o Tarea",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
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
                onClick = { /* Guardar Nota/Tarea */ },
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
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )

            // Fecha y hora (solo para tareas)
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

            // Botones para multimedia y audio (sin lista de attachments)
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
