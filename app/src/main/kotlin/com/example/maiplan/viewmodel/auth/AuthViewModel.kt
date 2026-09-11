package com.example.maiplan.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.network.api.UserLoginRequest
import com.example.maiplan.network.api.UserRegisterRequest
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {
    private var registerJob: Job? = null
    private val _registerResult = MutableLiveData<Result<UserEntity>>()
    val registerResult: LiveData<Result<UserEntity>> get() = _registerResult

    private var loginJob: Job? = null
    private val _loginResult = MutableLiveData<Result<UserEntity>>()
    val loginResult: LiveData<Result<UserEntity>> get() = _loginResult

    private var sessionRefreshJob: Job? = null
    private val _sessionRefreshResult = MutableLiveData<Result<UserEntity>>()
    val sessionRefreshResult: LiveData<Result<UserEntity>> get() = _sessionRefreshResult

    private var cachedSessionJob: Job? = null
    private val _cachedSessionUser = MutableLiveData<Result<UserEntity?>>()
    val cachedSessionUser: LiveData<Result<UserEntity?>> get() = _cachedSessionUser

    init {
        clearErrors()
    }

    fun register(user: UserRegisterRequest) {
        cancelRegister()

        registerJob = viewModelScope.launch {
            _registerResult.value = Result.Loading
            _registerResult.value = authRepo.register(user)
        }
    }

    fun login(request: UserLoginRequest) {
        cancelLogin()

        loginJob = viewModelScope.launch {
            _loginResult.value = Result.Loading
            _loginResult.value = authRepo.login(request)
        }
    }

    fun loadCachedSessionUser() {
        cachedSessionJob?.cancel()
        cachedSessionJob = viewModelScope.launch {
            _cachedSessionUser.value = Result.Loading
            _cachedSessionUser.value = authRepo.getCachedSessionUser()
        }
    }

    fun refreshSession() {
        sessionRefreshJob?.cancel()
        sessionRefreshJob = viewModelScope.launch {
            _sessionRefreshResult.value = Result.Loading
            _sessionRefreshResult.value = authRepo.refreshSession()
        }
    }

    fun clearErrors() {
        _loginResult.value = Result.Idle
        _registerResult.value = Result.Idle
    }

    fun cancelRegister() {
        registerJob?.cancel()
        resetRegisterResult()
    }

    fun resetRegisterResult() {
        _registerResult.value = Result.Idle
    }

    fun cancelLogin() {
        loginJob?.cancel()
        resetLoginResult()
    }

    fun resetLoginResult() {
        _loginResult.value = Result.Idle
    }
}
