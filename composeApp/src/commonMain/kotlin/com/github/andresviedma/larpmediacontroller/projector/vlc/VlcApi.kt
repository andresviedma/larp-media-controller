package com.github.andresviedma.larpmediacontroller.projector.vlc

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.io.Closeable

@OptIn(ExperimentalXmlUtilApi::class)
class VlcApi(
    val host: String,
    val port: Int = 8080,
    val userName: String = "",
    val password: String = "x",
) : Closeable {
    private val client: HttpClient = createClient()

    suspend fun getStatus(): VlcStatus =
        sendCommand(null)

    suspend fun play(file: String) = sendCommand("in_play", input = file)
    suspend fun enqueue(file: String, loop: Boolean = false) = sendCommand("in_enqueue", input = file)
    suspend fun clearQueue() = sendCommand("pl_empty")
    suspend fun stop() = sendCommand("pl_stop")
    suspend fun toggleFullScreen() = sendCommand("fullscreen")
    suspend fun toggleLoop() = sendCommand("pl_loop")
    suspend fun toggleRepeat() = sendCommand("pl_repeat")
    suspend fun setVolume(value: Int) = sendCommand("volume", value = value)
    suspend fun mute() = setVolume(0)
    suspend fun setMaxVolume() = setVolume(320)

    override fun close() {
        client.close()
    }

    suspend fun sendCommand(command: String?, input: String? = null, value: Any? = null): VlcStatus =
        client.get("http://$host:$port/requests/status.xml") {
            command?.let { parameter("command", it) }
            input?.let { parameter("input", it) }
            value?.let { parameter("val", it) }
        }.body()

    private fun createClient() = let { self ->
        HttpClient(CIO) {
            expectSuccess = true

            install(ContentNegotiation) {
                xml(
                    format = XML {
                        defaultPolicy {
                            unknownChildHandler = IGNORING_UNKNOWN_CHILD_HANDLER
                        }
                    },
                    contentType = ContentType.Text.Xml
                )
            }

            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = self.userName, password = self.password)
                    }
                }
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                retryOnException(maxRetries = 3)
                exponentialDelay()
            }
        }
    }
}

@Serializable
@XmlSerialName(value = "root")
data class VlcStatus(
    @XmlElement val fullscreen: String,
    @XmlElement val repeat: Boolean,
    @XmlElement val loop: Boolean,
    @XmlElement val state: String,
    @XmlElement val random: Boolean,
    @XmlElement val position: String,
    @XmlElement val length: String,
    @XmlElement val volume: Int,
) {
    inline val isFullScreen: Boolean get() = (fullscreen == "1" || fullscreen == "true")
    inline val isMuted: Boolean get() = (volume == 0)
}
