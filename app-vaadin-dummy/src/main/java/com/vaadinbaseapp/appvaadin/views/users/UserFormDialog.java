package com.vaadinbaseapp.appvaadin.views.users;

import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadinbaseapp.appvaadin.dto.UserRequest;
import com.vaadinbaseapp.appvaadin.dto.UserResponse;
import com.vaadinbaseapp.appvaadin.dto.UserTypeResponse;
import com.vaadinbaseapp.appvaadin.facade.UserFacade;
import com.vaadinbaseapp.appvaadin.views.support.FormErrorPresenter;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class UserFormDialog extends Dialog {

    private final UserFacade userFacade;
    private final Long editingUserId;
    private final Runnable onSaved;

    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final EmailField email = new EmailField();
    private final ComboBox<UserTypeResponse> userType = new ComboBox<>();
    private final boolean active;
    private final FormErrorPresenter errorPresenter;

    public UserFormDialog(UserFacade userFacade, List<UserTypeResponse> userTypes, UserResponse existingUser,
                           Runnable onSaved) {
        this.userFacade = userFacade;
        this.editingUserId = existingUser != null ? existingUser.id() : null;
        this.onSaved = onSaved;
        this.active = existingUser == null || existingUser.active();

        setWidth("25em");
        setHeaderTitle(existingUser != null ? getTranslation("users.edit") : getTranslation("users.new"));

        firstName.setLabel(getTranslation("users.firstName"));
        firstName.setWidthFull();
        lastName.setLabel(getTranslation("users.lastName"));
        lastName.setWidthFull();
        email.setLabel(getTranslation("users.email"));
        email.setWidthFull();
        userType.setLabel(getTranslation("users.type"));
        userType.setWidthFull();
        userType.setItems(selectableUserTypes(userTypes, existingUser));
        userType.setItemLabelGenerator(UserTypeResponse::name);

        if (existingUser != null) {
            firstName.setValue(existingUser.firstName());
            lastName.setValue(existingUser.lastName());
            email.setValue(existingUser.email());
            userTypes.stream().filter(t -> t.id().equals(existingUser.userType().id())).findFirst()
                    .ifPresent(userType::setValue);
        }

        Span formError = new Span();
        Map<String, HasValidation> fieldsByName = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email);
        this.errorPresenter = new FormErrorPresenter(formError, fieldsByName);

        VerticalLayout layout = new VerticalLayout(firstName, lastName, email, userType, formError);
        layout.setWidthFull();
        add(layout);

        Button save = new Button(getTranslation("common.save"), e -> save());
        save.getElement().getThemeList().add("primary");
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
    }

    private void save() {
        errorPresenter.clear();
        UserRequest request = new UserRequest(firstName.getValue(), lastName.getValue(), email.getValue(),
                userType.getValue() != null ? userType.getValue().id() : null, active);
        try {
            if (editingUserId != null) {
                userFacade.update(editingUserId, request);
            } else {
                userFacade.create(request);
            }
            onSaved.run();
            close();
        } catch (ApiException e) {
            errorPresenter.applyErrors(e.getApiError());
        }
    }

    private List<UserTypeResponse> selectableUserTypes(List<UserTypeResponse> userTypes, UserResponse existingUser) {
        Long currentTypeId = existingUser != null ? existingUser.userType().id() : null;
        return Stream.concat(
                        userTypes.stream().filter(UserTypeResponse::active),
                        userTypes.stream().filter(t -> t.id().equals(currentTypeId)))
                .distinct()
                .toList();
    }
}
