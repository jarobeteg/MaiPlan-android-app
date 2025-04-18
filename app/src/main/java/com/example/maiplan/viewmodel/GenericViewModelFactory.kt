package com.example.maiplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * [GenericViewModelFactory] is a reusable ViewModel factory
 * for creating ViewModels with a custom creator function.
 *
 * This allows injecting dependencies into ViewModels, in our case these are the repositories.
 *
 * @param T The type of ViewModel to create.
 * @property creator A function that returns an instance of the ViewModel.
 */
class GenericViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(creator()::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return creator() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
