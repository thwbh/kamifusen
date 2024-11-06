package io.tohuwabohu.kamifusen.ssr

import kotlinx.html.*
import kotlinx.html.classes
import kotlinx.html.stream.createHTML

private val navActiveClasses = setOf(
    "rounded-md",
    "px-3",
    "py-2",
    "text-sm",
    "font-medium",
    "text-gray-300",
    "hover:bg-gray-700",
    "hover:text-white",
    "bg-gray-900",
)

private val navInactiveClasses = setOf(
    "rounded-md",
    "px-3",
    "py-2",
    "text-sm",
    "font-medium",
    "text-gray-300",
    "hover:bg-gray-700",
    "hover:text-white"
)

fun renderAdminPage(navId: String, isFirstTimeSetup: Boolean = false, block: FlowContent.() -> Unit) = createHTML().html {
    attributes["lang"] = "EN"
    classes = setOf("h-full", "bg-gray-100")

    head {
        title("kamifusen - $navId")
        script(src = "/scripts/htmx.min.js") {}
        link(rel = "stylesheet", href = "/styles/main.css")
    }

    body {
        classes = setOf("h-full")

        div {
            classes = setOf("min-h-full")

            renderNavigation(navId, isFirstTimeSetup)

            block()
        }
    }
}

private fun FlowContent.renderNavigation(navId: String, isFirstTimeSetup: Boolean = false) =
    nav {
        classes = setOf("bg-gray-800")

        div {
            classes = setOf("mx-auto", "max-w-7xl", "px-4", "sm:px-6", "lg:px-8")

            div {
                classes = setOf("flex", "h-16", "items-center", "justify-between")

                div {
                    classes = setOf("flex", "items-center")

                    div {
                        classes = setOf("flex-shrink-0")
                        style = "min-width: 32px;"
                        img(classes = "h-8 w-auto", src = "/static/images/kamifusen-logo.png", alt = "kamifusen logo")
                    }

                    div {
                        classes = setOf("block")

                        if (!isFirstTimeSetup) {
                            div {
                                classes = setOf("ml-10", "flex", "items-baseline", "space-x-4")

                                a(href = "/dashboard") {
                                    classes = when (navId) {
                                        "Dashboard" -> navActiveClasses
                                        else -> navInactiveClasses
                                    }

                                    +"Dashboard"
                                }

                                a(href = "/pages") {
                                    classes = when (navId) {
                                        "Pages" -> navActiveClasses
                                        else -> navInactiveClasses
                                    }

                                    +"Pages"
                                }

                                a(href = "/users") {
                                    classes = when (navId) {
                                        "Users" -> navActiveClasses
                                        else -> navInactiveClasses
                                    }

                                    +"Users"
                                }

                                a(href = "/stats") {
                                    classes = when (navId) {
                                        "Stats" -> navActiveClasses
                                        else -> navInactiveClasses
                                    }

                                    +"Stats"
                                }
                            }
                        }
                    }
                }

                div {
                    classes = setOf("block")

                    div {
                        classes = setOf("ml-4", "flex", "items-center", "md:ml-6")

                        button(type = ButtonType.button) {
                            classes = setOf(
                                "relative",
                                "rounded-full",
                                "bg-gray-800",
                                "p-1",
                                "text-gray-400",
                                "hover:text-white",
                                "focus:outline-none",
                                "focus:ring-2",
                                "focus:ring-white",
                                "focus:ring-offset-2",
                                "focus:ring-offset-gray-800"
                            )

                            span {
                                classes = setOf("absolute", "-inset-1.5")
                            }
                            span {
                                classes = setOf("sr-only")
                                +"View notifications"
                            }
                        }

                        div {
                            classes = setOf("ml-10", "flex", "items-baseline", "space-x-4")
                            a(href = "#") {
                                classes = navInactiveClasses

                                attributes["hx-post"] = "/logout"

                                +"Logout"
                            }
                        }
                    }
                }
            }
        }
    }