package com.example.maiplan.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AuthRepository
import com.example.maiplan.utils.common.JWTUtils
import kotlinx.coroutines.launch

/**
 * [AuthViewModel] handles user authentication and exposes results to the UI
 * through [LiveData] wrapped in [Result] objects.
 *
 * ## Responsibilities:
 * - User registration, login, and password reset.
 * - Token refresh and profile retrieval.
 * - Managing loading, success, error, and idle states per operation.
 * - Clearing previous states before new operations.
 *
 * Acts as a bridge between the UI and [AuthRepository], ensuring proper threading via [viewModelScope].
 *
 * @property authRepository The repository responsible for making authentication API calls.
 *
 * @see AuthRepository
 * @see Result
 * @see Token
 * @see UserResponse
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _localRegisterResult = MutableLiveData<String> ()

    val localRegisterResult: LiveData<String> get() = _localRegisterResult

    private val _registerResult = MutableLiveData<Result<Token>>()
    /** Emits the result of a user registration request. */
    val registerResult: LiveData<Result<Token>> get() = _registerResult

    private val _loginResult = MutableLiveData<Result<Token>>()
    /** Emits the result of a user login request. */
    val loginResult: LiveData<Result<Token>> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Result<Token>>()
    /** Emits the result of a password reset request. */
    val resetPasswordResult: LiveData<Result<Token>> get() = _resetPasswordResult

    private val _tokenRefreshResult = MutableLiveData<Result<Token>>()
    /** Emits the result of a token refresh operation. */
    val tokenRefreshResult: LiveData<Result<Token>> get() = _tokenRefreshResult

    private val _profileResult = MutableLiveData<Result<UserResponse>>()
    /** Emits the result of fetching the user profile. */
    val profileResult: LiveData<Result<UserResponse>> get() = _profileResult

    init {
        clearErrors()
    }

    fun localRegister(user: AuthEntity) {
        viewModelScope.launch {
            val token = JWTUtils.createAccessToken(authRepository.localRegister(user))
            _localRegisterResult.postValue(token)
        }
    }

    /**
     * Registers a new user.
     *
     * @param user The registration data provided via [UserRegister].
     * @see UserRegister
     */
    fun register(user: UserRegister) {
        viewModelScope.launch {
            _registerResult.postValue(authRepository.register(user))
        }
    }

    /**
     * Logs in a user with credentials.
     *
     * @param user The login data provided via [UserLogin].
     * @see UserLogin
     */
    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.postValue(authRepository.login(user))
        }
    }

    /**
     * Initiates password reset for a user.
     *
     * @param user The password reset data provided via [UserResetPassword].
     * @see UserResetPassword
     */
    fun resetPassword(user: UserResetPassword) {
        viewModelScope.launch {
            _resetPasswordResult.postValue(authRepository.resetPassword(user))
        }
    }

    /**
     * Requests a refreshed authentication token.
     *
     * @param token The current (expired or soon-to-expire) token.
     */
    fun tokenRefresh(token: String) {
        viewModelScope.launch {
            _tokenRefreshResult.postValue(authRepository.tokenRefresh(token))
        }
    }

    /**
     * Retrieves the current user's profile.
     *
     * @param token A valid authentication token.
     */
    fun getProfile(token: String) {
        viewModelScope.launch {
            val result = authRepository.getProfile(token)
            _profileResult.postValue(result)
        }
    }

    /**
     * Clears existing login, register, and reset error states by resetting them to [Result.Idle].
     * This is typically called during screen initialization or before submitting a new request.
     */
    fun clearErrors() {
        _loginResult.postValue(Result.Idle)
        _registerResult.postValue(Result.Idle)
        _resetPasswordResult.postValue(Result.Idle)
    }
}