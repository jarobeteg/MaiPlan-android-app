package com.example.maiplan.repository.auth

import com.example.maiplan.network.api.AuthApi
import com.example.maiplan.network.api.Token
import com.example.maiplan.network.api.UserLogin
import com.example.maiplan.network.api.UserRegister
import com.example.maiplan.network.api.UserResetPassword
import com.example.maiplan.network.api.UserResponse
import retrofit2.Response

/**
 * Remote data source that directly communicates with the authentication-related endpoints via [AuthApi].
 *
 * Provides suspend functions that return raw [Response] objects to be handled by [AuthRepository].
 * Each function corresponds to an authentication-related network call.
 *
 * @property authApi An instance of [AuthApi] used to perform authentication HTTP requests.
 *
 * @see AuthApi
 */
class AuthRemoteDataSource(private val authApi: AuthApi) {

    /**
     * Sends a registration request to the API.
     *
     * @param user The registration payload.
     * @return The raw [Response] containing a [Token] or error body.
     */
    suspend fun register(user: UserRegister): Response<UserResponse> {
        return authApi.register(user)
    }

    /**
     * Sends a login request to the API.
     *
     * @param user The login payload.
     * @return The raw [Response] containing a [Token] or error body.
     */
    suspend fun login(user: UserLogin): Response<Token> {
        return authApi.login(user)
    }

    /**
     * Sends a password reset request to the API.
     *
     * @param user The password reset payload.
     * @return The raw [Response] containing a [Token] or error body.
     */
    suspend fun resetPassword(user: UserResetPassword): Response<Token> {
        return authApi.resetPassword(user)
    }

    /**
     * Sends a token refresh request to the API.
     *
     * @param token The expired or expiring token.
     * @return The raw [Response] containing a new [Token] or error body.
     */
    suspend fun tokenRefresh(token: String): Response<Token> {
        return authApi.tokenRefresh(token)
    }


    /**
     * Requests the current authenticated user's profile from the API.
     *
     * @param token A valid bearer token.
     * @return The raw [Response] containing [UserResponse] data or an error body.
     */
    suspend fun getProfile(token: String): Response<UserResponse> {
        return  authApi.getProfile(token)
    }
}