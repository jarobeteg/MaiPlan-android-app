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
 * [CategoryViewModel] manages all category-related operations and UI state updates
 * by interacting with [CategoryRepository]. It serves as the bridge between the
 * repository layer and UI, exposing results via [LiveData].
 *
 * ## Responsibilities:
 * - Creating, retrieving, updating, and deleting categories.
 * - Managing success/error/loading states for each operation.
 * - Supporting navigation behavior for edit workflows (e.g., swipe-to-edit).
 *
 * @property categoryRepository The repository responsible for performing category API requests.
 *
 * @see CategoryRepository
 * @see Result
 * @see CategoryResponse
 */
class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _createCategoryResult = MutableLiveData<Result<Unit>>()
    /** Emits the result of the create category operation. */
    val createCategoryResult: LiveData<Result<Unit>> get() = _createCategoryResult

    private var _categoryList = MutableLiveData<List<CategoryResponse>>()
    /** Emits the current list of categories retrieved from the server. */
    val categoryList: LiveData<List<CategoryResponse>> get() = _categoryList

    private val _updateCategoryResult = MutableLiveData<Result<Unit>>()
    /** Emits the result of the update category operation. */
    val updateCategoryResult: LiveData<Result<Unit>> get() = _updateCategoryResult

    private val _deleteCategoryResult = MutableLiveData<Result<Unit>>()
    /** Emits the result of the delete category operation. */
    val deleteCategoryResult: LiveData<Result<Unit>> get() = _deleteCategoryResult

    private val _isNavigating = MutableLiveData(false)
    /** Tracks whether a navigation action (e.g., swipe-to-edit) is currently in progress. */
    val isNavigating: LiveData<Boolean> get() = _isNavigating

    init {
        clearErrors()
    }

    /**
     * Creates a new category using the provided data.
     * On success, refreshes the category list for the user.
     *
     * @param category The [CategoryCreate] data for the new category.
     */
    fun createCategory(category: CategoryCreate) {
        viewModelScope.launch {
            val result = categoryRepository.createCategory(category)
            _createCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(category.userId)
        }
    }

    /**
     * Fetches all categories associated with the specified user.
     *
     * @param userId The ID of the user whose categories will be fetched.
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
     * Retrieves a specific category by its ID from the currently loaded category list.
     *
     * This function assumes that the category list has already been populated and that
     * the requested category ID exists in the list. A null assertion is used because
     * under normal application flow, this method should never be called unless data is present.
     *
     * @param categoryId The ID of the category to retrieve.
     * @return The matching [CategoryResponse] object.
     * @throws NullPointerException if the category list is null or the ID is not found — though this should never occur in expected usage, hence the assertion.
     */
    fun getCategoryById(categoryId: Int): CategoryResponse {
        return _categoryList.value!!.find { it.categoryId == categoryId }!!
    }

    /**
     * Updates a category with new data.
     * On success, refreshes the user's category list.
     *
     * @param category The updated category data.
     * @param userId The ID of the user to fetch updated categories for.
     */
    fun updateCategory(category: CategoryResponse, userId: Int) {
        viewModelScope.launch {
            val result = categoryRepository.updateCategory(category)
            _updateCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(userId)
        }
    }

    /**
     * Deletes a category by its ID.
     * On success, refreshes the user's category list.
     *
     * @param categoryId The ID of the category to delete.
     * @param userId The ID of the user to fetch updated categories for.
     */
    fun deleteCategory(categoryId: Int, userId: Int) {
        viewModelScope.launch {
            val result = categoryRepository.deleteCategory(categoryId)
            _deleteCategoryResult.postValue(result)
            if (result is Result.Success) getAllCategories(userId)
        }
    }

    /**
     * Clears the result state for category creation operations.
     * Useful before re-submitting a form.
     */
    fun clearCreateResult() {
        _createCategoryResult.postValue(Result.Idle)
    }

    /**
     * Clears the result state for category update operations.
     */
    fun clearUpdateResult() {
        _updateCategoryResult.postValue(Result.Idle)
    }

    /**
     * Initiates a navigation flag, typically used to prevent duplicate
     * navigation's (e.g., during swipe-to-edit).
     */
    fun startNavigation() {
        _isNavigating.value = true
    }

    /**
     * Resets the navigation flag after a short delay to prevent double-triggering.
     * Helps avoid screen flickering or duplicate navigation events.
     */
    fun resetNavigation() {
        viewModelScope.launch {
            delay(500)
            _isNavigating.value = false
        }
    }

    /**
     * Clears idle/error states for both create and update operations.
     * Useful when resetting UI between screens or actions.
     */
    fun clearErrors() {
        _createCategoryResult.postValue(Result.Idle)
        _updateCategoryResult.postValue(Result.Idle)
    }
}