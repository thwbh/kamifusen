package io.tohuwabohu.kamifusen.ssr

import io.tohuwabohu.kamifusen.crud.security.PasswordValidation
import kotlinx.html.*

private val passwordButtonStyles: Set<String> = setOf(
    "flex",
    "w-full",
    "justify-center",
    "rounded-md",
    "bg-gray-800",
    "px-3",
    "py-1.5",
    "text-sm/6",
    "font-semibold",
    "text-white",
    "shadow-sm",
    "hover:bg-indigo-700",
    "focus-visible:outline",
    "focus-visible:outline-2",
    "focus-visible:outline-offset-2",
    "focus-visible:outline-indigo-900"
)

private val passwordInputStyles: Set<String> = setOf(
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

/**
 * Creates a header section with the given heading text.
 *
 * @param headingText The text to be displayed as the header's main heading.
 */
fun MAIN.contentHeader(headingText: String) = header {
    classes = setOf("bg-white", "shadow")

    div {
        classes = setOf("mx-auto", "max-w-7xl", "px-4", "py-6", "sm:px-6", "lg:px-8")

        h1 {
            id = "content-heading"

            classes = setOf("text-3xl", "font-bold", "tracking-tight", "text-gray-900")

            +headingText
        }
    }
}

/**
 * Renders a div element with predefined CSS classes for styling and layout.
 *
 * @param block A lambda function containing the content to be placed inside the div element.
 */
fun MAIN.contentDiv(block: () -> Unit) = div {
    classes = setOf("mx-auto", "max-w-7xl", "px-4", "py-6", "sm:px-6", "lg:px-8")

    block()
}

/**
 * Adds styled table header (th) cells with predefined classes for consistent styling.
 *
 * @param block A lambda function that defines the content to be placed inside the th element.
 */
fun TR.styledTh(block: () -> Unit) = th {
    classes = setOf("bg-gray-800", "px-3", "py-2", "text-sm", "font-medium", "text-white")

    block()
}

/**
 * Creates a styled table cell (`td`) element with predefined CSS classes and applies the provided content block to it.
 *
 * @param block A lambda function that represents the content to be nested within the `td` element.
 */
fun TR.styledTd(block: () -> Unit) = td {
    classes = setOf("px-3", "py-2", "text-sm", "font-medium")

    block()
}

/**
 * Handles the password setup flow within the application's first-time setup process.
 *
 * Displays a form for creating an admin password if validation is not successful. If the password
 * has been successfully updated, it displays a success message along with a button to return to the login page.
 *
 * @param validation An optional validation result that indicates whether the password is valid.
 *                   If validation is provided and is not valid, an error message is displayed.
 */
fun FlowContent.passwordFlow(validation: PasswordValidation? = null) = div {
    classes = setOf("mt-10", "sm:mx-auto", "sm:w-full", "sm:max-w-sm")

    id = "first-time-setup"

    h2 {
        classes = setOf("text-2xl", "font-bold", "tracking-tight", "text-gray-900")

        +"First time setup"
    }

    if (validation?.valid == true) {
        p { +"Successfully updated password!" }
        div {
            div {
                button {
                    classes = passwordButtonStyles

                    attributes["hx-post"] = "/logout"

                    +"Back to login"
                }
            }
        }
    } else {
        p { +"It seems like this is your first visit. Please set an admin password to proceed." }

        form {
            classes = setOf("space-y-6")

            attributes["hx-post"] = "/fragment/register"
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
                        classes = passwordInputStyles

                        id = "password"
                        name = "password"
                        required = true
                    }
                }

                div {
                    classes = setOf("flex", "items-center", "justify-between")

                    label {
                        classes = setOf("block", "text-sm/6", "font-medium", "text-gray-900")

                        attributes["for"] = "password-confirm"

                        +"Confirm password: "
                    }
                }

                div {
                    classes = setOf("mt-2")

                    input(type = InputType.password) {
                        classes = passwordInputStyles

                        id = "password-confirm"
                        name = "password-confirm"
                        required = true
                    }
                }
            }
            div {
                button {
                    classes = passwordButtonStyles

                    type = ButtonType.submit

                    +"Set password"
                }
            }

            if (validation != null && !validation.valid) {
                div {
                    classes = setOf("text-sm/6", "text-red-600")

                    p { +validation.message!! }
                }
            }
        }
    }
}