package com.example.maiplan.utils.common

object Validation {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun validateEmail(email: String): String {
        val trimmed = email.trim()
        require(trimmed.isNotEmpty()) { "Email field cannot be empty!" }
        require(trimmed.matches(EMAIL_REGEX)) { "Invalid  email format!" }
        return trimmed
    }

    fun validateUsername(username: String): String {
        val trimmed = username.trim()
        require(trimmed.isNotEmpty()) { "Username field cannot be empty!" }
        return trimmed
    }

    fun validatePassword(password: String): String {
        val trimmed = password.trim()
        require(trimmed.isNotEmpty()) { "Password field cannot be empty!" }
        return trimmed
    }

    fun validatePasswordStrength(password: String) {
        require(password.length >= 8) { "Password must be at least 8 characters long." }
        require(password.any { it.isLowerCase() }) { "Password must contain a lowercase letter." }
        require(password.any { it.isUpperCase() }) { "Password must contain an uppercase letter." }
        require(password.any { it.isDigit() }) { "Password must contain a digit." }
        require(password.any { "!_@#$?".contains(it) }) { "Password must contain a special character." }
    }

    fun validatePasswordMatch(password: String, passwordAgain: String) {
        require(passwordAgain.isNotEmpty()) { "Password again field cannot be empty!" }
        require(password == passwordAgain) { "Passwords do not match!" }
    }
}