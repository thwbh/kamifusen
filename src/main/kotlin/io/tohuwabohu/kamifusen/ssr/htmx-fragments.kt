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
        table {
            classes = setOf("table-auto", "rounded-md")

            thead {
                tr {
                    styledTh { +"Path" }
                    styledTh { +"Last Hit" }
                    styledTh { +"Added" }
                }
            }
            tbody {
                pages.forEachIndexed { index, visit ->
                    tr {
                        classes = when (index % 2 == 0) {
                            true -> setOf("bg-slate-100")
                            false -> setOf("bg-slate-300")
                        }

                        styledTd { +visit.path }
                        styledTd {
                            when (visit.lastHit) {
                                null -> +"-"
                                else -> visit.lastHit!!.format(displayDateFormat)
                            }
                        }
                        styledTd { +visit.pageAdded.format(displayDateTimeFormat) }
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
        table {
            classes = setOf("table-auto", "rounded-md")

            thead {
                tr {
                    styledTh { +"Username" }
                    styledTh { +"Role" }
                    styledTh { +"Added" }
                    styledTh { +"Expires" }
                    styledTh { +"Actions" }
                }
            }

            tbody {
                users.forEachIndexed { index, user ->
                    tr {
                        classes = when (index % 2 == 0) {
                            true -> setOf("bg-slate-100")
                            false -> setOf("bg-slate-300")
                        }

                        styledTd { +user.username }
                        styledTd { +user.role }
                        styledTd {
                            when (user.expiresAt) {
                                null -> +"-"
                                else -> user.expiresAt!!.format(displayDateFormat)
                            }
                        }
                        styledTd {
                            when (user.added) {
                                null -> +"-"
                                else -> user.added!!.format(displayDateFormat)
                            }
                        }
                        td {
                            button {
                                attributes["hx-post"] = "/admin/user/retire/${user.id}"
                            }
                        }
                    }
                }
            }
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
