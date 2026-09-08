package no.nav.arbeid.search.api

import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.util.Timeout
import org.opensearch.client.transport.OpenSearchTransport
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder
import java.net.URI

private const val CONNECTION_REQUEST_TIMEOUT_MS = 5000L
private const val CONNECT_TIMEOUT_MS = 10000L
private const val RESPONSE_TIMEOUT_MS = 20000L
private const val MAX_CONNECTIONS = 256

data class AppConfig(
    val serverPort: Int = env("SERVER_PORT", "9027").toInt(),
    val openSearchUri: String = env("OPEN_SEARCH_URI", "http://localhost:9200"),
    val openSearchUsername: String = env("OPEN_SEARCH_USERNAME", "foo"),
    val openSearchPassword: String = env("OPEN_SEARCH_PASSWORD", "bar")
)

private fun env(name: String, default: String) = System.getenv(name) ?: default

fun openSearchTransport(config: AppConfig): OpenSearchTransport {
    val credentialsProvider = BasicCredentialsProvider().apply {
        setCredentials(
            AuthScope(null, null, -1, null, null),
            UsernamePasswordCredentials(config.openSearchUsername, config.openSearchPassword.toCharArray())
        )
    }

    return ApacheHttpClient5TransportBuilder.builder(HttpHost.create(URI.create(config.openSearchUri)))
        .setRequestConfigCallback { requestConfigBuilder ->
            requestConfigBuilder
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(CONNECTION_REQUEST_TIMEOUT_MS))
                .setResponseTimeout(Timeout.ofMilliseconds(RESPONSE_TIMEOUT_MS))
        }
        .setHttpClientConfigCallback { httpAsyncClientBuilder ->
            httpAsyncClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)
                .setConnectionManager(
                    PoolingAsyncClientConnectionManagerBuilder.create()
                        // Fix SSL hostname verification for *.local domains:
                        .setTlsStrategy(
                            ClientTlsStrategyBuilder.create()
                                .setHostnameVerifier(DefaultHostnameVerifier())
                                .buildAsync()
                        )
                        .setMaxConnTotal(MAX_CONNECTIONS)
                        .setMaxConnPerRoute(MAX_CONNECTIONS)
                        .setDefaultConnectionConfig(
                            ConnectionConfig.custom()
                                .setConnectTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT_MS))
                                .build()
                        )
                        .build()
                )
        }
        .build()
}
