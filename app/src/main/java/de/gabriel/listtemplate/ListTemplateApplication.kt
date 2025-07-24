package de.gabriel.listtemplate

import android.app.Application
import de.gabriel.listtemplate.data.AppContainer
import de.gabriel.listtemplate.data.AppDataContainer
import de.gabriel.listtemplate.data.PhotoSaverRepository

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
