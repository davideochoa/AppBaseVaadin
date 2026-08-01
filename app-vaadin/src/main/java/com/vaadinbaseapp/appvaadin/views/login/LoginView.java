package com.vaadinbaseapp.appvaadin.views.login;

import com.vaadinbaseapp.appvaadin.auth.AuthenticatedUser;
import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadinbaseapp.appvaadin.facade.AuthFacade;
import com.vaadinbaseapp.appvaadin.facade.SecurityUserFacade;
import com.vaadinbaseapp.appvaadin.views.users.UserListView;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Value;

@Route("login")
@PageTitle("Sign in")
public class LoginView extends VerticalLayout {

    private static final String GOOGLE_BUTTON_CONTAINER_ID = "google-btn-container";

    private final AuthFacade authFacade;
    private final AuthenticatedUser authenticatedUser;
    private final SecurityUserFacade securityUserFacade;

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Span errorMessage = new Span();
    private final Div googleButtonContainer = new Div();

    public LoginView(AuthFacade authFacade, AuthenticatedUser authenticatedUser,
                      SecurityUserFacade securityUserFacade,
                      @Value("${app.google.client-id:}") String googleClientId) {
        this.authFacade = authFacade;
        this.authenticatedUser = authenticatedUser;
        this.securityUserFacade = securityUserFacade;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        buildForm();

        if (googleClientId != null && !googleClientId.isBlank()) {
            buildGoogleButton(googleClientId);
        }
    }

    private void buildForm() {
        usernameField.setLabel(getTranslation("login.username"));
        usernameField.setWidth("20em");
        passwordField.setLabel(getTranslation("login.password"));
        passwordField.setWidth("20em");

        Button submit = new Button(getTranslation("login.submit"), e -> attemptLogin());
        submit.addClickShortcut(Key.ENTER);
        submit.setWidthFull();

        errorMessage.getElement().getThemeList().add("badge error");
        errorMessage.setVisible(false);

        googleButtonContainer.setId(GOOGLE_BUTTON_CONTAINER_ID);

        VerticalLayout formLayout = new VerticalLayout(
                new H1(getTranslation("login.title")), usernameField, passwordField, submit, errorMessage,
                googleButtonContainer);
        formLayout.setAlignItems(Alignment.STRETCH);
        formLayout.setWidth("24em");
        add(formLayout);
    }

    private void attemptLogin() {
        try {
            authFacade.login(usernameField.getValue(), passwordField.getValue());
            proceedAfterLogin();
        } catch (ApiException e) {
            showError(getTranslation("login.error"));
        }
    }

    private void buildGoogleButton(String googleClientId) {
        getElement().executeJs(
                "const s = document.createElement('script');"
                        + "s.src = 'https://accounts.google.com/gsi/client';"
                        + "s.async = true;"
                        + "const self = this;"
                        + "s.onload = () => {"
                        + "  google.accounts.id.initialize({ client_id: $0, callback: (resp) => {"
                        + "    self.$server.onGoogleCredential(resp.credential); } });"
                        + "  google.accounts.id.renderButton("
                        + "    document.getElementById($1), { theme: 'outline', size: 'large' });"
                        + "};"
                        + "document.head.appendChild(s);",
                googleClientId, GOOGLE_BUTTON_CONTAINER_ID);
    }

    @ClientCallable
    private void onGoogleCredential(String idToken) {
        try {
            authFacade.loginWithGoogle(idToken);
            proceedAfterLogin();
        } catch (ApiException e) {
            showError(getTranslation("login.error"));
        }
    }

    private void proceedAfterLogin() {
        if (authenticatedUser.isMustResetPassword()) {
            new ForcePasswordResetDialog(securityUserFacade,
                    () -> UI.getCurrent().navigate(UserListView.class)).open();
        } else {
            UI.getCurrent().navigate(UserListView.class);
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
}
