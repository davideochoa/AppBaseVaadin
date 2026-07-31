package com.appbasevaadin.appvaadin.client;

import com.appbasevaadin.appvaadin.dto.PageResponse;
import com.appbasevaadin.appvaadin.dto.UserRequest;
import com.appbasevaadin.appvaadin.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsersApiClientTest {

    private MockRestServiceServer mockServer;
    private UsersApiClient usersApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ms-users.test");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        usersApiClient = new UsersApiClient(builder.build());
    }

    @Test
    void searchParsesAPagedResponse() {
        mockServer.expect(requestTo("http://ms-users.test/users?page=0&size=20&text=jane"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"content":[{"id":1,"firstName":"Jane","lastName":"Doe","email":"jane@example.com",
                        "active":true,"createdAt":"2026-01-01T10:00:00","userType":{"id":2,"name":"User","description":"d"}}],
                        "totalElements":1,"totalPages":1,"number":0,"size":20}
                        """, MediaType.APPLICATION_JSON));

        PageResponse<UserResponse> result = usersApiClient.search("jane", null, null, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).email()).isEqualTo("jane@example.com");
        assertThat(result.totalElements()).isEqualTo(1);
        mockServer.verify();
    }

    @Test
    void createOnValidationErrorThrowsApiExceptionWithFieldErrors() {
        mockServer.expect(requestTo("http://ms-users.test/users"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"timestamp":"2026-01-01T10:00:00","status":400,"code":"VALIDATION",
                                "message":"The submitted data is invalid",
                                "errors":[{"field":"email","message":"Email format is invalid"}]}
                                """));

        UserRequest request = new UserRequest("jane.doe", "Jane", "Doe", "not-an-email", 2L, true);

        assertThatThrownBy(() -> usersApiClient.create(request))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException apiException = (ApiException) e;
                    assertThat(apiException.getApiError().errors()).hasSize(1);
                    assertThat(apiException.getApiError().errors().get(0).field()).isEqualTo("email");
                });
        mockServer.verify();
    }
}
