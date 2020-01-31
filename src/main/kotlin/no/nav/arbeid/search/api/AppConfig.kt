package no.nav.arbeid.search.api

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Configuration
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.apache.http.conn.ssl.DefaultHostnameVerifier
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import java.net.URL
import javax.inject.Singleton

@Factory
class AppConfig()  {

    @Singleton
    fun safeElasticClientBuilder(@Value("\${elasticsearch.url}") elasticsearchUrl: URL? = null): RestClientBuilder {
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
                }
    }
}