package io.tohuwabohu.kamifusen.ssr

import kotlinx.html.*
import kotlinx.html.attributes.enumEncode

/**
 * Creates a form element within the given HTML flow content block. The form can be configured
 * with various attributes including action, encoding type, and method. Additional attributes related
 * to htmx (hx-post, hx-target, hx-swap, and hx-trigger) can also be specified.
 *
 * @param action Specifies the URL to which the form data will be submitted.
 * @param encType Specifies the encoding type of the form (e.g., application/x-www-form-urlencoded, multipart/form-data).
 * @param method Specifies the HTTP method to use when sending form data (e.g., GET, POST).
 * @param classes Specifies the CSS classes to be applied to the form element.
 * @param hxPost Specifies a htmx attribute for making AJAX requests upon form submission.
 * @param hxTarget Specifies the target element for content replacement via htmx.
 * @param hxSwap Specifies how the response content should be swapped via htmx.
 * @param hxTrigger Specifies the events that trigger the htmx requests.
 * @param block Lambda function defining the content of the form.
 * @return Unit
 */
@HtmlTagMarker
inline fun FlowContent.form(
    action: String? = null,
    encType: FormEncType? = null,
    method: FormMethod? = null,
    classes: String? = null,
    hxPost: String? = null,
    hxTarget: String? = null,
    hxSwap: String? = null,
    hxTrigger: String? = null,
    crossinline block: FORM.() -> Unit = {}
): Unit = FORM(
    attributesMapOf(
        "action",
        action,
        "enctype",
        encType?.enumEncode(),
        "method",
        method?.enumEncode(),
        "class",
        "hx-post",
        hxPost,
        "hx-target",
        hxTarget,
        "hx-swap",
        hxSwap,
        "hx-trigger",
        hxTrigger,
        classes
    ), consumer
).visit(block)
