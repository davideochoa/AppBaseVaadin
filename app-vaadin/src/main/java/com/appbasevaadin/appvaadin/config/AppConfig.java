package com.appbasevaadin.appvaadin.config;

import com.appbasevaadin.appvaadin.auth.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    public RestClient securityRestClient(@Value("${app.clients.ms-security-base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient usersRestClient(@Value("${app.clients.ms-users-base-url}") String baseUrl,
                                       AuthInterceptor authInterceptor) {
        return RestClient.builder().baseUrl(baseUrl).requestInterceptor(authInterceptor).build();
    }

    @Bean
    public RestClient auditRestClient(@Value("${app.clients.ms-audit-base-url}") String baseUrl,
                                       AuthInterceptor authInterceptor) {
        return RestClient.builder().baseUrl(baseUrl).requestInterceptor(authInterceptor).build();
    }
}
