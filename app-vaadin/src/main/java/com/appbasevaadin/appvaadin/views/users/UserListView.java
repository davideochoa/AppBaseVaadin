package com.appbasevaadin.appvaadin.views.users;

import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.appbasevaadin.appvaadin.client.ApiException;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import com.appbasevaadin.appvaadin.dto.UserResponse;
import com.appbasevaadin.appvaadin.facade.UserFacade;
import com.appbasevaadin.appvaadin.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.stream.Stream;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Users")
public class UserListView extends VerticalLayout {

    private static final int PAGE_SIZE = 20;

    private final UserFacade userFacade;
    private final boolean isAdmin;

    private String searchText = "";
    private final Grid<UserResponse> grid = new Grid<>(UserResponse.class, false);

    public UserListView(UserFacade userFacade, AuthenticatedUser authenticatedUser) {
        this.userFacade = userFacade;
        this.isAdmin = authenticatedUser.hasRole("ADMINISTRATOR");

        setSizeFull();
        add(buildToolbar());
        add(buildGrid());
        setFlexGrow(1, grid);
    }

    private HorizontalLayout buildToolbar() {
        TextField search = new TextField();
        search.setPlaceholder(getTranslation("users.search"));
        search.setClearButtonVisible(true);
        search.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> {
            searchText = e.getValue();
            grid.getDataProvider().refreshAll();
        });

        HorizontalLayout toolbar = new HorizontalLayout(search);

        if (isAdmin) {
            Button newUserButton = new Button(getTranslation("users.new"), e -> openForm(null));
            toolbar.add(newUserButton);
        }

        return toolbar;
    }

    private Grid<UserResponse> buildGrid() {
        grid.addColumn(UserResponse::firstName).setHeader(getTranslation("users.firstName"));
        grid.addColumn(UserResponse::lastName).setHeader(getTranslation("users.lastName"));
        grid.addColumn(UserResponse::email).setHeader(getTranslation("users.email"));
        grid.addColumn(user -> user.userType() != null ? user.userType().name() : "")
                .setHeader(getTranslation("users.type"));
        grid.addColumn(user -> user.active() ? getTranslation("common.yes") : getTranslation("common.no"))
                .setHeader(getTranslation("users.active"));

        if (isAdmin) {
            grid.addComponentColumn(this::buildActionsColumn).setHeader("");
        }

        grid.setSizeFull();
        grid.setItems(fetchQuery -> fetchPage(fetchQuery.getOffset(), fetchQuery.getLimit()),
                countQuery -> countTotal());

        return grid;
    }

    private HorizontalLayout buildActionsColumn(UserResponse user) {
        Button edit = new Button(getTranslation("users.edit"), e -> openForm(user));
        Button delete = new Button(getTranslation("users.delete"), e -> confirmAndDelete(user));
        return new HorizontalLayout(edit, delete);
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
        UserFormDialog dialog = new UserFormDialog(userFacade, userFacade.listUserTypes(), existingUser,
                () -> grid.getDataProvider().refreshAll());
        dialog.open();
    }

    private void confirmAndDelete(UserResponse user) {
        try {
            userFacade.delete(user.id());
            grid.getDataProvider().refreshAll();
        } catch (ApiException e) {
            Notification.show(e.getApiError() != null ? e.getApiError().message() : e.getMessage());
        }
    }
}
