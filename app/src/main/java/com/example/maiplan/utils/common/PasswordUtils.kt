package com.example.maiplan.utils.common

import at.favre.lib.crypto.bcrypt.BCrypt

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
}