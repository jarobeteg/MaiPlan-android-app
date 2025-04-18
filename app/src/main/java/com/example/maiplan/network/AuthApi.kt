package com.example.maiplan.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Represents an authentication token returned by the server.
 *
 * @property accessToken The token used for authentication.
 * @property tokenType The type of the token (Bearer).
 */
data class Token(@SerializedName("access_token") val accessToken: String, @SerializedName("token_type") val tokenType: String)

/**
 * Data model for user registration requests.
 *
 * @property email The user's email.
 * @property username The user's username.
 * @property password The user's chosen password.
 * @property passwordAgain Confirmation of the user's password.
 */
data class UserRegister(val email: String, val username: String, val password: String, @SerializedName("password_again") val passwordAgain: String)

/**
 * Data model for user password reset requests.
 *
 * @property email The user's email.
 * @property password The new password.
 * @property passwordAgain Confirmation of the new password.
 */
data class UserResetPassword(val email: String, val password: String, @SerializedName("password_again") val passwordAgain: String)

/**
 * Data model for user login requests.
 *
 * @property email The user's email.
 * @property password The user's password.
 */
data class UserLogin(val email: String, val password: String)

/**
 * Represents the user's profile information returned by the server.
 *
 * @property id The user's unique Id.
 * @property email The user's email address.
 * @property username The user's username.
 */
data class UserResponse(@SerializedName("user_id") val id: Int, val email: String, val username: String)

/**
 * API interface defining authentication-related endpoints.
 */
interface AuthApi {

    /**
     * Registers a new user.
     *
     * @param auth The [UserRegister] request body.
     * @return A [Response] containing a [Token] on success.
     */
    @POST("auth/register")
    suspend fun register(@Body auth: UserRegister): Response<Token>

    /**
     * Authenticates a user and returns a token.
     *
     * @param auth The [UserLogin] request body.
     * @return A [Response] containing a [Token] on success.
     */
    @POST("auth/login")
    suspend fun login(@Body auth: UserLogin): Response<Token>

    /**
     * Resets the user's password.
     *
     * @param auth The [UserResetPassword] request body.
     * @return A [Response] containing a [Token] on success.
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body auth: UserResetPassword): Response<Token>

    /**
     * Refreshes the user's authentication token.
     *
     * @param token The current token passed in the Authorization header.
     * @return A [Response] containing a new [Token] on success.
     */
    @POST("auth/token-refresh")
    suspend fun tokenRefresh(@Header("Authorization") token: String): Response<Token>

    /**
     * Fetches the currently authenticated user's profile.
     *
     * @param token The current token passed in the Authorization header.
     * @return A [Response] containing the [UserResponse] data.
     */
    @GET("auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserResponse>
}