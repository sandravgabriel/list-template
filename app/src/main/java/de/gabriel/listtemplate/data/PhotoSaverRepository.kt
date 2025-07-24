package de.gabriel.listtemplate.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PhotoSaverRepository(context: Context, private val contentResolver: ContentResolver) {

    private val _photos = mutableListOf<File>()

    fun getPhotos() = _photos.toList()
    fun isEmpty() = _photos.isEmpty()

    private val cacheFolder = File(context.cacheDir, "photos").also { it.mkdir() }
    val photoFolder = File(context.filesDir, "photos").also { it.mkdir() }

    private fun generateFileName() = "${System.currentTimeMillis()}.jpg"
    private fun generatePhotoLogFile() = File(photoFolder, generateFileName())
    fun generatePhotoCacheFile() = File(cacheFolder, generateFileName())

    fun cacheCapturedPhoto(photo: File) {
        _photos += photo
    }

    suspend fun cacheFromUri(uri: Uri) {
        withContext(Dispatchers.IO) {

            contentResolver.openInputStream(uri)?.use { input ->
                val cachedPhoto = generatePhotoCacheFile()

                cachedPhoto.outputStream().use { output ->
                    input.copyTo(output)
                    _photos += cachedPhoto
                }
            }
        }
    }

    suspend fun cacheFromUris(uris: List<Uri>) {
        uris.forEach {
            cacheFromUri(it)
        }
    }

    suspend fun removeFile(photo: File) {
        withContext(Dispatchers.IO) {
            photo.delete()
            _photos -= photo
        }
    }

    suspend fun savePhotos(): List<File> {
        return withContext(Dispatchers.IO) {
            val savedPhotos = _photos.map { it.copyTo(generatePhotoLogFile()) }

            _photos.forEach { it.delete() }
            _photos.clear()

            savedPhotos
        }
    }
}