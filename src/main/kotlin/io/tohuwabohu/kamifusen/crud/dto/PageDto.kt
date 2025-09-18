package io.tohuwabohu.kamifusen.crud.dto

import java.time.LocalDateTime
import java.util.UUID

data class PageDto(
    var id: UUID,
    var path: String,
    var domain: String? = null,
    val pageAdded: LocalDateTime?,
    var lastHit: LocalDateTime?
)