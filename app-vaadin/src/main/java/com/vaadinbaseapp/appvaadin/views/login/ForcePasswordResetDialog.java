package com.vaadinbaseapp.appvaadin.views.login;

import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadinbaseapp.appvaadin.facade.SecurityUserFacade;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;

/**
 * Shown right after a successful login when the account is flagged
 * mustResetPassword (a brand-new user, or one an admin just reset) — the
 * account's current password is predictable (its own username, encrypted),
 * so the user must replace it before doing anything else. Not closable via
 * ESC/outside-click: this isn't optional.
 */
public class ForcePasswordResetDialog extends Dialog {

    private final SecurityUserFacade securityUserFacade;
    private final Runnable onSuccess;

    private final PasswordField newPassword = new PasswordField();
    private final PasswordField confirmPassword = new PasswordField();
    private final Span errorMessage = new Span();

    public ForcePasswordResetDialog(SecurityUserFacade securityUserFacade, Runnable onSuccess) {
        this.securityUserFacade = securityUserFacade;
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

        if (newPassword.getValue() == null || newPassword.getValue().length() < 6) {
            showError(getTranslation("resetPassword.error.tooShort"));
            return;
        }
        if (!newPassword.getValue().equals(confirmPassword.getValue())) {
            showError(getTranslation("resetPassword.error.mismatch"));
            return;
        }

        try {
            securityUserFacade.changeOwnPassword(newPassword.getValue());
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
