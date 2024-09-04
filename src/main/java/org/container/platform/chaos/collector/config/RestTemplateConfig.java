package org.container.platform.chaos.collector.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
/**
 * RestTemplateConfig 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Rest template rest template
     *
     * @return the rest template
     * @throws KeyStoreException        the key store exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyManagementException   the key management exception
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .additionalInterceptors(clientHttpRequestInterceptor())
                .requestFactory(() -> {
                    try {
                        return httpComponentsClientHttpRequestFactory();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                })
                .build();
    }

    @Bean("shortTimeoutRestTemplate")
    public RestTemplate shortTimeoutRestTemplate() {
        return new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(1))
                .setReadTimeout(Duration.ofSeconds(1))
                .requestFactory(() -> {
                    try {
                        return httpComponentsClientHttpRequestFactory();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                })
                .build();
    }

    @Bean("apiRestTemplate")
    public RestTemplate apiRestTemplate() {
        return new RestTemplateBuilder().setConnectTimeout(Duration.ofMinutes(1))
                .setReadTimeout(Duration.ofMinutes(1))
                .additionalInterceptors(apiRequestInterceptor())
                .requestFactory(() -> {
                    try {
                        return httpComponentsClientHttpRequestFactory();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                })
                .build();
    }

    public ClientHttpRequestInterceptor clientHttpRequestInterceptor() {
        return (request, body, execution) -> {
            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
            try {
                return retryTemplate.execute(context -> execution.execute(request, body));
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException{
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    public ClientHttpRequestInterceptor apiRequestInterceptor() {
        return (request, body, execution) -> {
            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(1));
            try {
                return retryTemplate.execute(context -> execution.execute(request, body));
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

}
