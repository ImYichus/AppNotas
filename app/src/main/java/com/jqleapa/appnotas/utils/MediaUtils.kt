// MediaUtils.kt (o al final de MiFileProviderMultimedia.kt)
package com.jqlqapa.appnotas.utils // Usa tu paquete de utilidades

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Asegúrate de que el 'authority' coincida con lo que tienes en el AndroidManifest.xml
// En tu manifest es: "net.ivanvega.archivosmultimediaconcompose.fileprovidermultimedia"
// Ojo: Si cambiaste el paquete en el manifest a com.jqlqapa..., actualízalo aquí también.
const val FILE_PROVIDER_AUTHORITY = "net.ivanvega.archivosmultimediaconcompose.fileprovidermultimedia"

fun createMediaFile(context: Context, type: String): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val (prefix, suffix, directory) = when (type) {
        "IMAGE" -> Triple("IMG_", ".jpg", Environment.DIRECTORY_PICTURES)
        "VIDEO" -> Triple("VID_", ".mp4", Environment.DIRECTORY_MOVIES)
        "AUDIO" -> Triple("AUD_", ".mp4", Environment.DIRECTORY_MUSIC) // Usamos mp4/aac para audio
        else -> Triple("FILE_", ".dat", Environment.DIRECTORY_DOWNLOADS)
    }

    val storageDir = context.getExternalFilesDir(directory)
    return File.createTempFile("${prefix}${timeStamp}_", suffix, storageDir)
}

fun getMediaUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
}