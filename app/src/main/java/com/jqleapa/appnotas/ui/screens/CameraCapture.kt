package com.jqleapa.appnotas.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
//import com.jqleapa.appnotas.ui.viewmodel.HomeViewModel
import com.jqlqapa.appnotas.data.model.MediaEntity
import com.jqlqapa.appnotas.ui.viewmodel.HomeViewModel
import java.io.File

fun createImageFile(context: Context): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg", storageDir)
}


@Composable
fun CameraCaptureScreen(viewModel: HomeViewModel, noteId: Long? = null) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                viewModel.addMedia(
                    MediaEntity(
                        noteId = noteId ?: 0,
                        filePath = photoUri.toString(),
                        mediaType = "IMAGE",
                        description = "Foto tomada con cámara"
                    )
                )
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val file = createImageFile(context)
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            photoUri?.let { uri ->
                takePictureLauncher.launch(uri)
            }
        }) {
            Text("Tomar Foto")
        }

    }
}
