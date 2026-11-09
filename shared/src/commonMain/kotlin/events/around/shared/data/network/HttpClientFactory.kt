package events.around.shared.data.network

import co.touchlab.kermit.Logger
import events.around.shared.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Supplies the `Authorization` header value, or null when nobody is signed in. */
fun interface AuthorizationProvider {
    fun authorization(): String?
}

private const val CONNECT_TIMEOUT_MILLIS = 15_000L
private const val REQUEST_TIMEOUT_MILLIS = 60_000L

val apiJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}

fun createHttpClient(
    engine: HttpClientEngine,
    config: AppConfig,
    authorization: AuthorizationProvider,
): HttpClient = HttpClient(engine) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(apiJson)
    }

    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        // Long enough for a poster upload on a slow connection.
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }

    // Runs for every request, so the signature is read fresh after sign-in and sign-out.
    install(DefaultRequest) {
        url(config.baseUrl.trimEnd('/') + "/")
        headers.append(HttpHeaders.Accept, "application/json")
        authorization.authorization()?.let { headers.append(HttpHeaders.Authorization, it) }
    }

    if (config.isDebug) {
        install(Logging) {
            level = LogLevel.INFO
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Logger.withTag("http").d { message }
                }
            }
        }
    }
}
