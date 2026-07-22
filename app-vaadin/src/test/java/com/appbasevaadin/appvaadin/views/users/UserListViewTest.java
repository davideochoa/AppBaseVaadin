package com.appbasevaadin.appvaadin.views.users;

import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import com.appbasevaadin.appvaadin.facade.UserFacade;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class UserListViewTest {

    private UserFacade userFacade;

    @BeforeEach
    void setUp() {
        MockVaadin.setup();
        userFacade = Mockito.mock(UserFacade.class);
        when(userFacade.search(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 0, 20));
        when(userFacade.listUserTypes()).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void administratorSeesTheNewUserButton() {
        AuthenticatedUser adminUser = Mockito.mock(AuthenticatedUser.class);
        when(adminUser.hasRole("ADMINISTRATOR")).thenReturn(true);

        UserListView view = new UserListView(userFacade, adminUser);
        UI.getCurrent().add(view);

        assertThat(_find(view, Button.class)).isNotEmpty();
    }

    @Test
    void nonAdministratorDoesNotSeeTheNewUserButton() {
        AuthenticatedUser regularUser = Mockito.mock(AuthenticatedUser.class);
        when(regularUser.hasRole("ADMINISTRATOR")).thenReturn(false);

        UserListView view = new UserListView(userFacade, regularUser);
        UI.getCurrent().add(view);

        assertThat(_find(view, Button.class)).isEmpty();
    }
}
