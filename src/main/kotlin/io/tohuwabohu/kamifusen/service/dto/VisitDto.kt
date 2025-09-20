package io.tohuwabohu.kamifusen.service.dto

import java.util.*

/**
 * Internal context DTOs for service layer
 */
data class VisitContextDto(
    val remoteAddress: String,
    val userAgent: String,
    val referrer: String?,
    val country: String?,
    val pageHit: PageHitRequestDto
)

data class VisitorInfoDto(
    val remoteAddress: String,
    val userAgent: String,
    val referrer: String?,
    val country: String?
)

data class VisitResultDto(
    val visitCount: Long,
    val isNewVisitor: Boolean,
    val sessionId: UUID?
)