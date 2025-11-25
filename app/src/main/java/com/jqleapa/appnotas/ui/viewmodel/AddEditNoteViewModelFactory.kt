// Archivo: com/jqlqapa/appnotas/ui/viewmodel/AddEditViewModelFactory.kt
package com.jqlqapa.appnotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jqleapa.appnotas.ui.viewmodel.AddEditNoteViewModel
import com.jqlqapa.appnotas.data.NoteRepository

class AddEditViewModelFactory( // ⬅️ Clase de nivel superior
    private val noteRepository: NoteRepository,
    private val editId: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditNoteViewModel::class.java)) {
            val viewModel = AddEditNoteViewModel(noteRepository)

            // Si se proporciona un ID, carga la nota para edición.
            if (editId != null && editId != 0L) {
                viewModel.loadNote(editId)
            }
            return viewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}