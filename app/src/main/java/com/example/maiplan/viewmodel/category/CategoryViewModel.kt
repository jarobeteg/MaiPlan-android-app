package com.example.maiplan.viewmodel.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.api.CategoryCreate
import com.example.maiplan.network.api.CategoryResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.category.CategoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * [CategoryViewModel] handles operations related to category management,
 * such as creating, retrieving, updating, and deleting categories.
 *
 * It also manages UI states for form operations and navigation flags
 * needed for smooth category editing interactions.
 *
 * ## Responsibilities:
 * - Creating, fetching, updating, and deleting categories via [com.example.maiplan.repository.category.CategoryRepository].
 * - Exposing operation results through [androidx.lifecycle.LiveData] to update the UI.
 * - Managing a temporary navigation flag to handle swipe-to-edit behaviors.
 *
 * @property categoryRepository The repository responsible for category-related API calls.
 *
 * @see com.example.maiplan.repository.category.CategoryRepository
 * @see com.example.maiplan.repository.Result
 * @see com.example.maiplan.network.api.CategoryResponse
 */
class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _createCategoryResult = MutableLiveData<Result<Unit>>()
    /** Exposes the result of creating a new category. */
    val createCategoryResult: LiveData<Result<Unit>> get() = _createCategoryResult

    private var _categoryList = MutableLiveData<List<CategoryResponse>>()
    /** Exposes the current list of categories. */
    val categoryList: LiveData<List<CategoryResponse>> get() = _categoryList

    private val _updateCategoryResult = MutableLiveData<Result<Unit>>()
    /** Exposes the result of updating a category. */
    val updateCategoryResult: LiveData<Result<Unit>> get() = _updateCategoryResult

    private val _deleteCategoryResult = MutableLiveData<Result<Unit>>()
    /** Exposes the result of deleting a category. */
    val deleteCategoryResult: LiveData<Result<Unit>> get() = _deleteCategoryResult

    private val _isNavigating = MutableLiveData(false)
    /** Indicates whether a swipe navigation action is currently in progress. */
    val isNavigating: LiveData<Boolean> get() = _isNavigating

    init {
        clearErrors()
    }

    /**
     * Creates a new category and refreshes the category list on Success.
     *
     * @param category The [com.example.maiplan.network.api.CategoryCreate] object containing the new category's data.
     *
     * @see com.example.maiplan.network.api.CategoryCreate
     */
    fun createCategory(category: CategoryCreate) {
        viewModelScope.launch {
            val result = categoryRepository.createCategory(category)
            _createCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(category.userId)
        }
    }

    /**
     * Fetches all categories for a given user.
     *
     * @param userId The Id of the user whose categories should be fetched.
     */
    fun getAllCategories(userId: Int) {
        viewModelScope.launch {
            when (val result = categoryRepository.getAllCategories(userId)) {
                is Result.Success -> _categoryList.postValue(result.data)
                else -> _categoryList.postValue(emptyList())
            }
        }
    }

    /**
     * Fetches a single category by its Id from the local category list.
     *
     * @param categoryId The Id of the category to find.
     * @return The [CategoryResponse] corresponding to the given Id.
     */
    fun getCategoryById(categoryId: Int): CategoryResponse {
        return _categoryList.value!!.find { it.categoryId == categoryId }!!
    }

    /**
     * Updates an existing category and refreshes the category list on Success.
     *
     * @param category The updated [CategoryResponse] object.
     * @param userId The Id of the user owning the category.
     *
     * @see CategoryResponse
     */
    fun updateCategory(category: CategoryResponse, userId: Int) {
        viewModelScope.launch {
            val result = categoryRepository.updateCategory(category)
            _updateCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(userId)
        }
    }

    /**
     * Deletes a category by its Id and refreshes the category list on Success.
     *
     * @param categoryId The Id of the category to delete.
     * @param userId The Id of the user owning the category.
     */
    fun deleteCategory(categoryId: Int, userId: Int) {
        viewModelScope.launch {
            val result = categoryRepository.deleteCategory(categoryId)
            _deleteCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(userId)
        }
    }

    /**
     * Resets the create category result to idle.
     */
    fun clearCreateResult() {
        _createCategoryResult.postValue(Result.Idle)
    }

    /**
     * Resets the update category result to idle.
     */
    fun clearUpdateResult() {
        _updateCategoryResult.postValue(Result.Idle)
    }

    /**
     * Starts a navigation action by setting the navigation flag to true.
     *
     * This is used as a workaround to prevent double navigation events
     * when using swipe to edit functionality on the category list.
     */
    fun startNavigation() {
        _isNavigating.value = true
    }

    /**
     * Resets the navigation flag after a short delay.
     *
     * This prevents duplicate navigation events that could cause the
     * [UpdateCategoryScreen] to open twice and create flickering.
     */
    fun resetNavigation() {
        viewModelScope.launch {
            delay(500)
            _isNavigating.value = false
        }
    }

    /**
     * Clears previous error or idle states for create and update operations.
     */
    fun clearErrors() {
        _createCategoryResult.postValue(Result.Idle)
        _updateCategoryResult.postValue(Result.Idle)
    }
}