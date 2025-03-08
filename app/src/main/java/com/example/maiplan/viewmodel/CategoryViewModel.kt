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

    private val _updateCategoryResult = MutableLiveData<Result<Unit>>()
    val updateCategoryResult: LiveData<Result<Unit>> get() = _updateCategoryResult

    private val _deleteCategoryResult = MutableLiveData<Result<Unit>>()
    val deleteCategoryResult: LiveData<Result<Unit>> get() = _deleteCategoryResult

    private val _isCreateCategoryScreen = MutableStateFlow(false)
    val isCreateCategoryScreen: StateFlow<Boolean> = _isCreateCategoryScreen.asStateFlow()

    private var _isUpdateCategoryScreen = MutableStateFlow(false)
    val isUpdateCategoryScreen: StateFlow<Boolean> = _isUpdateCategoryScreen.asStateFlow()

    init {
        clearErrors()
    }

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

    fun updateCategory(category: CategoryResponse, userId: Int) {
        viewModelScope.launch {
            _updateCategoryResult.postValue(categoryRepository.updateCategory(category))
            getAllCategories(userId)
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            _deleteCategoryResult.postValue(categoryRepository.deleteCategory(categoryId))
        }
    }

    fun handleAddCategoryClicked() {
        _isCreateCategoryScreen.value = true
    }

    fun resetCreateCategoryScreen() {
        _isCreateCategoryScreen.value = false
    }

    fun handleUpdateCategoryClicked() {
        _isUpdateCategoryScreen.value = true
    }

    fun resetUpdateCategoryScreen() {
        _isUpdateCategoryScreen.value = false
    }

    fun clearErrors() {
        _createCategoryResult.postValue(Result.Idle)
        _updateCategoryResult.postValue(Result.Idle)
    }
}