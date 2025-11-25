package com.jqleapa.appnotas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jqlqapa.appnotas.data.NoteRepository
import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModel

// Esta clase es la "receta" que le dice a Android cómo crear HomeViewModel
class HomeViewModelFactory(
    private val noteRepository: NoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica que la clase solicitada sea HomeViewModel
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Retorna una nueva instancia de HomeViewModel, inyectando el NoteRepository
            return HomeViewModel(noteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}