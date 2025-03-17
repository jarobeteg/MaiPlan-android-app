package com.example.maiplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.CategoryCreate
import com.example.maiplan.network.CategoryResponse
import com.example.maiplan.repository.CategoryRepository
import com.example.maiplan.repository.Result
import kotlinx.coroutines.delay
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

    private val _isNavigating = MutableLiveData(false)
    val isNavigating: LiveData<Boolean> get() = _isNavigating

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

    fun getCategoryById(categoryId: Int): CategoryResponse {
        return _categoryList.value!!.find { it.categoryId == categoryId }!!
    }

    fun updateCategory(category: CategoryResponse, userId: Int) {
        viewModelScope.launch {
            _updateCategoryResult.postValue(categoryRepository.updateCategory(category))
            getAllCategories(userId)
        }
    }

    fun deleteCategory(categoryId: Int, userId: Int) {
        viewModelScope.launch {
            _deleteCategoryResult.postValue(categoryRepository.deleteCategory(categoryId))
            getAllCategories(userId)
        }
    }

    /*
    navigating functions are important for the swipe to edit functionality in category management
    otherwise something causes the UpdateCategoryScreen to open twice and the UI would flicker
    this is a temporary solution until I can't find something better, hopefully
    */
    fun startNavigation() {
        _isNavigating.value = true
    }

    fun resetNavigation() {
        viewModelScope.launch {
            delay(500)
            _isNavigating.value = false
        }
    }

    fun clearErrors() {
        _createCategoryResult.postValue(Result.Idle)
        _updateCategoryResult.postValue(Result.Idle)
    }
}