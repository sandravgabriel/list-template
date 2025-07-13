package de.gabriel.template.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.gabriel.template.TemplateApplication
import de.gabriel.template.ui.home.HomeViewModel
import de.gabriel.template.ui.item.ItemDetailsViewModel
import de.gabriel.template.ui.item.ItemEditViewModel
import de.gabriel.template.ui.item.ItemEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ItemEditViewModel(
                this.createSavedStateHandle(),
                //listRandomizerApplication().container.itemsRepository
            )
        }
        initializer {
            //ItemEntryViewModel(listRandomizerApplication().container.itemsRepository)
            ItemEntryViewModel()
        }

        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                //listRandomizerApplication().container.itemsRepository
            )
        }

        initializer {
            HomeViewModel()
        }

        /**initializer {
            HomeViewModel(listRandomizerApplication().container.itemsRepository)
        }
        **/
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [TemplateApplication].
 */
fun CreationExtras.listRandomizerApplication(): TemplateApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as TemplateApplication)
