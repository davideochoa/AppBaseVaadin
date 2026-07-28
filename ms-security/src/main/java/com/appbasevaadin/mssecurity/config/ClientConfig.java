package com.appbasevaadin.mssecurity.config;

import com.appbasevaadin.mssecurity.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Builds the outbound {@link RestClient}s this service depends on, so components like
 * {@link com.appbasevaadin.mssecurity.client.UsersClient} receive an already-configured client
 * instead of constructing their own — the "how do I reach ms-users over HTTP" concern lives here,
 * not inside the class that uses it.
 */
@Configuration
public class ClientConfig {

    @Bean
    public RestClient usersRestClient(@Value("${app.clients.ms-users-base-url}") String msUsersBaseUrl,
                                       JwtService jwtService) {
        return RestClient.builder()
                .baseUrl(msUsersBaseUrl)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(jwtService.generateServiceToken());
                    return execution.execute(request, body);
                })
                .build();
    }
}
