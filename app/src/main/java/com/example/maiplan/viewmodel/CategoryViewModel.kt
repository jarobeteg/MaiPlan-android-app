package com.example.maiplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.CategoryResponse
import com.example.maiplan.repository.CategoryRepository
import com.example.maiplan.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _createCategoryResult = MutableLiveData<Result<Unit>>()
    val createCategoryResult: LiveData<Result<Unit>> get() = _createCategoryResult

    private var _categoryList = MutableLiveData<List<CategoryResponse>>()
    val categoryList: LiveData<List<CategoryResponse>> get() = _categoryList

    private var _isCreateCategoryScreen = MutableStateFlow(false)
    val isCreateCategoryScreen: StateFlow<Boolean> = _isCreateCategoryScreen.asStateFlow()

    fun createCategory(category: CategoryCreate) {
        viewModelScope.launch {
            _createCategoryResult.postValue(categoryRepository.createCategory(category))
            getAllCategories(category.userId)
        }
    }

    fun getAllCategories(userId: Int) {
        viewModelScope.launch {
            when (val result = categoryRepository.getAllCategories(userId)) {
                is Result.Success -> _categoryList.postValue(result.data)
                else -> _categoryList.postValue(emptyList())
            }
        }
    }

    fun handleAddCategoryClicked() {
        _isCreateCategoryScreen.value = true
    }

    fun resetCreateCategoryScreen() {
        _isCreateCategoryScreen.value = false
    }
}