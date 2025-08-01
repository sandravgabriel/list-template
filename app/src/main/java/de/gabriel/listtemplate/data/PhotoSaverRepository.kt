package de.gabriel.listtemplate.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PhotoSaverRepository(context: Context, private val contentResolver: ContentResolver) {

    private var _photo: File? = null

    fun getPhoto() = _photo

    private val cacheFolder = File(context.cacheDir, "photos").also { it.mkdir() }
    val photoFolder = File(context.filesDir, "photos").also { it.mkdir() }

    private fun generateFileName() = "${System.currentTimeMillis()}.jpg"
    private fun generatePhotoFile() = File(photoFolder, generateFileName())
    fun generatePhotoCacheFile() = File(cacheFolder, generateFileName())

    suspend fun cacheFromUri(uri: Uri) {
        withContext(Dispatchers.IO) {

            contentResolver.openInputStream(uri)?.use { input ->
                val cachedPhoto = generatePhotoCacheFile()

                cachedPhoto.outputStream().use { output ->
                    input.copyTo(output)
                    _photo = cachedPhoto
                }
            }
        }
    }

    suspend fun removeFile() {
        withContext(Dispatchers.IO) {
            _photo?.delete()
            _photo = null
        }
    }

    suspend fun savePhoto(): File? {
        return withContext(Dispatchers.IO) {
            val cachedFileToSave = _photo
            if (cachedFileToSave == null || !cachedFileToSave.exists()) {
                Log.w("PhotoSaver", "Keine gültige Cache-Datei zum Speichern vorhanden.") //TODO
                _photo = null
                return@withContext null
            }

            try {
                val targetFile = File(photoFolder, "${System.currentTimeMillis()}.jpg") // photoFolder ist filesDir/photos
                val savedPersistentFile = cachedFileToSave.copyTo(targetFile, overwrite = true)
                Log.d("PhotoSaver", "Datei persistent gespeichert unter: ${savedPersistentFile.absolutePath}") //TODO
                cachedFileToSave.delete()
                _photo = null
                return@withContext savedPersistentFile
            } catch (e: Exception) {
                Log.e("PhotoSaver", "Fehler beim Speichern der Datei von Cache nach Persistent", e) //TODO
                // Optional: Versuchen, die (möglicherweise unvollständige) Zieldatei zu löschen, wenn ein Fehler auftritt
                // targetFile.delete() // Benötigt, dass targetFile außerhalb des try deklariert ist oder hier neu erstellt wird.
                return@withContext null
            }
        }
    }
}