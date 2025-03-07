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

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _registerResult = MutableLiveData<Result<Token>>()
    val registerResult: LiveData<Result<Token>> get() = _registerResult

    private val _loginResult = MutableLiveData<Result<Token>>()
    val loginResult: LiveData<Result<Token>> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Result<Token>>()
    val resetPasswordResult: LiveData<Result<Token>> get() = _resetPasswordResult

    private val _tokenRefreshResult = MutableLiveData<Result<Token>>()
    val tokenRefreshResult: LiveData<Result<Token>> get() = _tokenRefreshResult

    private val _profileResult = MutableLiveData<Result<UserResponse>> ()
    val profileResult: LiveData<Result<UserResponse>> get() = _profileResult

    private val _userId = MutableLiveData<Int?>()
    val userId: LiveData<Int?> get() = _userId

    init {
        clearErrors()
    }

    fun register(user: UserRegister) {
        viewModelScope.launch {
            _registerResult.postValue(authRepository.register(user))
        }
    }

    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.postValue(authRepository.login(user))
        }
    }

    fun resetPassword(user: UserResetPassword) {
        viewModelScope.launch {
            _resetPasswordResult.postValue(authRepository.resetPassword(user))
        }
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

            if (result is Result.Success) {
                _userId.postValue(result.data.id)
            }
        }
    }

    fun clearErrors() {
        _loginResult.postValue(Result.Idle)
        _registerResult.postValue(Result.Idle)
    }
}