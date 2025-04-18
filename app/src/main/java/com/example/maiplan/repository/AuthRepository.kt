package com.example.maiplan.repository

import com.example.maiplan.network.AuthApi
import com.example.maiplan.network.Token
import com.example.maiplan.network.UserRegister
import com.example.maiplan.network.UserLogin
import com.example.maiplan.network.UserResetPassword
import com.example.maiplan.network.UserResponse

/**
 * Repository responsible for handling authentication-related network operations.
 *
 * Wraps API calls and returns a [Result] indicating success or failure,
 * abstracting network and error handling from the rest of the app.
 *
 * @property authApi An instance of [AuthApi] for making authentication network requests.
 *
 * @see AuthApi
 * @see Result
 */
class AuthRepository(private val authApi: AuthApi) {

    /**
     * Registers a new user.
     *
     * @param user The [UserRegister] object containing registration data.
     * @return A [Result] containing a [Token] on success or an error on failure.
     *
     * @see Result
     * @see UserRegister
     * @see Token
     * @see handleResponse
     */
    suspend fun register(user: UserRegister): Result<Token> {
        return try {
            handleResponse(authApi.register(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Logs in an existing user.
     *
     * @param user The [UserLogin] object containing login credentials.
     * @return A [Result] containing a [Token] on success or an error on failure.
     *
     * @see Result
     * @see UserLogin
     * @see Token
     * @see handleResponse
     */
    suspend fun login(user: UserLogin): Result<Token> {
        return try {
            handleResponse(authApi.login(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Requests a password reset for a user.
     *
     * @param user The [UserResetPassword] object containing password reset info.
     * @return A [Result] containing a [Token] on success or an error on failure.
     *
     * @see Result
     * @see UserResetPassword
     * @see Token
     * @see handleResponse
     */
    suspend fun resetPassword(user: UserResetPassword): Result<Token> {
        return try {
            handleResponse(authApi.resetPassword(user))
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
            handleResponse(authApi.tokenRefresh(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Fetches the user's profile data.
     *
     * @param token The authentication token.
     * @return A [Result] containing [UserResponse] with user data or an error on failure.
     *
     * @see Result
     * @see UserResponse
     * @see handleResponse
     */
    suspend fun getProfile(token: String): Result<UserResponse> {
        return try {
            handleResponse(authApi.getProfile(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}