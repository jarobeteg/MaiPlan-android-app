package com.example.maiplan.repository.auth

import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.repository.Result
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling authentication-related network operations.
 *
 * Wraps API calls and returns a [com.example.maiplan.repository.Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property authApi An instance of [com.example.maiplan.network.api.AuthApi] for making authentication network requests.
 *
 * @see com.example.maiplan.network.api.AuthApi
 * @see com.example.maiplan.repository.Result
 */
class AuthRepository(private val remoteDataSource: AuthRemoteDataSource) {

    /**
     * Registers a new user.
     *
     * @param user The [com.example.maiplan.network.api.UserRegister] object containing registration data.
     * @return A [com.example.maiplan.repository.Result] containing a [com.example.maiplan.network.api.Token] on success or an error on failure.
     *
     * @see com.example.maiplan.repository.Result
     * @see com.example.maiplan.network.api.UserRegister
     * @see com.example.maiplan.network.api.Token
     * @see com.example.maiplan.repository.handleResponse
     */
    suspend fun register(user: UserRegister): Result<Token> {
        return try {
            handleResponse(remoteDataSource.register(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Logs in an existing user.
     *
     * @param user The [com.example.maiplan.network.api.UserLogin] object containing login credentials.
     * @return A [Result] containing a [Token] on success or an error on failure.
     *
     * @see Result
     * @see com.example.maiplan.network.api.UserLogin
     * @see Token
     * @see handleResponse
     */
    suspend fun login(user: UserLogin): Result<Token> {
        return try {
            handleResponse(remoteDataSource.login(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Requests a password reset for a user.
     *
     * @param user The [com.example.maiplan.network.api.UserResetPassword] object containing password reset info.
     * @return A [Result] containing a [Token] on success or an error on failure.
     *
     * @see Result
     * @see com.example.maiplan.network.api.UserResetPassword
     * @see Token
     * @see handleResponse
     */
    suspend fun resetPassword(user: UserResetPassword): Result<Token> {
        return try {
            handleResponse(remoteDataSource.resetPassword(user))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    /**
     * Refreshes the authentication token.
     *
     * @param token The expired or soon to expire token to refresh.
     * @return A [Result] containing a new [Token] or an error on failure.
     *
     * @see Result
     * @see Token
     * @see handleResponse
     */
    suspend fun tokenRefresh(token: String): Result<Token> {
        return try {
            handleResponse(remoteDataSource.tokenRefresh(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Fetches the user's profile data.
     *
     * @param token The authentication token.
     * @return A [Result] containing [com.example.maiplan.network.api.UserResponse] with user data or an error on failure.
     *
     * @see Result
     * @see com.example.maiplan.network.api.UserResponse
     * @see handleResponse
     */
    suspend fun getProfile(token: String): Result<UserResponse> {
        return try {
            handleResponse(remoteDataSource.getProfile(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}