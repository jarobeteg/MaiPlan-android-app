package com.example.maiplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.network.Token
import com.example.maiplan.network.UserAuth
import com.example.maiplan.network.UserResponse
import com.example.maiplan.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<AuthRepository.Result<UserResponse>>()
    val registerResult: LiveData<AuthRepository.Result<UserResponse>> get() = _registerResult

    private val _loginResult = MutableLiveData<AuthRepository.Result<Token>>()
    val loginResult: LiveData<AuthRepository.Result<Token>> get() = _loginResult

    private val _profileResult = MutableLiveData<AuthRepository.Result<UserResponse>> ()
    val profileResult: LiveData<AuthRepository.Result<UserResponse>> get() = _profileResult

    fun register(user: UserAuth) {
        viewModelScope.launch {
            _registerResult.value = authRepository.register(user)
        }
    }

    fun login(user: UserAuth) {
        viewModelScope.launch {
            _loginResult.value = authRepository.login(user)
        }
    }

    fun getProfile(token: String) {
        viewModelScope.launch {
            _profileResult.value = authRepository.getProfile(token)
        }
    }
}