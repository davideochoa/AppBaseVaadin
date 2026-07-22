package com.appbasevaadin.msusers.controller;

import com.appbasevaadin.msusers.dto.UserRequest;
import com.appbasevaadin.msusers.entity.UserType;
import com.appbasevaadin.msusers.repository.UserTypeRepository;
import com.appbasevaadin.msusers.support.PostgresTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIT extends PostgresTestContainerBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserTypeRepository userTypeRepository;

    private Long userTypeId;

    @BeforeEach
    void setUp() {
        userTypeId = userTypeRepository.findAll().stream()
                .filter(t -> t.getName().equals("User"))
                .findFirst()
                .orElseGet(() -> {
                    UserType type = new UserType();
                    type.setName("User");
                    type.setDescription("Standard access");
                    return userTypeRepository.save(type);
                })
                .getId();
    }

    private UserRequest buildValidRequest() {
        UserRequest request = new UserRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe." + System.nanoTime() + "@example.com");
        request.setUserTypeId(userTypeId);
        return request;
    }

    @Test
    void fullUserCrudFlow() {
        UserRequest request = buildValidRequest();

        ResponseEntity<String> created = restTemplate.postForEntity("/users", request, String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).contains(request.getEmail());
    }

    @Test
    void createWithInvalidEmailReturns400() {
        UserRequest request = buildValidRequest();
        request.setEmail("not-an-email");

        ResponseEntity<String> response = restTemplate.postForEntity("/users", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("VALIDATION");
    }

    @Test
    void getNonexistentUserReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/users/999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("NOT_FOUND");
    }

    @Test
    void createWithDuplicateEmailReturns409() {
        UserRequest request = buildValidRequest();
        restTemplate.postForEntity("/users", request, String.class);

        ResponseEntity<String> secondResponse = restTemplate.postForEntity("/users", request, String.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void listUserTypesIncludesAdministratorAndUser() {
        ResponseEntity<String> response = restTemplate.getForEntity("/user-types", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Administrator").contains("User");
    }
}
