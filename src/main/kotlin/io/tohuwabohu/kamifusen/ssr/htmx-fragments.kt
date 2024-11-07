package io.tohuwabohu.kamifusen.ssr

import io.tohuwabohu.kamifusen.crud.security.PageValidation
import io.tohuwabohu.kamifusen.crud.security.PasswordValidation
import io.tohuwabohu.kamifusen.crud.security.UserValidation
import jakarta.ws.rs.core.Response
import kotlinx.html.*
import kotlinx.html.stream.createHTML

/**
 * Renders the HTML content for the password setup flow based on the given validation result.
 * The generated content includes forms for password entry and messages for different validation outcomes.
 *
 * @param validationResult An optional validation result of type [PasswordValidation]. If provided and the validation is not successful,
 *                         an appropriate error message is displayed in the generated HTML content.
 */
fun renderPasswordFlow(validationResult: PasswordValidation?) = createHTML().div {
    passwordFlow(validationResult)
}

/**
 * Renders an HTML button for the provided API key, allowing users to copy the key to their clipboard.
 *
 * @param keyRaw The raw API key string to be rendered in the button's click event.
 */
fun renderCreatedApiKey(keyRaw: String) = createHTML().button {
    button {
        id = "key"
        classes = setOf("flex", "w-full", "p-2")

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

/**
 * Renders the username validation error message and input field with error styling.
 *
 * @param validationResult The result of the username validation process containing the validation status and message.
 */
fun renderUsernameValidationError(validationResult: UserValidation) = createHTML().div {
    id = "username"

    p ("sr-only") {
        +validationResult.message!!
    }

    input(InputType.text) {
        classes = setOf("table-input-inline", "h-8", "!bg-red-200")
        onFocus = "this.classList.remove('!bg-red-200')"
        name = "username"
        required = true

        placeholder = validationResult.message!!
    }
}

fun renderPageValidationError(validationResult: PageValidation) = createHTML().div {
    id = "page"

    p ("sr-only") {
        +validationResult.message!!
    }

    input(InputType.text) {
        classes = setOf("table-input-inline", "h-8", "!bg-red-200")
        onFocus = "this.classList.remove('!bg-red-200')"
        name = "path"
        required = true

        placeholder = validationResult.message!!
    }
}

fun renderError(function: () -> Response.Status): String = createHTML().div {
    id = "error"
    h1 { +function().reasonPhrase }
    p { +"Whoops. Something went wrong." }
    button {
        onClick = "window.location.href = '/'"
        +"Go back"
    }
}
