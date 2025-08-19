package de.gabriel.listtemplate.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class PhotoSaverRepository(context: Context, private val contentResolver: ContentResolver) {

    @VisibleForTesting
    internal var photo: File? = null

    private val cacheFolder = File(context.cacheDir, "photos").also { it.mkdir() }
    val photoFolder = File(context.filesDir, "photos").also { it.mkdir() }

    private fun generateFileName() = "${System.currentTimeMillis()}.jpg"
    fun generatePhotoCacheFile() = File(cacheFolder, generateFileName())

    suspend fun cacheFromUri(uri: Uri) {
        photo = null // Zurücksetzen am Anfang

        withContext(Dispatchers.IO) {
            val targetFile = generatePhotoCacheFile()
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    // FileOutputStream(targetFile).use { outputStream -> // Alternative 1
                    targetFile.outputStream().use { outputStream ->      // Alternative 2 (idiomatischer)
                        inputStream.copyTo(outputStream)
                    }
                    photo = targetFile
                }
            } catch (e: FileNotFoundException) {
                Log.e("PhotoSaverRepository", "File not found for URI: $uri", e)
                targetFile.delete() // Versuch aufzuräumen
            } catch (e: IOException) {
                Log.e("PhotoSaverRepository", "Error caching photo from URI: $uri", e)
                targetFile.delete() // Versuch aufzuräumen
            }
        }
    }

    suspend fun removeFile(): Boolean {
        val photoFile = photo
        return withContext(Dispatchers.IO) {
            try {
                if (photoFile != null && photoFile.exists()) {
                    val deleted = photoFile.delete()
                    if (deleted) {
                        photo = null
                    }
                    deleted
                } else {
                    // File doesn't exist or photo is null, consider this a success
                    photo = null // Still clear photo if it was already null or non-existent
                    true
                }
            } catch (e: IOException) {
                Log.e("PhotoSaverRepository", "Error deleting file: ${photoFile?.absolutePath}", e)
                false
            } catch (e: SecurityException) {
                Log.e("PhotoSaverRepository", "Security error deleting file: ${photoFile?.absolutePath}", e)
                // Handle potential security exceptions if file permissions are an issue
                false
            }
        }
    }

    suspend fun savePhoto(): File? {
        return withContext(Dispatchers.IO) {
            val cachedFileToSave = photo
            if (cachedFileToSave == null || !cachedFileToSave.exists()) {
                Log.w("PhotoSaverRepository", "No valid cache file to save.")
                photo = null
                return@withContext null
            }

            try {
                val targetFile = File(photoFolder, "${System.currentTimeMillis()}.jpg") // photoFolder ist filesDir/photos
                val savedPersistentFile = cachedFileToSave.copyTo(targetFile, overwrite = true)
                Log.d("PhotoSaverRepository", "File saved persistently at: ${savedPersistentFile.absolutePath}")
                cachedFileToSave.delete()
                photo = null
                return@withContext savedPersistentFile
            } catch (e: Exception) {
                Log.e("PhotoSaverRepository", "Error saving file from cache to persistent storage", e)
                // Optional: Versuchen, die (möglicherweise unvollständige) Zieldatei zu löschen, wenn ein Fehler auftritt
                // targetFile.delete() // Benötigt, dass targetFile außerhalb des try deklariert ist oder hier neu erstellt wird.
                return@withContext null
            }
        }
    }
}