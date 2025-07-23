package de.gabriel.template

import android.app.Application
import de.gabriel.template.data.AppContainer
import de.gabriel.template.data.AppDataContainer
import de.gabriel.template.data.PhotoSaverRepository

class ListTemplateApplication : Application() {

    /**
     * AppContainer instance used to obtain dependencies
     */
    lateinit var container: AppContainer
    lateinit var photoSaver: PhotoSaverRepository

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        photoSaver = PhotoSaverRepository(this, this.contentResolver)
    }
}
