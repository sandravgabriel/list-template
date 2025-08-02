package de.gabriel.listtemplate.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class PhotoSaverRepository(context: Context, private val contentResolver: ContentResolver) {

    private var _photo: File? = null

    private val cacheFolder = File(context.cacheDir, "photos").also { it.mkdir() }
    val photoFolder = File(context.filesDir, "photos").also { it.mkdir() }

    private fun generateFileName() = "${System.currentTimeMillis()}.jpg"
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

    suspend fun removeFile(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val photoFile = _photo
                if (photoFile != null && photoFile.exists()) {
                    val deleted = photoFile.delete()
                    if (deleted) {
                        _photo = null
                    }
                    deleted
                } else {
                    // File doesn't exist or _photo is null, consider this a success
                    _photo = null // Still clear _photo if it was already null or non-existent
                    true
                }
            } catch (e: IOException) {
                //TODO
                // Log the exception or handle it appropriately
                false // Indicate failure
            } catch (e: SecurityException) {
                //TODO
                // Handle potential security exceptions if file permissions are an issue
                false
            }
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