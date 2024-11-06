package io.tohuwabohu.kamifusen.ssr

import io.tohuwabohu.kamifusen.crud.security.PasswordValidation
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

fun renderError(function: () -> Response.Status): String = createHTML().div {
    id = "error"
    h1 { +function().reasonPhrase }
    p { +"Whoops. Something went wrong." }
    button {
        onClick = "window.location.href = '/'"
        +"Go back"
    }
}
