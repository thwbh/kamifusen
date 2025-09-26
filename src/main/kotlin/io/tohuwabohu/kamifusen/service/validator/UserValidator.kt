package io.tohuwabohu.kamifusen.service.validator

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.service.crud.ApiUserRepository

enum class UserValidation(val valid: Boolean, val message: String? = null) {
    VALID(true),
    EMPTY(false, "Name must not be empty."),
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