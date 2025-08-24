package com.example.maiplan.repository.auth

import com.example.maiplan.database.entities.AuthEntity
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.repository.Result
import retrofit2.Response
import com.example.maiplan.repository.handleResponse

/**
 * Repository responsible for handling authentication-related operations.
 *
 * Acts as an abstraction layer between the [AuthRemoteDataSource] and the rest of the application.
 * It wraps raw network responses using [Result] and manages error handling.
 *
 * - All methods use `try-catch` to return a clean [Result] object.
 * - Uses [handleResponse] to convert [Response] into [Result].
 *
 * @property remoteDataSource An instance of [AuthRemoteDataSource] that directly communicates with the [AuthApi].
 *
 * @see AuthRemoteDataSource
 * @see AuthApi
 * @see Result
 * @see handleResponse
 */
class AuthRepository(private val remoteDataSource: AuthRemoteDataSource? = null, private val localDataSource: AuthLocalDataSource? = null) {

    suspend fun localRegister(user: AuthEntity): Long {
        return try {
            localDataSource!!.register(user)
        } catch (e: Exception) {

        } as Long
    }

    /**
     * Registers a new user.
     *
     * @param user The [UserRegister] data containing user registration info.
     * @return A [Result] containing a [Token] on success, or an [Exception] on failure.
     */
    suspend fun register(user: UserRegister): Result<Token> {
        return try {
            handleResponse(remoteDataSource!!.register(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Logs in an existing user.
     *
     * @param user The [UserLogin] data containing user login credentials.
     * @return A [Result] containing a [Token] on success, or an [Exception] on failure.
     */
    suspend fun login(user: UserLogin): Result<Token> {
        return try {
            handleResponse(remoteDataSource!!.login(user))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Requests a password reset for the user.
     *
     * @param user The [UserResetPassword] data containing email or reset info.
     * @return A [Result] containing a [Token] on success, or an [Exception] on failure.
     */
    suspend fun resetPassword(user: UserResetPassword): Result<Token> {
        return try {
            handleResponse(remoteDataSource!!.resetPassword(user))
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    /**
     * Refreshes the current authentication token.
     *
     * @param token The expired or near-expiry token string.
     * @return A [Result] containing a new [Token] on success, or an [Exception] on failure.
     */
    suspend fun tokenRefresh(token: String): Result<Token> {
        return try {
            handleResponse(remoteDataSource!!.tokenRefresh(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Fetches the authenticated user's profile information.
     *
     * @param token A valid bearer token for the user.
     * @return A [Result] containing a [UserResponse] on success, or an [Exception] on failure.
     */
    suspend fun getProfile(token: String): Result<UserResponse> {
        return try {
            handleResponse(remoteDataSource!!.getProfile(token))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}