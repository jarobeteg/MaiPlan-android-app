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
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<AuthRepository.Result<Token>>()
    val registerResult: LiveData<AuthRepository.Result<Token>> get() = _registerResult

    private val _loginResult = MutableLiveData<AuthRepository.Result<Token>>()
    val loginResult: LiveData<AuthRepository.Result<Token>> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<AuthRepository.Result<Token>>()
    val resetPasswordResult: LiveData<AuthRepository.Result<Token>> get() = _resetPasswordResult

    private val _profileResult = MutableLiveData<AuthRepository.Result<UserResponse>> ()
    val profileResult: LiveData<AuthRepository.Result<UserResponse>> get() = _profileResult

    init {
        _registerResult.value = AuthRepository.Result.Idle
        _loginResult.value = AuthRepository.Result.Idle
    }

    fun register(user: UserRegister) {
        viewModelScope.launch {
            _registerResult.value = authRepository.register(user)
        }
    }

    fun login(user: UserLogin) {
        viewModelScope.launch {
            _loginResult.value = authRepository.login(user)
        }
    }

    fun resetPassword(user: UserResetPassword) {
        viewModelScope.launch {
            _resetPasswordResult.value = authRepository.resetPassword(user)
        }
    }

    fun getProfile(token: String) {
        viewModelScope.launch {
            _profileResult.value = authRepository.getProfile(token)
        }
    }

    fun clearErrors() {
        _loginResult.value = AuthRepository.Result.Idle
        _registerResult.value = AuthRepository.Result.Idle
    }
}