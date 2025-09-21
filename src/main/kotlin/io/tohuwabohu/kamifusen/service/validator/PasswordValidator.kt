package io.tohuwabohu.kamifusen.service.validator

import io.smallrye.mutiny.Uni

enum class PasswordValidation(val valid: Boolean, val message: String? = null) {
    VALID(true),
    EMPTY(false, "Your password is empty."),
    TOO_SHORT(false, "Your password must be at least 8 characters long."),
    NO_MATCH(false, "Your passwords do not match.");
}

fun validatePassword(password: String, passwordConfirmation: String): Uni<PasswordValidation> {
    val result = if (password != passwordConfirmation) {
        PasswordValidation.NO_MATCH
    } else if (password.isBlank() || passwordConfirmation.isBlank()) {
        PasswordValidation.EMPTY
    } else if (password.length < 8) {
        PasswordValidation.TOO_SHORT
    } else PasswordValidation.VALID

    return Uni.createFrom().item(result)
}