package io.tohuwabohu.kamifusen.crud.security

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUserRepository

enum class UserValidation(val valid: Boolean, val message: String? = null) {
    VALID(true),
    EMPTY(false, "Field must not be empty."),
    EXISTS(false, "Name is already taken.");
}

fun validateUser(username: String, apiUserRepository: ApiUserRepository): Uni<UserValidation> {
    return if (username.isBlank()) {
        Uni.createFrom().item(UserValidation.EMPTY)
    } else {
        apiUserRepository.findByUsername(username).map { user ->
            if (user != null) {
                UserValidation.EXISTS
            } else UserValidation.VALID
        }
    }
}