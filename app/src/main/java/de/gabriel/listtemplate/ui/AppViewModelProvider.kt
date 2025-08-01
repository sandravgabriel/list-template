package de.gabriel.listtemplate.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.gabriel.listtemplate.ListTemplateApplication
import de.gabriel.listtemplate.ui.item.ItemDetailsViewModel
import de.gabriel.listtemplate.ui.item.ItemEditViewModel
import de.gabriel.listtemplate.ui.item.ItemEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                templateApplication().container.itemsRepository,
                templateApplication().photoSaver
            )
        }

        initializer {
            ItemEntryViewModel(
                templateApplication().container.itemsRepository,
                templateApplication().photoSaver,
            )
        }

        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                templateApplication().container.itemsRepository,
                templateApplication().photoSaver
            )
        }

        initializer {
            ItemEditViewModel(
                this.createSavedStateHandle(),
                templateApplication().container.itemsRepository,
                templateApplication().photoSaver
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [ListTemplateApplication].
 */
fun CreationExtras.templateApplication(): ListTemplateApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ListTemplateApplication)
