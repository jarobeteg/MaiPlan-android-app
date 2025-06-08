package com.example.maiplan.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AuthRepository
import kotlinx.coroutines.launch

/**
 * [AuthViewModel] manages user authentication-related operations
 * and exposes their results to the UI through [androidx.lifecycle.LiveData].
 *
 * ## Responsibilities:
 * - Handling user registration, login, password reset, token refresh, and profile retrieval.
 * - Managing the UI state (success, loading, error) for each operation using a [com.example.maiplan.repository.Result] wrapper.
 * - Clearing previous error or idle states when needed.
 *
 * @property authRepository The repository responsible for making authentication API calls.
 *
 * @see com.example.maiplan.repository.auth.AuthRepository
 * @see com.example.maiplan.repository.Result
 * @see com.example.maiplan.network.api.Token
 * @see com.example.maiplan.network.api.UserResponse
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _registerResult = MutableLiveData<Result<Token>>()
    /** Exposes the registration operation result. */
    val registerResult: LiveData<Result<Token>> get() = _registerResult

    private val _loginResult = MutableLiveData<Result<Token>>()
    /** Exposes the login operation result. */
    val loginResult: LiveData<Result<Token>> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Result<Token>>()
    /** Exposes the password reset operation result. */
    val resetPasswordResult: LiveData<Result<Token>> get() = _resetPasswordResult

    private val _tokenRefreshResult = MutableLiveData<Result<Token>>()
    /** Exposes the token refresh operation result. */
    val tokenRefreshResult: LiveData<Result<Token>> get() = _tokenRefreshResult

    private val _profileResult = MutableLiveData<Result<UserResponse>>()
    /** Exposes the user profile retrieval operation result. */
    val profileResult: LiveData<Result<UserResponse>> get() = _profileResult

    init {
        clearErrors()
    }

    /**
     * Initiates the user registration process.
     *
     * @param user The [com.example.maiplan.network.api.UserRegister] data to register a new user.
     *
     * @see com.example.maiplan.network.api.UserRegister
     */
    fun register(user: UserRegister) {
        viewModelScope.launch {
            _registerResult.postValue(authRepository.register(user))
        }
    }

    /**
     * Initiates the user login process.
     *
     * @param user The [com.example.maiplan.network.api.UserLogin] data containing user credentials.
     *
     * @see com.example.maiplan.network.api.UserLogin
     */
    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.postValue(authRepository.login(user))
        }
    }

    /**
     * Initiates the password reset process.
     *
     * @param user The [com.example.maiplan.network.api.UserResetPassword] data containing reset information.
     *
     * @see com.example.maiplan.network.api.UserResetPassword
     */
    fun resetPassword(user: UserResetPassword) {
        viewModelScope.launch {
            _resetPasswordResult.postValue(authRepository.resetPassword(user))
        }
    }

    /**
     * Requests a new token using the existing token.
     *
     * @param token The current expired or near-expiration token.
     */
    fun tokenRefresh(token: String) {
        viewModelScope.launch {
            _tokenRefreshResult.postValue(authRepository.tokenRefresh(token))
        }
    }

    /**
     * Fetches the user profile using a valid token.
     *
     * @param token The authentication token to access profile information.
     */
    fun getProfile(token: String) {
        viewModelScope.launch {
            val result = authRepository.getProfile(token)
            _profileResult.postValue(result)
        }
    }

    /**
     * Resets the error states for registration, login, and password reset operations.
     * Useful for clearing previous errors before new form submissions.
     */
    fun clearErrors() {
        _loginResult.postValue(Result.Idle)
        _registerResult.postValue(Result.Idle)
        _resetPasswordResult.postValue(Result.Idle)
    }
}