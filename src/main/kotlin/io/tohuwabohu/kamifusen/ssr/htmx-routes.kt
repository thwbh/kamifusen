package io.tohuwabohu.kamifusen.ssr

import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.Page
import io.tohuwabohu.kamifusen.crud.dto.PageVisitDto
import kotlinx.html.*
import java.time.format.DateTimeFormatter

/**
 * Determines the CSS class to apply to a row based on its index.
 *
 * @param index The index of the row.
 * @return A set of CSS class names.
 */
private fun rowClass(index: Int): Set<String> = when (index % 2 == 0) {
    true -> setOf("bg-slate-100")
    false -> setOf("bg-slate-300")
}

private val displayDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val displayDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/**
 * Generates the main dashboard view for the admin user.
 *
 * @param adminUser The authenticated admin user for whom the dashboard is generated.
 */
fun FlowContent.dashboard(adminUser: ApiUser) = main {
    id = "main-content"

    contentHeader("Dashboard")
    contentDiv {
        if (adminUser.updated != null) {
            p { +"Welcome to kamifusen. Manage your pages and statistics here." }

        } else {
            passwordFlow()
        }
    }
}

/**
 * Renders statistical data of page visits in an HTML table format.
 *
 * @param pageVisits List of page visit data transfer objects containing information about visits.
 */
fun FlowContent.stats(pageVisits: List<PageVisitDto>) = main {
    id = "main-content"

    contentHeader("Stats")
    contentDiv {
        table {
            classes = setOf("table-auto", "rounded-md")

            thead {
                tr {
                    styledTh { +"Path" }
                    styledTh { +"Domain" }
                    styledTh { +"Visits" }
                    styledTh { +"Added" }
                }
            }
            tbody {
                pageVisits.forEachIndexed { index, visit ->
                    tr {
                        classes = when (index % 2 == 0) {
                            true -> setOf("bg-slate-100")
                            false -> setOf("bg-slate-300")
                        }

                        styledTd { +visit.path }
                        styledTd { +if(visit.domain == null) "" else visit.domain!! }
                        styledTd { +visit.visits.toString() }
                        styledTd { +visit.pageAdded.format(displayDateTimeFormat) }
                    }
                }
            }
        }
    }
}

/**
 * Adds and manages a list of pages for tracking within the main content area.
 *
 * @param pages List of pages to be displayed and managed in the table.
 */
fun FlowContent.pages(pages: List<Page>) = main {
    id = "main-content"

    contentHeader("Pages")
    contentDiv {
        form {
            p { +"Add pages to track or remove pages from tracking." }
            p { +"If you use an API Key to track a page that does not show up here, visits won't be tracked." }
            p { +"Add a domain for better oversight. Following best practise, each domain should have their own API Key." }

            table {
                classes = setOf("table-auto", "rounded-md")

                thead {
                    tr {
                        styledTh { +"Path" }
                        styledTh { +"Domain" }
                        styledTh { +"Last Hit" }
                        styledTh { +"Added" }
                        styledTh { +"Action" }
                    }
                }
                tbody {
                    pages.forEachIndexed { index, page ->
                        tr {
                            classes = rowClass(index)

                            styledTd { +page.path }
                            styledTd { +page.domainGroup.groupName }
                            styledTd {
                                when (page.lastHit) {
                                    null -> +"-"
                                    else -> page.lastHit!!.format(displayDateFormat)
                                }
                            }
                            styledTd { +page.pageAdded.format(displayDateTimeFormat) }

                            td {
                                classes = setOf("flex", "justify-center", "px-3", "py-2", "text-sm", "font-medium")
                                div {
                                    button {
                                        id = "delete"

                                        attributes["hx-swap"] = "outerHTML"
                                        attributes["hx-target"] = "#delete"
                                        attributes["hx-post"] = "/fragment/pagedel/${page.id}"
                                        attributes["hx-confirm"] =
                                            "This will remove tracking for this page. Do you want to proceed?"

                                        span {
                                            classes = setOf("tabler--trash")
                                        }

                                        span {
                                            classes = setOf("sr-only")
                                            p { +"Remove page" }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    tr {
                        classes = rowClass(pages.size)

                        td {
                            div {
                                id = "path"

                                input(InputType.text) {
                                    classes = setOf("table-input-inline", "h-8")

                                    name = "path"
                                    required = true
                                }
                            }
                        }

                        td {
                            div {
                                id = "domain"

                                input(InputType.text) {
                                    classes = setOf("table-input-inline", "h-8")

                                    name = "domain"
                                    required = false
                                }
                            }
                        }
                        td {}
                        td {}

                        td {
                            classes = setOf("flex", "justify-center", "px-3", "py-2", "text-sm", "font-medium")
                            div {
                                button {
                                    id = "add"

                                    attributes["hx-swap"] = "outerHTML"
                                    attributes["hx-target"] = "#add"
                                    attributes["hx-post"] = "/fragment/pageadd"

                                    span {
                                        classes = setOf("tabler--world-plus")
                                    }

                                    span {
                                        classes = setOf("sr-only")
                                        p { +"Add page" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders a HTML content block displaying a table of API users with functionality to issue and revoke API keys.
 *
 * @param users A list of `ApiUser` objects to be displayed in the table. Each user includes details such as username, role,
 *              expiration date, and the date they were added. The table also provides actions to retire or regenerate API keys
 *              for the users.
 */
fun FlowContent.users(users: List<ApiUser>) = main {
    id = "main-content"

    contentHeader("Users")
    contentDiv {
        div {
            classes = setOf("p-2")

            p { +"You can issue and revoke new API Keys here. Immediately copy and distribute the API Key after generation, you will be able to do that only once." }
        }

        form {
            table {
                classes = setOf("table-auto", "rounded-md")

                thead {
                    tr {
                        styledTh { +"Username" }
                        styledTh { +"Role" }
                        styledTh { +"Expires" }
                        styledTh { +"Added" }
                        styledTh { +"Actions" }
                    }
                }

                tbody {
                    users.forEachIndexed { index, user ->
                        tr {
                            id = "user-${user.id.toString()}"

                            classes = rowClass(index)

                            styledTd { +user.username }
                            styledTd { +user.role }
                            styledTd {
                                when (user.expiresAt) {
                                    null -> +"-"
                                    else -> +user.expiresAt!!.format(displayDateTimeFormat)
                                }
                            }
                            styledTd {
                                when (user.added) {
                                    null -> +"-"
                                    else -> +user.added!!.format(displayDateFormat)
                                }
                            }
                            td {
                                classes = setOf("flex", "justify-center", "px-3", "py-2", "text-sm", "font-medium")

                                if (user.username != "admin") {
                                    button {
                                        if (user.expiresAt == null) {
                                            attributes["hx-post"] = "/fragment/retire/${user.id}"
                                            attributes["hx-swap"] = "outerHTML"
                                            attributes["hx-target"] = "#user-${user.id.toString()}"
                                            attributes["hx-confirm"] =
                                                "This will revoke any access granted by this API Key. Do you want to proceed?"

                                            span {
                                                classes = setOf("tabler--key-off")
                                            }

                                            span {
                                                classes = setOf("sr-only")
                                                p { +"Retire user" }
                                            }
                                        } else {
                                            attributes["hx-post"] = "/fragment/refresh/${user.id}"
                                            attributes["hx-swap"] = "outerHTML"
                                            attributes["hx-target"] = "#user-${user.id.toString()}"

                                            span {
                                                classes = setOf("tabler--refresh")
                                            }

                                            span {
                                                classes = setOf("sr-only")
                                                p { +"Regenerate" }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    tr {
                        classes = rowClass(users.size)

                        td {
                            div {
                                id = "username"

                                input(InputType.text) {
                                    classes = setOf("table-input-inline", "h-8")

                                    name = "username"
                                    required = true
                                }
                            }
                        }
                        td {
                            div {
                                id = "role"

                                select {
                                    classes = setOf("table-input-inline", "h-8")

                                    name = "role"

                                    option { +"api-user" }
                                    option { +"api-admin" }
                                }
                            }
                        }
                        td {}
                        td {
                            div {
                                input(InputType.date) {
                                    classes = setOf("table-input-inline", "h-8")

                                    name = "expiresAt"
                                    required = false
                                }
                            }
                        }
                        td {
                            classes = setOf("flex", "justify-center", "px-3", "py-2", "text-sm", "font-medium")
                            div {
                                button {
                                    id = "key"

                                    attributes["hx-swap"] = "outerHTML"
                                    attributes["hx-target"] = "#key"
                                    attributes["hx-post"] = "/fragment/keygen"

                                    span {
                                        classes = setOf("tabler--user-plus")
                                    }

                                    span {
                                        classes = setOf("sr-only")
                                        p { +"Add user" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}