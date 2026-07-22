package com.appbasevaadin.appvaadin.views.users;

import com.appbasevaadin.appvaadin.client.ApiException;
import com.appbasevaadin.appvaadin.dto.ApiError;
import com.appbasevaadin.appvaadin.dto.UserRequest;
import com.appbasevaadin.appvaadin.dto.UserResponse;
import com.appbasevaadin.appvaadin.dto.UserTypeResponse;
import com.appbasevaadin.appvaadin.facade.UserFacade;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.Map;

public class UserFormDialog extends Dialog {

    private final UserFacade userFacade;
    private final Long editingUserId;
    private final Runnable onSaved;

    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final EmailField email = new EmailField();
    private final ComboBox<UserTypeResponse> userType = new ComboBox<>();
    private final Checkbox active = new Checkbox();
    private final Span formError = new Span();

    public UserFormDialog(UserFacade userFacade, List<UserTypeResponse> userTypes, UserResponse existingUser,
                           Runnable onSaved) {
        this.userFacade = userFacade;
        this.editingUserId = existingUser != null ? existingUser.id() : null;
        this.onSaved = onSaved;

        setHeaderTitle(existingUser != null ? getTranslation("users.edit") : getTranslation("users.new"));

        firstName.setLabel(getTranslation("users.firstName"));
        lastName.setLabel(getTranslation("users.lastName"));
        email.setLabel(getTranslation("users.email"));
        userType.setLabel(getTranslation("users.type"));
        userType.setItems(userTypes);
        userType.setItemLabelGenerator(UserTypeResponse::name);
        active.setLabel(getTranslation("users.active"));
        active.setValue(true);

        if (existingUser != null) {
            firstName.setValue(existingUser.firstName());
            lastName.setValue(existingUser.lastName());
            email.setValue(existingUser.email());
            userTypes.stream().filter(t -> t.id().equals(existingUser.userType().id())).findFirst()
                    .ifPresent(userType::setValue);
            active.setValue(existingUser.active());
        }

        formError.getElement().getThemeList().add("badge error");
        formError.setVisible(false);

        VerticalLayout layout = new VerticalLayout(firstName, lastName, email, userType, active, formError);
        add(layout);

        Button save = new Button(getTranslation("common.save"), e -> save());
        save.getElement().getThemeList().add("primary");
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
    }

    private void save() {
        clearErrors();
        UserRequest request = new UserRequest(firstName.getValue(), lastName.getValue(), email.getValue(),
                userType.getValue() != null ? userType.getValue().id() : null, active.getValue());
        try {
            if (editingUserId != null) {
                userFacade.update(editingUserId, request);
            } else {
                userFacade.create(request);
            }
            onSaved.run();
            close();
        } catch (ApiException e) {
            applyErrors(e.getApiError());
        }
    }

    private void applyErrors(ApiError apiError) {
        if (apiError == null) {
            return;
        }
        if (apiError.errors() == null || apiError.errors().isEmpty()) {
            formError.setText(apiError.message());
            formError.setVisible(true);
            return;
        }
        Map<String, HasValidation> fieldsByName = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email);
        for (ApiError.FieldError fieldError : apiError.errors()) {
            HasValidation field = fieldsByName.get(fieldError.field());
            if (field != null) {
                field.setInvalid(true);
                field.setErrorMessage(fieldError.message());
            } else {
                formError.setText(fieldError.field() + ": " + fieldError.message());
                formError.setVisible(true);
            }
        }
    }

    private void clearErrors() {
        for (HasValidation field : List.of(firstName, lastName, email)) {
            field.setInvalid(false);
        }
        formError.setVisible(false);
    }
}
