package io.tohuwabohu.kamifusen.ssr

import jakarta.ws.rs.core.Response
import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun createPasswordUpdateDiv() =
    createHTML().div {
        id = "first-time-setup"
        h1 { +"First time setup" }
        p { +"It seems like this is your first visit." }
        p { +"Please set an admin password." }
        form {
            attributes["hx-post"] = "/admin/register"
            attributes["hx-swap"] = "outerHTML"
            attributes["hx-target"] = "#first-time-setup"
            attributes["hx-trigger"] = "submit"
            action = "/admin/register"
            method = FormMethod.post

            label {
                +"Password: "
                passwordInput {
                    name = "password"
                    id = "newPassword"
                }
            }

            input(type = InputType.submit) {
                value = "Submit"
            }
        }
    }.toString()

fun createSuccessfulLoginDiv() =
    createHTML().div {
        h1 { +"Admin password set!" }
        p { +"You can now login with your new password." }
        button(type = ButtonType.button) {
            onClick = "window.location.href = '/'"
            +"Go back"
        }
    }.toString()

fun errorDiv(function: () -> Response.Status): String =
    createHTML().div {
        id = "error"
        h1 { +function().reasonPhrase }
        p { +"Whoops. Something went wrong."}
        button {
            onClick = "window.location.href = '/'"
            +"Go back"
        }
    }.toString().trim()