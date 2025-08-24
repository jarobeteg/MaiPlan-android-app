package com.example.maiplan.utils.common

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import java.util.Date
import java.util.concurrent.TimeUnit

object JWTUtils {
    private const val SECRET_KEY = "dd98b292bdaa08e31b758425b20b328d26d627d55d56b9c61203a3db67207b11" // this is just a placeholder
    private val ALGORITHM: Algorithm = Algorithm.HMAC256(SECRET_KEY)
    private const val ACCESS_TOKEN_EXPIRY_DAYS = 7L

    fun createAccessToken(userId: Long): String {
        val now = Date()
        val expiry = Date(now.time + TimeUnit.DAYS.toMillis(ACCESS_TOKEN_EXPIRY_DAYS))

        val builder = JWT.create()
            .withSubject(userId.toString())
            .withExpiresAt(expiry)

        return builder.sign(ALGORITHM)
    }

    fun getUserIdFromToken(token: String): Int {
        try {
            val verifier = JWT.require(ALGORITHM).build()
            val decodedJWT = verifier.verify(token)
            val subject = decodedJWT.subject ?: throw SecurityException("Missing 'sub' claim")
            return subject.toInt()
        } catch (e: TokenExpiredException) {
            throw SecurityException("Token has expired")
        } catch (e: SignatureVerificationException) {
            throw SecurityException("Invalid token signature")
        } catch (e: JWTDecodeException) {
            throw SecurityException("Invalid token format")
        }
    }
}