package com.appbasevaadin.appvaadin.views.users;

import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import com.appbasevaadin.appvaadin.dto.UserRequest;
import com.appbasevaadin.appvaadin.dto.UserResponse;
import com.appbasevaadin.appvaadin.facade.SecurityUserFacade;
import com.appbasevaadin.appvaadin.facade.UserFacade;
import com.appbasevaadin.appvaadin.views.MainLayout;
import com.appbasevaadin.appvaadin.views.support.ActiveToggleSupport;
import com.appbasevaadin.appvaadin.views.support.SearchFieldSupport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.stream.Stream;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Users")
public class UserListView extends VerticalLayout {

    private static final int PAGE_SIZE = 20;

    private final UserFacade userFacade;
    private final SecurityUserFacade securityUserFacade;
    private final boolean isAdmin;

    private String searchText = "";
    private final Grid<UserResponse> grid = new Grid<>(UserResponse.class, false);

    public UserListView(UserFacade userFacade, SecurityUserFacade securityUserFacade,
                         AuthenticatedUser authenticatedUser) {
        this.userFacade = userFacade;
        this.securityUserFacade = securityUserFacade;
        this.isAdmin = authenticatedUser.hasRole("ADMINISTRATOR");

        setSizeFull();
        add(buildToolbar());
        add(buildGrid());
        setFlexGrow(1, grid);
    }

    private HorizontalLayout buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout(
                SearchFieldSupport.buildFilterField(getTranslation("users.search"), value -> {
                    searchText = value;
                    grid.getDataProvider().refreshAll();
                }));

        if (isAdmin) {
            Button newUserButton = new Button(getTranslation("users.new"), e -> openForm(null));
            toolbar.add(newUserButton);
        }

        return toolbar;
    }

    private Grid<UserResponse> buildGrid() {
        grid.addColumn(UserResponse::username).setHeader(getTranslation("users.username"));
        grid.addColumn(UserResponse::firstName).setHeader(getTranslation("users.firstName"));
        grid.addColumn(UserResponse::lastName).setHeader(getTranslation("users.lastName"));
        grid.addColumn(UserResponse::email).setHeader(getTranslation("users.email"));
        grid.addColumn(user -> user.userType() != null ? user.userType().name() : "")
                .setHeader(getTranslation("users.type"));

        if (isAdmin) {
            grid.addComponentColumn(this::buildActiveToggle).setHeader(getTranslation("users.active"));
            grid.addComponentColumn(this::buildActionsColumn).setHeader("");
        } else {
            grid.addColumn(user -> user.active() ? getTranslation("common.yes") : getTranslation("common.no"))
                    .setHeader(getTranslation("users.active"));
        }

        grid.setSizeFull();
        grid.setItems(fetchQuery -> fetchPage(fetchQuery.getOffset(), fetchQuery.getLimit()),
                countQuery -> countTotal());

        return grid;
    }

    private Checkbox buildActiveToggle(UserResponse user) {
        return ActiveToggleSupport.buildToggle(user.active(), active -> {
            UserRequest request = new UserRequest(user.username(), user.firstName(), user.lastName(), user.email(),
                    user.userType().id(), active);
            String role = user.userType().name().toUpperCase();
            ActiveToggleSupport.persistAndRefresh(
                    () -> {
                        userFacade.update(user.id(), request);
                        securityUserFacade.update(user.username(), user.username(), user.email(), role, active);
                    },
                    () -> grid.getDataProvider().refreshAll());
        });
    }

    private HorizontalLayout buildActionsColumn(UserResponse user) {
        Button edit = new Button(getTranslation("users.edit"), e -> openForm(user));
        return new HorizontalLayout(edit);
    }

    private Stream<UserResponse> fetchPage(int offset, int limit) {
        int page = offset / limit;
        return currentPage(page, limit).content().stream();
    }

    private int countTotal() {
        return (int) currentPage(0, PAGE_SIZE).totalElements();
    }

    private PageResponse<UserResponse> currentPage(int page, int size) {
        return userFacade.search(searchText, null, null, page, size);
    }

    private void openForm(UserResponse existingUser) {
        UserFormDialog dialog = new UserFormDialog(userFacade, securityUserFacade, userFacade.listUserTypes(),
                existingUser, () -> grid.getDataProvider().refreshAll());
        dialog.open();
    }
}
