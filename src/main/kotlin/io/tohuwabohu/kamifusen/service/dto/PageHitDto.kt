package io.tohuwabohu.kamifusen.service.dto

data class PageHitDto(
    var path: String,
    var domain: String
)

/**
 * Request DTOs for page visit endpoints
 */
data class PageHitRequestDto(
    val path: String,
    val domain: String
)
