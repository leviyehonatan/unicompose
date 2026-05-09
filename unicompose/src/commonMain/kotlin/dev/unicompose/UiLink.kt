package dev.unicompose

import androidx.compose.runtime.Composable
import dev.unicompose.style.Style

/**
 * A hyperlink to an external or in-app destination.
 *
 * Backed by:
 *  - A semantic `<a href="…">` element on Compose HTML — keyboard-focusable,
 *    follows browser conventions (Cmd/Ctrl-click opens new tab, hover shows URL,
 *    crawlers see the link target).
 *  - A clickable `Text` on Compose Multiplatform that calls a platform-appropriate
 *    URI-opener via `LocalUriHandler` when activated.
 *
 * The link is styled by [style] and renders [text] as its label.
 *
 * @param text Visible link label.
 * @param href Destination URL. Use absolute URLs for external links; relative
 *   paths are honored by the browser on web but ignored on CMP.
 * @param style Visual styling. Defaults to no styling — typical use applies an
 *   accent color and underline-equivalent at the call site.
 *
 * @sample
 * ```
 * UiLink(
 *     text = "Read the docs",
 *     href = "https://github.com/leviyehonatan/unicompose",
 *     style = Style(color = rgb(0x35, 0x6D, 0xF5)),
 * )
 * ```
 */
@Composable
public expect fun UiLink(text: String, href: String, style: Style = Style.Empty)
