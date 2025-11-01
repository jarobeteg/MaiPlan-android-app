package com.example.maiplan.viewmodel.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.NetworkChecker
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryRepository
import com.example.maiplan.repository.orEmptyList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepo: CategoryRepository,
    private val networkChecker: NetworkChecker
) : ViewModel() {
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
            _createCategoryResult.postValue(Result.Loading)
            val result = categoryRepo.createCategory(category)
            _createCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(category.userId)
        }
    }

    fun getAllCategories(userId: Int) {
        viewModelScope.launch {
            val result = categoryRepo.getAllCategories(userId)
            _categoryList.postValue(result.orEmptyList())
        }
    }

    fun getCategory(categoryId: Int): CategoryResponse {
        return _categoryList.value!!.find { it.categoryId == categoryId }!!
    }

    fun updateCategory(category: CategoryResponse, userId: Int) {
        viewModelScope.launch {
            _updateCategoryResult.postValue(Result.Loading)
            val result = categoryRepo.updateCategory(category, userId)
            _updateCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(userId)
        }
    }

    fun softDeleteCategory(categoryId: Int, userId: Int) {
        viewModelScope.launch {
            val result = categoryRepo.softDeleteCategory(categoryId, userId)
            _deleteCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(userId)
        }
    }

    fun clearCreateResult() {
        _createCategoryResult.postValue(Result.Idle)
    }

    fun clearUpdateResult() {
        _updateCategoryResult.postValue(Result.Idle)
    }

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