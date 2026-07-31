package com.appbasevaadin.appvaadin.views.login;

import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.appbasevaadin.appvaadin.client.ApiException;
import com.appbasevaadin.appvaadin.dto.ApiError;
import com.appbasevaadin.appvaadin.facade.AuthFacade;
import com.appbasevaadin.appvaadin.facade.SecurityUserFacade;
import com.appbasevaadin.appvaadin.testutil.KaribuTestSetup;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class LoginViewTest {

    private AuthFacade authFacade;
    private LoginView loginView;

    @BeforeEach
    void setUp() {
        KaribuTestSetup.setupProductionMode();
        authFacade = Mockito.mock(AuthFacade.class);
        AuthenticatedUser authenticatedUser = Mockito.mock(AuthenticatedUser.class);
        SecurityUserFacade securityUserFacade = Mockito.mock(SecurityUserFacade.class);
        loginView = new LoginView(authFacade, authenticatedUser, securityUserFacade, "");
        com.vaadin.flow.component.UI.getCurrent().add(loginView);
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void invalidCredentialsShowAnErrorMessageAndDoNotNavigate() {
        ApiError apiError = new ApiError(LocalDateTime.now(), 401, "UNAUTHORIZED", "Invalid username or password",
                List.of());
        doThrow(new ApiException(apiError)).when(authFacade).login(eq("jane.doe"), eq("wrong"));

        _setValue(_get(loginView, TextField.class), "jane.doe");
        _setValue(_get(loginView, PasswordField.class), "wrong");
        _click(_get(loginView, Button.class));

        verify(authFacade).login("jane.doe", "wrong");
        Span errorMessage = _get(loginView, Span.class);
        assertThat(errorMessage.isVisible()).isTrue();
    }
}
