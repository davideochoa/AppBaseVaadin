package com.appbasevaadin.appvaadin.views.usertypes;

import com.appbasevaadin.appvaadin.client.ApiException;
import com.appbasevaadin.appvaadin.dto.UserTypeRequest;
import com.appbasevaadin.appvaadin.dto.UserTypeResponse;
import com.appbasevaadin.appvaadin.facade.UserFacade;
import com.appbasevaadin.appvaadin.views.support.FormErrorPresenter;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Map;

public class UserTypeFormDialog extends Dialog {

    private final UserFacade userFacade;
    private final Long editingUserTypeId;
    private final Runnable onSaved;

    private final TextField name = new TextField();
    private final TextField description = new TextField();
    private final boolean active;
    private final FormErrorPresenter errorPresenter;

    public UserTypeFormDialog(UserFacade userFacade, UserTypeResponse existingUserType, Runnable onSaved) {
        this.userFacade = userFacade;
        this.editingUserTypeId = existingUserType != null ? existingUserType.id() : null;
        this.onSaved = onSaved;
        this.active = existingUserType == null || existingUserType.active();

        setWidth("25em");
        setHeaderTitle(existingUserType != null ? getTranslation("userTypes.edit") : getTranslation("userTypes.new"));

        name.setLabel(getTranslation("userTypes.name"));
        name.setWidthFull();
        description.setLabel(getTranslation("userTypes.description"));
        description.setWidthFull();

        if (existingUserType != null) {
            name.setValue(existingUserType.name());
            description.setValue(existingUserType.description() != null ? existingUserType.description() : "");
        }

        Span formError = new Span();
        Map<String, HasValidation> fieldsByName = Map.of("name", name);
        this.errorPresenter = new FormErrorPresenter(formError, fieldsByName);

        VerticalLayout layout = new VerticalLayout(name, description, formError);
        layout.setWidthFull();
        add(layout);

        Button save = new Button(getTranslation("common.save"), e -> save());
        save.getElement().getThemeList().add("primary");
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
    }

    private void save() {
        errorPresenter.clear();
        UserTypeRequest request = new UserTypeRequest(name.getValue(), description.getValue(), active);
        try {
            if (editingUserTypeId != null) {
                userFacade.updateUserType(editingUserTypeId, request);
            } else {
                userFacade.createUserType(request);
            }
            onSaved.run();
            close();
        } catch (ApiException e) {
            errorPresenter.applyErrors(e.getApiError());
        }
    }
}
