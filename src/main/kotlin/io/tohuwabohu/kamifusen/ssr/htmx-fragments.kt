package io.tohuwabohu.kamifusen.ssr

import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.Page
import io.tohuwabohu.kamifusen.crud.dto.PageVisitDto
import jakarta.ws.rs.core.Response
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.time.format.DateTimeFormatter

private val passwordButtonStyles: Set<String> = setOf(
    "flex",
    "w-full",
    "justify-center",
    "rounded-md",
    "bg-indigo-600",
    "px-3",
    "py-1.5",
    "text-sm/6",
    "font-semibold",
    "text-white",
    "shadow-sm",
    "hover:bg-indigo-500",
    "focus-visible:outline",
    "focus-visible:outline-2",
    "focus-visible:outline-offset-2",
    "focus-visible:outline-indigo-600"
)

private fun rowClass(index: Int): Set<String> = when (index % 2 == 0) {
    true -> setOf("bg-slate-100")
    false -> setOf("bg-slate-300")
}

private val displayDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val displayDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun renderPasswordFlow() = createHTML().div {
    classes = setOf("mt-10", "sm:mx-auto", "sm:w-full", "sm:max-w-sm")

    id = "first-time-setup"
    p { +"It seems like this is your first visit. Please set an admin password to proceed." }
    form {
        classes = setOf("space-y-6")

        attributes["hx-post"] = "/admin/render/register"
        attributes["hx-swap"] = "outerHTML"
        attributes["hx-target"] = "#first-time-setup"
        attributes["hx-trigger"] = "submit"

        div {
            div {
                classes = setOf("flex", "items-center", "justify-between")

                label {
                    classes = setOf("block", "text-sm/6", "font-medium", "text-gray-900")

                    attributes["for"] = "password"

                    +"Password: "
                }
            }
            div {
                classes = setOf("mt-2")

                input(type = InputType.password) {
                    classes = setOf(
                        "block",
                        "w-full",
                        "rounded-md",
                        "border-0",
                        "py-1.5",
                        "text-gray-900",
                        "shadow-sm",
                        "ring-1",
                        "ring-inset",
                        "ring-gray-300",
                        "placeholder:text-gray-400",
                        "focus:ring-2",
                        "focus:ring-inset",
                        "focus:ring-indigo-600",
                        "sm:text-sm/6"
                    )

                    id = "password"
                    name = "password"
                    autoComplete = true
                    required = true
                }
            }
        }
        div {
            input(type = InputType.submit) {
                classes = passwordButtonStyles

                attributes["hx-post"] = "/admin/render/register"

                value = "Set password"
            }
        }

    }
}

fun renderPasswordFlowSuccess() = createHTML().div {
    classes = setOf("mt-10", "sm:mx-auto", "sm:w-full", "sm:max-w-sm")

    id = "first-time-setup"
    p { +"Successfully set password!" }
    div {
        div {
            input(type = InputType.submit) {
                classes = passwordButtonStyles
                onClick = "window.location.href = '/'"

                value = "Go back"
            }
        }

    }
}

fun renderDashboard() = createHTML().main {
    id = "main-content"

    contentHeader("Dashboard")
    contentDiv {
        p { +"Welcome to kamifusen. Here you can manage your pages and statistics." }
    }
}

fun renderStats(pageVisits: List<PageVisitDto>) = createHTML().main {
    id = "main-content"

    contentHeader("Stats")
    contentDiv {
        table {
            classes = setOf("table-auto", "rounded-md")

            thead {
                tr {
                    styledTh { +"Path" }
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
                        styledTd { +visit.visits.toString() }
                        styledTd { +visit.pageAdded.format(displayDateTimeFormat) }
                    }
                }
            }
        }
    }
}

fun renderPages(pages: List<Page>) = createHTML().main {
    id = "main-content"

    contentHeader("Pages")
    contentDiv {
        form {

            table {
                classes = setOf("table-auto", "rounded-md")

                thead {
                    tr {
                        styledTh { +"Path" }
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
                                        attributes["hx-post"] = "/admin/render/pagedel/${page.id}"
                                        attributes["hx-confirm"] = "This will remove tracking for this page. Do you want to proceed?"

                                        span {
                                            classes = setOf("tabler--world-cancel")
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
                                input(InputType.text) {
                                    classes = setOf("table-input-inline", "h-8")

                                    name = "path"
                                    required = true
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
                                    attributes["hx-post"] = "/admin/render/pageadd"

                                    span {
                                        classes = setOf("tabler--world-plus")
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

fun renderUserManagement(users: List<ApiUser>) = createHTML().main {
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
                                            attributes["hx-post"] = "/admin/render/retire/${user.id}"
                                            attributes["hx-swap"] = "outerHTML"
                                            attributes["hx-target"] = "#user-${user.id.toString()}"
                                            attributes["hx-confirm"] = "This will revoke any access granted by this API Key. Do you want to proceed?"

                                            span {
                                                classes = setOf("tabler--key-off")
                                            }

                                            span {
                                                classes = setOf("sr-only")
                                                p { +"Retire user" }
                                            }
                                        } else {
                                            attributes["hx-post"] = "/admin/render/refresh/${user.id}"
                                            attributes["hx-swap"] = "outerHTML"
                                            attributes["hx-target"] = "#user-${user.id.toString()}"

                                            span {
                                                classes = setOf("tabler--refresh")
                                            }

                                            span {
                                                classes = setOf("sr-only")
                                                p { + "Regenerate" }
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
                                input(InputType.text) {
                                    classes = setOf("table-input-inline", "h-8")

                                    name = "username"
                                    required = true
                                }
                            }
                        }
                        td {
                            div {
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
                                    attributes["hx-post"] = "/admin/render/keygen"

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

fun renderCreatedApiKey(keyRaw: String) = createHTML().button {
    button {
        id = "key"
        classes = setOf("flex", "w-full")

        onClick = "navigator.clipboard.writeText('$keyRaw')"

        span {
            classes = setOf("tabler--key-filled")
        }

        span {
            classes = setOf("sr-only")
            p { +"API Key: " }
        }

        p {
            +"Copy to clipboard"
        }
    }
}

fun createErrorDiv(function: () -> Response.Status): String = createHTML().div {
    id = "error"
    h1 { +function().reasonPhrase }
    p { +"Whoops. Something went wrong." }
    button {
        onClick = "window.location.href = '/'"
        +"Go back"
    }
}
