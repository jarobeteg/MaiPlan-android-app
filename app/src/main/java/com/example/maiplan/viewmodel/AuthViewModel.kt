package com.example.maiplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.Token
import com.example.maiplan.network.UserRegister
import com.example.maiplan.network.UserLogin
import com.example.maiplan.network.UserResetPassword
import com.example.maiplan.network.UserResponse
import com.example.maiplan.repository.AuthRepository
import com.example.maiplan.repository.Result
import kotlinx.coroutines.launch

/**
 * [AuthViewModel] manages user authentication-related operations
 * and exposes their results to the UI through [LiveData].
 *
 * ## Responsibilities:
 * - Handling user registration, login, password reset, token refresh, and profile retrieval.
 * - Managing the UI state (success, loading, error) for each operation using a [Result] wrapper.
 * - Clearing previous error or idle states when needed.
 *
 * @property authRepository The repository responsible for making authentication API calls.
 *
 * @see AuthRepository
 * @see Result
 * @see Token
 * @see UserResponse
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

    private val _profileResult = MutableLiveData<Result<UserResponse>> ()
    /** Exposes the user profile retrieval operation result. */
    val profileResult: LiveData<Result<UserResponse>> get() = _profileResult

    init {
        clearErrors()
    }

    /**
     * Initiates the user registration process.
     *
     * @param user The [UserRegister] data to register a new user.
     *
     * @see UserRegister
     */
    fun register(user: UserRegister) {
        viewModelScope.launch {
            _registerResult.postValue(authRepository.register(user))
        }
    }

    /**
     * Initiates the user login process.
     *
     * @param user The [UserLogin] data containing user credentials.
     *
     * @see UserLogin
     */
    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.postValue(authRepository.login(user))
        }
    }

    /**
     * Initiates the password reset process.
     *
     * @param user The [UserResetPassword] data containing reset information.
     *
     * @see UserResetPassword
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