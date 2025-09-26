package io.tohuwabohu.kamifusen.service

import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.PageHitRequestDto
import io.tohuwabohu.kamifusen.service.crud.Page
import io.tohuwabohu.kamifusen.service.crud.PageRepository
import io.tohuwabohu.kamifusen.service.crud.PageVisitRepository
import io.tohuwabohu.kamifusen.service.crud.SessionRepository
import io.tohuwabohu.kamifusen.service.crud.Visitor
import io.tohuwabohu.kamifusen.service.crud.VisitorRepository
import io.tohuwabohu.kamifusen.service.context.VisitContext
import io.tohuwabohu.kamifusen.service.context.VisitResult
import io.tohuwabohu.kamifusen.service.mapper.VisitRequestMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.NotFoundException
import java.util.*

/**
 * Domain service for handling page visit business logic
 */
@ApplicationScoped
class PageVisitService(
    private val pageRepository: PageRepository,
    private val visitorRepository: VisitorRepository,
    private val sessionRepository: SessionRepository,
    private val pageVisitRepository: PageVisitRepository,
    private val visitRequestMapper: VisitRequestMapper
) {

    /**
     * Processes a page hit request, handling visitor creation, session management, and visit tracking
     */
    @WithTransaction
    fun processPageHit(context: VisitContext): Uni<VisitResult> {
        Log.debug("Processing page hit for ${context.pageHit.domain}${context.pageHit.path}")

        return validatePageHitRequest(context.pageHit)
            .flatMap { validationErrors ->
                if (validationErrors != null && validationErrors.isNotEmpty()) {
                    val errorMessage = "Validation failed: ${validationErrors.joinToString(", ")}"
                    Log.warn("Invalid page hit request: $errorMessage")
                    Uni.createFrom().failure(IllegalArgumentException(errorMessage))
                } else {
                    ensurePageExists(context.pageHit)
                        .flatMap { page -> processVisitor(context, page!!) }
                }
            }
            .onFailure().invoke { throwable ->
                Log.error("Failed to process page hit: ${context.pageHit.domain}${context.pageHit.path}", throwable)
            }
    }

    /**
     * Validates the page hit request as first step of processing
     */
    private fun validatePageHitRequest(pageHit: PageHitRequestDto): Uni<List<String>?> {
        return visitRequestMapper.validatePageHitRequest(pageHit)
    }

    /**
     * Gets the visit count for a specific page
     */
    fun getVisitCount(pageId: UUID): Uni<Long> {
        return pageRepository.findByPageId(pageId)
            .chain { page ->
                if (page != null) {
                    pageVisitRepository.countVisits(page.id)
                } else {
                    Uni.createFrom().failure(NotFoundException("Page not found: $pageId"))
                }
            }
    }

    /**
     * Ensures the page exists in the database, creating it if necessary
     */
    private fun ensurePageExists(pageHit: PageHitRequestDto): Uni<Page?> {
        return pageRepository.addPageIfAbsent(pageHit.path, pageHit.domain)
    }

    /**
     * Processes visitor-related logic for the page hit
     */
    private fun processVisitor(context: VisitContext, page: Page): Uni<VisitResult> {
        return visitorRepository.findByInfo(context.remoteAddress, context.userAgent)
            .flatMap { visitor ->
                if (visitor == null) {
                    handleNewVisitor(context, page)
                } else {
                    handleExistingVisitor(context, page, visitor)
                }
            }
    }

    /**
     * Handles the case where a new visitor is detected
     */
    private fun handleNewVisitor(context: VisitContext, page: Page): Uni<VisitResult> {
        Log.debug("Creating new visitor for ${context.remoteAddress}")

        return createNewVisitor(context)
            .flatMap { visitor ->
                recordVisitAndSession(visitor, page, isNewVisitor = true)
                    .flatMap { sessionId ->
                        pageVisitRepository.countVisits(page.id)
                            .map { count -> VisitResult(count, true, sessionId) }
                    }
            }
    }

    /**
     * Handles the case where an existing visitor is detected
     */
    private fun handleExistingVisitor(context: VisitContext, page: Page, visitor: Visitor): Uni<VisitResult> {
        Log.debug("Processing existing visitor ${visitor.id}")

        return updateVisitorLastSeen(visitor)
            .flatMap { _ -> checkAndProcessVisit(context, page, visitor) }
    }

    /**
     * Creates a new visitor with the provided context information
     */
    private fun createNewVisitor(context: VisitContext): Uni<Visitor> {
        return visitorRepository.addVisitor(
            remoteAddress = context.remoteAddress,
            userAgent = context.userAgent,
            referrer = context.referrer,
            country = context.country
        )
    }

    /**
     * Records a visit and manages session state
     */
    private fun recordVisitAndSession(visitor: Visitor, page: Page, isNewVisitor: Boolean): Uni<UUID?> {
        return pageVisitRepository.addVisit(page.id, visitor.id)
            .flatMap { _ ->
                // New visitors always start a new session
                sessionRepository.findOrCreateSessionForVisitor(visitor.id, hasRecentActivity = false)
                    .flatMap { session ->
                        sessionRepository.incrementPageViews(session.id)
                            .map { session.id }
                    }
                    .onFailure().recoverWithItem { throwable ->
                        Log.warn("Session management failed for visitor ${visitor.id}", throwable)
                        null
                    }
            }
    }

    /**
     * Updates the visitor's last seen timestamp
     */
    private fun updateVisitorLastSeen(visitor: Visitor): Uni<Visitor?> {
        return visitorRepository.updateLastSeen(visitor.id)
            .onFailure().recoverWithItem { throwable ->
                Log.warn("Failed to update last seen for visitor ${visitor.id}", throwable)
                visitor
            }
    }

    /**
     * Checks if this is a new visit and processes accordingly
     */
    private fun checkAndProcessVisit(context: VisitContext, page: Page, visitor: Visitor): Uni<VisitResult> {
        return pageVisitRepository.countVisitsForVisitor(page.id, visitor.id)
            .flatMap { existingVisitCount ->
                if (existingVisitCount <= 0) {
                    processNewVisitForExistingVisitor(context, page, visitor)
                } else {
                    // Visitor has already visited this page, return current count
                    pageVisitRepository.countVisits(page.id)
                        .map { totalCount -> VisitResult(totalCount, false, null) }
                }
            }
    }

    /**
     * Processes a new visit for an existing visitor
     */
    private fun processNewVisitForExistingVisitor(context: VisitContext, page: Page, visitor: Visitor): Uni<VisitResult> {
        return checkForRecentActivity(visitor, context.pageHit.domain)
            .flatMap { hasRecentActivity ->
                recordVisitAndManageSession(visitor, page, hasRecentActivity)
            }
    }

    /**
     * Checks if the visitor has recent activity on the domain (for session continuity)
     */
    private fun checkForRecentActivity(visitor: Visitor, domain: String): Uni<Boolean> {
        // TODO: minutesBack should be configurable
        return pageVisitRepository.findRecentVisitByVisitorOnDomain(visitor.id, domain, 30)
            .map { recentVisit -> recentVisit != null }
    }

    /**
     * Records the visit and manages session based on recent activity
     */
    private fun recordVisitAndManageSession(visitor: Visitor, page: Page, hasRecentActivity: Boolean): Uni<VisitResult> {
        return pageVisitRepository.addVisit(page.id, visitor.id)
            .flatMap { _ ->
                sessionRepository.findOrCreateSessionForVisitor(visitor.id, hasRecentActivity)
                    .flatMap { session ->
                        sessionRepository.incrementPageViews(session.id)
                            .flatMap { _ ->
                                pageVisitRepository.countVisits(page.id)
                                    .map { count -> VisitResult(count, false, session.id) }
                            }
                    }
                    .onFailure().recoverWithUni { throwable ->
                        Log.warn("Session management failed for visitor ${visitor.id}", throwable)
                        // Fallback: just return the visit count without session info
                        pageVisitRepository.countVisits(page.id)
                            .map { count -> VisitResult(count, false, null) }
                    }
            }
    }
}