package com.example.maiplan.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.AuthEntityResponse
import com.example.maiplan.network.NetworkChecker
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val networkChecker: NetworkChecker
) : ViewModel() {
    private val _showServerUnreachableToast = MutableSharedFlow<Unit>()
    val showServerUnreachableToast: SharedFlow<Unit> get() = _showServerUnreachableToast

    private val _showLocalLoginToast = MutableSharedFlow<Unit>()
    val showLocalLoginToast: SharedFlow<Unit> get() = _showLocalLoginToast

    // not in use
    private val _syncResult = MutableLiveData<Result<Unit>>()
    val syncResult: LiveData<Result<Unit>> = _syncResult

    private var registerJob: Job? = null
    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> get() = _registerResult

    private var loginJob: Job? = null
    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> get() = _loginResult

    private val _localLoginResult = MutableLiveData<AuthEntityResponse>()
    val localLoginResult: LiveData<AuthEntityResponse> get() = _localLoginResult

    private var resetPasswordJob: Job? = null
    private val _resetPasswordResult = MutableLiveData<Result<AuthResponse>>()
    val resetPasswordResult: LiveData<Result<AuthResponse>> get() = _resetPasswordResult

    // not in use
    private val _tokenRefreshResult = MutableLiveData<Result<Token>>()
    val tokenRefreshResult: LiveData<Result<Token>> get() = _tokenRefreshResult

    private val _profileResult = MutableLiveData<Result<AuthResponse>>()
    val profileResult: LiveData<Result<AuthResponse>> get() = _profileResult

    init {
        clearErrors()
    }

    fun sync() {
        viewModelScope.launch {
            authRepo.sync()
        }
    }

    fun register(user: UserRegister) {
        cancelRegister()
        registerJob = viewModelScope.launch {
            _registerResult.postValue(Result.Loading)
            if (networkChecker.canReachServer()) {
                _registerResult.postValue(authRepo.register(user))
            } else {
                _registerResult.postValue(Result.Idle)
                _showServerUnreachableToast.emit(Unit)
            }
        }
    }

    fun login(user: UserLogin) {
        cancelLogin()
        loginJob = viewModelScope.launch {
            _loginResult.postValue(Result.Loading)
            if (networkChecker.canReachServer()) {
                _loginResult.postValue(authRepo.login(user))
            } else {
                _localLoginResult.postValue(authRepo.localLogin(user))
                _showLocalLoginToast.emit(Unit)
            }
        }
    }

    fun resetPassword(user: UserResetPassword) {
        cancelResetPassword()
        resetPasswordJob = viewModelScope.launch {
            _resetPasswordResult.postValue(Result.Loading)
            if (networkChecker.canReachServer()) {
                _resetPasswordResult.postValue(authRepo.resetPassword(user))
            } else {
                _resetPasswordResult.postValue(Result.Idle)
                _showServerUnreachableToast.emit(Unit)
            }
        }
    }

    // not in use
    fun tokenRefresh(token: String) {
        viewModelScope.launch {
            _tokenRefreshResult.postValue(authRepo.tokenRefresh(token))
        }
    }

    fun getProfile(token: String) {
        viewModelScope.launch {
            _profileResult.postValue(authRepo.getProfile(token))
        }
    }

    fun clearErrors() {
        _loginResult.postValue(Result.Idle)
        _registerResult.postValue(Result.Idle)
        _resetPasswordResult.postValue(Result.Idle)
    }

    fun cancelRegister() {
        registerJob?.cancel()
        resetRegisterResult()
    }

    fun resetRegisterResult() {
        _registerResult.postValue(Result.Idle)
    }

    fun cancelLogin() {
        loginJob?.cancel()
        resetLoginResult()
    }

    fun resetLoginResult() {
        _loginResult.postValue(Result.Idle)
    }

    fun cancelResetPassword() {
        resetPasswordJob?.cancel()
        resetPasswordResult()
    }

    fun resetPasswordResult() {
        _resetPasswordResult.postValue(Result.Idle)
    }
}