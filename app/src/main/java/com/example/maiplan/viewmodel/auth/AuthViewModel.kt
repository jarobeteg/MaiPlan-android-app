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
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _syncResult = MutableLiveData<Result<Unit>>()
    val syncResult: LiveData<Result<Unit>> = _syncResult

    private val _localRegisterResult = MutableLiveData<String> ()
    val localRegisterResult: LiveData<String> get() = _localRegisterResult

    private val _registerResult = MutableLiveData<Result<Token>>()
    val registerResult: LiveData<Result<Token>> get() = _registerResult

    private val _loginResult = MutableLiveData<Result<Token>>()
    val loginResult: LiveData<Result<Token>> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Result<Token>>()
    val resetPasswordResult: LiveData<Result<Token>> get() = _resetPasswordResult

    private val _tokenRefreshResult = MutableLiveData<Result<Token>>()
    val tokenRefreshResult: LiveData<Result<Token>> get() = _tokenRefreshResult

    private val _profileResult = MutableLiveData<Result<UserResponse>>()
    val profileResult: LiveData<Result<UserResponse>> get() = _profileResult

    init {
        clearErrors()
    }

    fun localRegister(user: AuthEntity) {
        viewModelScope.launch {}
    }

    fun sync() {}

    fun register(user: UserRegister) {
        viewModelScope.launch {
            _registerResult.postValue(authRepository.register(user))
        }
    }

    fun setRegisterError(failure: Result.Failure) {
        _registerResult.postValue(failure)
    }

    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.postValue(authRepository.login(user))
        }
    }

    fun setLoginError(failure: Result.Failure) {
        _loginResult.postValue(failure)
    }

    fun resetPassword(user: UserResetPassword) {
        viewModelScope.launch {
            _resetPasswordResult.postValue(authRepository.resetPassword(user))
        }
    }

    fun setResetPasswordError(failure: Result.Failure) {
        _resetPasswordResult.postValue(failure)
    }

    fun tokenRefresh(token: String) {
        viewModelScope.launch {
            _tokenRefreshResult.postValue(authRepository.tokenRefresh(token))
        }
    }

    fun getProfile(token: String) {
        viewModelScope.launch {
            val result = authRepository.getProfile(token)
            _profileResult.postValue(result)
        }
    }

    fun clearErrors() {
        _loginResult.postValue(Result.Idle)
        _registerResult.postValue(Result.Idle)
        _resetPasswordResult.postValue(Result.Idle)
    }
}