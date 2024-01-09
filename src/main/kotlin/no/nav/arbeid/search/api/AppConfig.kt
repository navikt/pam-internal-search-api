package no.nav.arbeid.search.api

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.conn.ssl.DefaultHostnameVerifier
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.opensearch.client.RestClient
import org.opensearch.client.RestClientBuilder
import org.slf4j.LoggerFactory
import java.net.URL


@Factory
class AppConfig()  {
    companion object{
        private val LOG = LoggerFactory.getLogger(AppConfig::class.java)
    }

    @Singleton
    fun safeElasticClientBuilder(@Value("\${elasticsearch.url}") elasticsearchUrl: URL? = null,
                                 @Value("\${elasticsearch.user:foo}") user: String,
                                 @Value("\${elasticsearch.password:bar}") password: String): RestClientBuilder {

        val credentialsProvider = BasicCredentialsProvider().apply {
            LOG.info("Connecting opensearch using $elasticsearchUrl and user $user")
            setCredentials(AuthScope.ANY, UsernamePasswordCredentials(user, password))
        }

        return RestClient.builder(HttpHost.create(elasticsearchUrl.toString()))
                .setRequestConfigCallback { requestConfigBuilder: RequestConfig.Builder ->
                    requestConfigBuilder
                            .setConnectionRequestTimeout(5000)
                            .setConnectTimeout(10000)
                            .setSocketTimeout(20000)
                }
                .setHttpClientConfigCallback { httpAsyncClientBuilder: HttpAsyncClientBuilder ->
                    httpAsyncClientBuilder // Fix SSL hostname verification for *.local domains:
                            .setSSLHostnameVerifier(DefaultHostnameVerifier())
                            .setMaxConnTotal(256)
                            .setMaxConnPerRoute(256)
                            .setDefaultCredentialsProvider(credentialsProvider)
                }
    }
}
