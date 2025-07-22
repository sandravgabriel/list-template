package de.gabriel.template

import android.app.Application
import de.gabriel.template.data.AppContainer
import de.gabriel.template.data.AppDataContainer

class ListTemplateApplication : Application() {

    /**
     * AppContainer instance used to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
