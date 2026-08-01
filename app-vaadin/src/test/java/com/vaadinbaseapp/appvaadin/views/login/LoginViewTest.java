package com.vaadinbaseapp.appvaadin.views.login;

import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadinbaseapp.appvaadin.dto.ApiError;
import com.vaadinbaseapp.appvaadin.dto.TokenResponse;
import com.vaadinbaseapp.appvaadin.facade.AuthFacade;
import com.vaadinbaseapp.appvaadin.testutil.KaribuTestSetup;
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
import static org.mockito.Mockito.when;

class LoginViewTest {

    private AuthFacade authFacade;
    private LoginView loginView;

    @BeforeEach
    void setUp() {
        KaribuTestSetup.setupProductionMode();
        authFacade = Mockito.mock(AuthFacade.class);
        loginView = new LoginView(authFacade, "");
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

    @Test
    void accountFlaggedMustResetPasswordShowsTheForcedResetDialogInsteadOfNavigating() {
        when(authFacade.login("admin", "admin"))
                .thenReturn(new TokenResponse(null, null, true, "raw-reset-token", "Bearer"));

        _setValue(_get(loginView, TextField.class), "admin");
        _setValue(_get(loginView, PasswordField.class), "admin");
        _click(_get(loginView, Button.class));

        com.vaadin.flow.component.dialog.Dialog dialog =
                _get(com.vaadin.flow.component.dialog.Dialog.class);
        assertThat(dialog.isOpened()).isTrue();
    }
}
