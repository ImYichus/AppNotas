package com.jqleapa.appnotas.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String?,
    val isTask: Boolean = false,         // true si es tarea
    val createdAt: Long = System.currentTimeMillis(),
    val dueAt: Long? = null,             // fecha de cumplimiento (epoch millis) si es tarea
    val status: Int = 0                  // 0 = pendiente, 1 = cumplida, 2 = pospuesta
)
