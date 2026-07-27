package com.appbasevaadin.appvaadin.views.support;

import com.appbasevaadin.appvaadin.dto.ApiError;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.html.Span;

import java.util.Map;

/**
 * Maps an {@link ApiError} from a failed create/update call onto a dialog's fields: field-level
 * errors go on the matching input via {@link HasValidation}, anything else (a 409 with no
 * {@code field}, or a field name the dialog doesn't have a component for) falls back to a
 * form-level banner. Shared by {@link com.appbasevaadin.appvaadin.views.users.UserFormDialog} and
 * {@link com.appbasevaadin.appvaadin.views.usertypes.UserTypeFormDialog}, which previously
 * duplicated this mapping logic field-for-field.
 */
public class FormErrorPresenter {

    private final Span formError;
    private final Map<String, HasValidation> fieldsByName;

    public FormErrorPresenter(Span formError, Map<String, HasValidation> fieldsByName) {
        this.formError = formError;
        this.fieldsByName = fieldsByName;
        formError.getElement().getThemeList().add("badge error");
        formError.setVisible(false);
    }

    public void applyErrors(ApiError apiError) {
        if (apiError == null) {
            return;
        }
        if (apiError.errors() == null || apiError.errors().isEmpty()) {
            showFormError(apiError.message());
            return;
        }
        for (ApiError.FieldError fieldError : apiError.errors()) {
            HasValidation field = fieldsByName.get(fieldError.field());
            if (field != null) {
                field.setInvalid(true);
                field.setErrorMessage(fieldError.message());
            } else {
                showFormError(fieldError.field() + ": " + fieldError.message());
            }
        }
    }

    public void clear() {
        fieldsByName.values().forEach(field -> field.setInvalid(false));
        formError.setVisible(false);
    }

    private void showFormError(String message) {
        formError.setText(message);
        formError.setVisible(true);
    }
}
