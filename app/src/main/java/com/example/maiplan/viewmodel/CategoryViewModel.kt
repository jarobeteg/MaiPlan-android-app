package com.example.maiplan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.maiplan.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private var _isCreateCategoryScreen = MutableStateFlow(false)
    val isCreateCategoryScreen: StateFlow<Boolean> = _isCreateCategoryScreen.asStateFlow()

    fun handleAddCategoryClicked() {
        _isCreateCategoryScreen.value = true
    }

    fun resetCreateCategoryScreen() {
        _isCreateCategoryScreen.value = false
    }
}