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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepo: AuthRepository) : ViewModel() {
    private val _isServerReachable = MutableSharedFlow<Boolean>(replay = 1)
    val isServerReachable: SharedFlow<Boolean> = _isServerReachable.asSharedFlow()
    private var pollingJob: Job? = null

    // not in use
    private val _syncResult = MutableLiveData<Result<Unit>>()
    val syncResult: LiveData<Result<Unit>> = _syncResult

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> get() = _registerResult

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> get() = _loginResult

    private val _localLoginResult = MutableLiveData<AuthEntityResponse>()
    val localLoginResult: LiveData<AuthEntityResponse> get() = _localLoginResult

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

    fun startNetworkMonitoring(networkChecker: NetworkChecker) {
        viewModelScope.launch {
            networkChecker.observeNetworkStatus().collect { isOnline ->
                if (isOnline) {
                    startReachabilityPolling(networkChecker)
                } else {
                    _isServerReachable.emit(false)
                }
            }
        }
    }

    private fun startReachabilityPolling(networkChecker: NetworkChecker) {
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val reachable = networkChecker.canReachServer()
                _isServerReachable.emit(reachable)
                delay(2500)
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            authRepo.sync()
        }
    }

    fun register(user: UserRegister) {
        viewModelScope.launch {
            _registerResult.postValue(authRepo.register(user))
        }
    }

    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.postValue(authRepo.login(user))
        }
    }

    fun localLogin(user: UserLogin) {
        viewModelScope.launch {
            _localLoginResult.postValue(authRepo.localLogin(user))
        }
    }

    fun resetPassword(user: UserResetPassword) {
        viewModelScope.launch {
            _resetPasswordResult.postValue(authRepo.resetPassword(user))
        }
    }

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
}