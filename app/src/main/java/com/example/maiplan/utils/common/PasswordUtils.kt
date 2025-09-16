package com.example.maiplan.utils.common

import at.favre.lib.crypto.bcrypt.BCrypt
object PasswordUtils {
    // cost = 12 means stronger but slower hashing and it produces a hash format that is compatible with bcrypt across other programming languages
    // toCharArray is standard practice for password hashing in android since Strings are immutable and they live in memory until the garbage collector comes and collects it
    // char can be cleared and the password will not stay in memory, in a memory dump a string password can be leaked but when its a cleared char array then it will not be leaked
    // have to make input field of password to use CharArray and not String - remove this comment after safe input fields have been added
    // for now I will stick to usage of String since in the very end of sending data and having them synced doesn't seem to change much
    // but I will do more research and experimenting with this topic as I value data safety
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified
    }
}