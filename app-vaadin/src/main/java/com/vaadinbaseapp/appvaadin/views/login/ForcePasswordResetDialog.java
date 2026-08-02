package com.vaadinbaseapp.appvaadin.views.login;

import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadinbaseapp.appvaadin.facade.AuthFacade;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;

/**
 * Shown right after a login response comes back flagged mustResetPassword —
 * the account never received a full session (no accessToken/refreshToken),
 * only a single-use resetToken scoped to this flow. Not closable via
 * ESC/outside-click: this isn't optional. On success the caller is expected
 * to send the user back to a fresh login with the new password (no
 * autologin) — this dialog never touches AuthenticatedUser/VaadinSession.
 */
public class ForcePasswordResetDialog extends Dialog {

    private static final int MIN_PASSWORD_LENGTH = 4;

    private final AuthFacade authFacade;
    private final String resetToken;
    private final Runnable onSuccess;

    private final PasswordField newPassword = new PasswordField();
    private final PasswordField confirmPassword = new PasswordField();
    private final Span errorMessage = new Span();

    public ForcePasswordResetDialog(AuthFacade authFacade, String resetToken, Runnable onSuccess) {
        this.authFacade = authFacade;
        this.resetToken = resetToken;
        this.onSuccess = onSuccess;

        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setHeaderTitle(getTranslation("resetPassword.title"));

        newPassword.setLabel(getTranslation("resetPassword.newPassword"));
        newPassword.setWidthFull();
        confirmPassword.setLabel(getTranslation("resetPassword.confirm"));
        confirmPassword.setWidthFull();

        errorMessage.getElement().getThemeList().add("badge error");
        errorMessage.setVisible(false);

        VerticalLayout layout = new VerticalLayout(
                new Paragraph(getTranslation("resetPassword.description")),
                newPassword, confirmPassword, errorMessage);
        layout.setWidthFull();
        add(layout);

        Button submit = new Button(getTranslation("resetPassword.submit"), e -> submit());
        submit.getElement().getThemeList().add("primary");
        getFooter().add(submit);
    }

    private void submit() {
        errorMessage.setVisible(false);

        if (newPassword.getValue() == null || newPassword.getValue().length() < MIN_PASSWORD_LENGTH) {
            showError(getTranslation("resetPassword.error.tooShort"));
            return;
        }
        if (!newPassword.getValue().equals(confirmPassword.getValue())) {
            showError(getTranslation("resetPassword.error.mismatch"));
            return;
        }

        try {
            authFacade.completePasswordReset(resetToken, newPassword.getValue());
            close();
            onSuccess.run();
        } catch (ApiException e) {
            showError(e.getApiError() != null ? e.getApiError().message() : e.getMessage());
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
}
