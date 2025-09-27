package com.example.maiplan.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.network.api.AuthResponse
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AuthRepository
import com.example.maiplan.utils.common.PasswordUtils
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepo: AuthRepository) : ViewModel() {

    private val _syncResult = MutableLiveData<Result<Unit>>()
    val syncResult: LiveData<Result<Unit>> = _syncResult

    private val _localRegisterResult = MutableLiveData<String> ()
    val localRegisterResult: LiveData<String> get() = _localRegisterResult

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> get() = _registerResult

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Result<AuthResponse>>()
    val resetPasswordResult: LiveData<Result<AuthResponse>> get() = _resetPasswordResult

    private val _tokenRefreshResult = MutableLiveData<Result<Token>>()
    val tokenRefreshResult: LiveData<Result<Token>> get() = _tokenRefreshResult

    private val _profileResult = MutableLiveData<Result<AuthResponse>>()
    val profileResult: LiveData<Result<AuthResponse>> get() = _profileResult

    init {
        clearErrors()
    }

    fun localRegister(user: AuthEntity) {
        viewModelScope.launch {
            authRepo.localRegister(user)
        }
    }

    fun sync() {
        viewModelScope.launch {
            authRepo.sync()
        }
    }

    fun register(user: UserRegister) {
        viewModelScope.launch {
            val result = authRepo.register(user)
            _registerResult.postValue(authRepo.register(user))
            if (result is Result.Success) {
                val authEntity = AuthEntity(
                    email = user.email,
                    username = user.username,
                    passwordHash = PasswordUtils.hashPassword(user.password),
                    syncState = 4
                )
                localRegister(authEntity)
            }
            _registerResult.postValue(result)
        }
    }

    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.postValue(authRepo.login(user))
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