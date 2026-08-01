package com.vaadinbaseapp.appvaadin.views.usertypes;

import com.vaadinbaseapp.appvaadin.auth.AuthenticatedUser;
import com.vaadinbaseapp.appvaadin.dto.UserTypeRequest;
import com.vaadinbaseapp.appvaadin.dto.UserTypeResponse;
import com.vaadinbaseapp.appvaadin.facade.UserFacade;
import com.vaadinbaseapp.appvaadin.views.MainLayout;
import com.vaadinbaseapp.appvaadin.views.support.ActiveToggleSupport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "user-types", layout = MainLayout.class)
@PageTitle("User types")
public class UserTypeListView extends VerticalLayout {

    private final UserFacade userFacade;
    private final boolean isAdmin;
    private final Grid<UserTypeResponse> grid = new Grid<>(UserTypeResponse.class, false);

    public UserTypeListView(UserFacade userFacade, AuthenticatedUser authenticatedUser) {
        this.userFacade = userFacade;
        this.isAdmin = authenticatedUser.hasRole("ADMINISTRATOR");

        setSizeFull();
        add(buildToolbar());
        add(buildGrid());
        setFlexGrow(1, grid);
        refresh();
    }

    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        if (isAdmin) {
            Button newUserType = new Button(getTranslation("userTypes.new"), e -> openForm(null));
            toolbar.add(newUserType);
        }
        return toolbar;
    }

    private Grid<UserTypeResponse> buildGrid() {
        grid.addColumn(UserTypeResponse::name).setHeader(getTranslation("userTypes.name"));
        grid.addColumn(UserTypeResponse::description).setHeader(getTranslation("userTypes.description"));

        if (isAdmin) {
            grid.addComponentColumn(this::buildActiveToggle).setHeader(getTranslation("userTypes.active"));
            grid.addComponentColumn(this::buildActionsColumn).setHeader("");
        } else {
            grid.addColumn(t -> t.active() ? getTranslation("common.yes") : getTranslation("common.no"))
                    .setHeader(getTranslation("userTypes.active"));
        }

        grid.setSizeFull();
        return grid;
    }

    private Checkbox buildActiveToggle(UserTypeResponse userType) {
        return ActiveToggleSupport.buildToggle(userType.active(), active -> {
            UserTypeRequest request = new UserTypeRequest(userType.name(), userType.description(), active);
            ActiveToggleSupport.persistAndRefresh(
                    () -> userFacade.updateUserType(userType.id(), request),
                    this::refresh);
        });
    }

    private HorizontalLayout buildActionsColumn(UserTypeResponse userType) {
        Button edit = new Button(getTranslation("userTypes.edit"), e -> openForm(userType));
        return new HorizontalLayout(edit);
    }

    private void openForm(UserTypeResponse existingUserType) {
        UserTypeFormDialog dialog = new UserTypeFormDialog(userFacade, existingUserType, this::refresh);
        dialog.open();
    }

    private void refresh() {
        grid.setItems(userFacade.listUserTypes());
    }
}
