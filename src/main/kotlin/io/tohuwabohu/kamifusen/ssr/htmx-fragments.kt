package io.tohuwabohu.kamifusen.ssr

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

fun renderPasswordFlow() =
    createHTML().div {
        classes = setOf("mt-10", "sm:mx-auto", "sm:w-full", "sm:max-w-sm")

        id = "first-time-setup"
        p { +"It seems like this is your first visit. Please set an admin password to proceed." }
        form {
            classes = setOf("space-y-6")

            attributes["hx-post"] = "/admin/register"
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

                    attributes["hx-post"] = "/admin/register"

                    value = "Set password"
                }
            }

        }
    }.toString()

fun renderPasswordFlowSuccess() =
    createHTML().div {
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
    }.toString()

fun renderStats(pageVisits: List<PageVisitDto>) =
    createHTML().main {
        id = "main-content"

        contentHeader("Stats")

        div {
            classes = setOf("mx-auto", "max-w-7xl", "px-4", "py-6", "sm:px-6", "lg:px-8")

            table {
                thead {
                    tr {
                        th {
                            +"Path"
                        }
                        th {
                            +"Visits"
                        }
                        th {
                            +"Added"
                        }
                    }
                }
                tbody {
                    pageVisits.forEach { visit ->
                        tr {
                            td { +visit.path }
                            td { +visit.visits.toString() }
                            td { +visit.pageAdded.format(DateTimeFormatter.ISO_DATE_TIME) }
                        }
                    }
                }
            }
        }

    }.toString()

fun createErrorDiv(function: () -> Response.Status): String =
    createHTML().div {
        id = "error"
        h1 { +function().reasonPhrase }
        p { +"Whoops. Something went wrong." }
        button {
            onClick = "window.location.href = '/'"
            +"Go back"
        }
    }.toString()
