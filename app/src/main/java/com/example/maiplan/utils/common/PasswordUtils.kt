package com.example.maiplan.utils.common

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordValidationException(val code: Int, message: String) : IllegalArgumentException(message)

object PasswordUtils {
    // cost = 12 means stronger but slower hashing and it produces a hash format that is compatible with bcrypt across other programming languages
    // toCharArray is standard practice for password hashing in android since Strings are immutable and they live in memory until the garbage collector comes and collects it
    // char can be cleared and the password will not stay in memory, in a memory dump a string password can be leaked but when its a cleared char array then it will not be leaked
    // have to make input field of password to use CharArray and not String - remove this comment after safe input fields have been added
    fun hashPassword(password: CharArray): String {
        try {
            return BCrypt.withDefaults().hashToString(12, password)
        } finally {
            password.fill('\u0000') // wipe from memory
        }
    }

    fun verifyPassword(plainPassword: CharArray, hashedPassword: String): Boolean {
        try {
            val result = BCrypt.verifyer().verify(plainPassword, hashedPassword)
            return result.verified
        } finally {
            plainPassword.fill('\u0000') // wipe from memory
        }
    }

    fun validatePassword(password: CharArray): CharArray {
        val trimmed = password.trimWhitespace()
        if (trimmed.isEmpty()) {
            throw PasswordValidationException(4, "Password field cannot be empty!")
        }
        return trimmed
    }

    fun validatePasswordStrength(password: CharArray) {
        if (password.size < 8)
            throw PasswordValidationException(5, "Password too short!")
        if (!password.any { it.isLowerCase() })
            throw PasswordValidationException(5, "Password must contain lowercase!")
        if (!password.any { it.isUpperCase() })
            throw PasswordValidationException(5, "Password must contain uppercase!")
        if (!password.any { it.isDigit() })
            throw PasswordValidationException(5, "Password must contain a digit!")
        if (!password.any { "!_@#$?".contains(it) })
            throw PasswordValidationException(5, "Password must contain a special character!")
    }

    fun validatePasswordMatch(password: CharArray, passwordAgain: CharArray) {
        val trimmed = passwordAgain.trimWhitespace()
        if (trimmed.isEmpty())
            throw PasswordValidationException(6, "Password again field cannot be empty")
        if (!password.contentEquals(trimmed))
            throw PasswordValidationException(7, "Passwords do not match")
    }

    private fun CharArray.trimWhitespace(): CharArray {
        return filterNot { it.isWhitespace() }.toCharArray()
    }
}