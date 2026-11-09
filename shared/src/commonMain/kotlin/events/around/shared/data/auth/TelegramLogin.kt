package events.around.shared.data.auth

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.formUrlEncode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * A signed Telegram login: what the login widget hands back, kept verbatim as [signature] because the server
 * recomputes the hash over exactly those pairs.
 */
@Serializable
data class TelegramLogin(
    val userId: Long,
    val username: String? = null,
    val firstName: String? = null,
    val photoUrl: String? = null,
    /** Unix seconds; the server rejects the signature a day after this. */
    val authDate: Long,
    /** `id=…&first_name=…&auth_date=…&hash=…`, sent as `Authorization: tglogin <signature>`. */
    val signature: String,
) {
    val displayName: String get() = username?.let { "@$it" } ?: firstName ?: "id$userId"

    fun isExpired(nowEpochSeconds: Long): Boolean = nowEpochSeconds - authDate >= MAX_AGE_SECONDS

    companion object {
        /** As long as the backend accepts it (`TelegramInitData::parseLoginWidget`). */
        const val MAX_AGE_SECONDS: Long = 24 * 60 * 60
    }
}

/**
 * Telegram's login page, opened in an in-app web view.
 *
 * Without a window opener Telegram returns to [returnTo] with `#tgAuthResult=<base64url JSON>` — the same
 * mechanism `telegram-widget.js` reads in `haveTgAuthResult()`. The origin must be the domain bound to the bot
 * with @BotFather `/setdomain`, so it is the site's origin, not the app's.
 */
object TelegramLoginPage {
    private const val AUTH_URL = "https://oauth.telegram.org/auth"
    private const val RESULT_KEY = "tgAuthResult="

    fun url(botId: Long, siteBaseUrl: String, languageTag: String): String {
        val origin = origin(siteBaseUrl)
        return URLBuilder(AUTH_URL).apply {
            parameters.append("bot_id", botId.toString())
            parameters.append("origin", origin)
            // Lets the bot write first: the moderator's decision arrives as a bot message.
            parameters.append("request_access", "write")
            parameters.append("lang", languageTag.substringBefore('-'))
            parameters.append("return_to", returnTo(siteBaseUrl))
        }.buildString()
    }

    fun returnTo(siteBaseUrl: String): String = origin(siteBaseUrl) + "/"

    /** True when [url] is the redirect back, with or without a result; the web view stops there. */
    fun isReturn(url: String, siteBaseUrl: String): Boolean = url.startsWith(returnTo(siteBaseUrl))

    /** The login inside a redirect URL, or null when the URL carries none (the person closed the page). */
    @OptIn(ExperimentalEncodingApi::class)
    fun parseResult(url: String): TelegramLogin? {
        val start = url.indexOf(RESULT_KEY).takeIf { it >= 0 } ?: return null
        val encoded = url.substring(start + RESULT_KEY.length).substringBefore('&').trimEnd('=')
        if (encoded.isEmpty()) return null

        val json = runCatching {
            Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(encoded).decodeToString()
        }.getOrNull() ?: return null
        val fields = runCatching { Json.parseToJsonElement(json).jsonObject }.getOrNull() ?: return null
        return fromFields(fields)
    }

    internal fun fromFields(fields: JsonObject): TelegramLogin? {
        val pairs = fields.mapNotNull { (key, value) ->
            (value as? JsonPrimitive)?.takeUnless { it.content == "null" }?.let { key to it.content }
        }
        val values = pairs.toMap()
        val userId = values["id"]?.toLongOrNull() ?: return null
        val authDate = values["auth_date"]?.toLongOrNull() ?: return null
        if (values["hash"].isNullOrEmpty()) return null

        return TelegramLogin(
            userId = userId,
            username = values["username"],
            firstName = values["first_name"],
            photoUrl = values["photo_url"],
            authDate = authDate,
            signature = pairs.formUrlEncode(),
        )
    }

    private fun origin(siteBaseUrl: String): String {
        val url = Url(siteBaseUrl)
        val defaultPort = url.port == url.protocol.defaultPort
        return "${url.protocol.name}://${url.host}" + if (defaultPort) "" else ":${url.port}"
    }
}
