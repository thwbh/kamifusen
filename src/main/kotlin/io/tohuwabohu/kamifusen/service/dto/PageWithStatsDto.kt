package io.tohuwabohu.kamifusen.service.dto

import java.time.LocalDateTime
import java.util.*

/**
 * DTO that combines Page data with visit statistics for admin UI
 */
data class PageWithStatsDto(
    val id: UUID,
    val path: String,
    val domain: String?,
    val pageAdded: LocalDateTime,
    val lastHit: LocalDateTime?,
    val visitCount: Long
)