package com.example.maiplan.viewmodel.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.maiplan.category.data.CreateCategoryInput
import com.example.maiplan.category.data.UpdateCategoryInput
import com.example.maiplan.database.entities.CategoryEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CategoryViewModel(
    private val categoryRepo: CategoryRepository,
    private val userLocalId: Long
) : ViewModel() {
    val categoryList: LiveData<List<CategoryEntity>> =
        categoryRepo.observeCategories(userLocalId).asLiveData()

    private val _createCategoryResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val createCategoryResult: LiveData<Result<Unit>> = _createCategoryResult

    private val _updateCategoryResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val updateCategoryResult: LiveData<Result<Unit>> = _updateCategoryResult

    private val _deleteCategoryResult = MutableLiveData<Result<Unit>>(Result.Idle)
    val deleteCategoryResult: LiveData<Result<Unit>> = _deleteCategoryResult

    private val _isNavigating = MutableLiveData(false)
    val isNavigating: LiveData<Boolean> = _isNavigating

    fun createCategory(input: CreateCategoryInput) {
        viewModelScope.launch {
            _createCategoryResult.value = Result.Loading
            _createCategoryResult.value = categoryRepo.createCategory(input, userLocalId)
        }
    }

    fun updateCategory(input: UpdateCategoryInput) {
        viewModelScope.launch {
            _updateCategoryResult.value = Result.Loading
            _updateCategoryResult.value = categoryRepo.updateCategory(input, userLocalId)
        }
    }

    fun softDeleteCategory(categoryLocalId: Long) {
        viewModelScope.launch {
            _deleteCategoryResult.value = Result.Loading
            _deleteCategoryResult.value = categoryRepo.softDeleteCategory(categoryLocalId, userLocalId)
        }
    }

    fun getCategory(categoryLocalId: Long): CategoryEntity? {
        return categoryList.value?.find { it.categoryLocalId == categoryLocalId }
    }

    fun clearCreateResult() {
        _createCategoryResult.value = Result.Idle
    }

    fun clearUpdateResult() {
        _updateCategoryResult.value = Result.Idle
    }

    fun clearDeleteResult() {
        _deleteCategoryResult.value = Result.Idle
    }

    fun clearErrors() {
        clearCreateResult()
        clearUpdateResult()
        clearDeleteResult()
    }

    fun startNavigation() {
        _isNavigating.value = true
    }

    fun resetNavigation() {
        viewModelScope.launch {
            delay(500.milliseconds)
            _isNavigating.value = false
        }
    }
}