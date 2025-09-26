package io.tohuwabohu.kamifusen.service.validator

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.service.crud.PageRepository

enum class PageValidation(val valid: Boolean, val message: String? = null) {
    VALID(true),
    EMPTY(false, "Path must not be empty."),
    EXISTS(false, "Page already exists.");
}

fun validatePage(path: String, domain: String = "", pageRepository: PageRepository): Uni<PageValidation> {
    return if (path.isBlank()) {
        Uni.createFrom().item(PageValidation.EMPTY)
    } else {
        pageRepository.addPageIfAbsent(path, domain).map { page ->
            if (page != null && page.domain == domain) {
                PageValidation.EXISTS
            } else PageValidation.VALID
        }
    }
}