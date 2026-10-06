package com.example.maiplan.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.auth.AccountRepository
import com.example.maiplan.utils.common.UserSession
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {
    private val _usernameResult = MutableLiveData<Result<UserEntity>>(Result.Idle)
    val usernameResult: LiveData<Result<UserEntity>> get() = _usernameResult
    private val _passwordResult = MutableLiveData<Result<UserEntity>>(Result.Idle)
    val passwordResult: LiveData<Result<UserEntity>> get() = _passwordResult

    private val isBusy: Boolean get() =
        _usernameResult.value is Result.Loading || _passwordResult.value is Result.Loading

    fun changeUsername(username: String) {
        if (isBusy) return
        _usernameResult.value = Result.Loading
        viewModelScope.launch {
            _usernameResult.value = updateActiveUser(repository.changeUsername(username))
        }
    }

    fun changePassword(password: String, passwordAgain: String) {
        if (isBusy) return
        _passwordResult.value = Result.Loading
        viewModelScope.launch {
            _passwordResult.value = updateActiveUser(repository.changePassword(password, passwordAgain))
        }
    }

    fun clearUsernameResult() {
        if (!isBusy) _usernameResult.value = Result.Idle
    }

    fun clearPasswordResult() {
        if (!isBusy) _passwordResult.value = Result.Idle
    }

    private fun updateActiveUser(result: Result<UserEntity>): Result<UserEntity> {
        if (result is Result.Success && UserSession.userSyncId == result.data.syncId) {
            UserSession.setup(result.data)
        }
        return result
    }
}
