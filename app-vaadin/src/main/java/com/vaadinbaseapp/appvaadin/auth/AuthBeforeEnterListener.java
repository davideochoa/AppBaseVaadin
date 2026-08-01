package com.vaadinbaseapp.appvaadin.auth;

import com.vaadinbaseapp.appvaadin.views.audit.AuditLogView;
import com.vaadinbaseapp.appvaadin.views.login.LoginView;
import com.vaadinbaseapp.appvaadin.views.users.UserListView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;

public class AuthBeforeEnterListener implements BeforeEnterListener {

    private static final String ADMINISTRATOR = "ADMINISTRATOR";

    private final AuthenticatedUser authenticatedUser;

    public AuthBeforeEnterListener(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean isLoginView = LoginView.class.equals(event.getNavigationTarget());
        boolean authenticated = authenticatedUser.isAuthenticated();

        if (isLoginView) {
            if (authenticated) {
                event.rerouteTo(UserListView.class);
            }
            return;
        }

        if (!authenticated) {
            event.rerouteTo(LoginView.class);
            return;
        }

        boolean isAuditView = AuditLogView.class.equals(event.getNavigationTarget());
        if (isAuditView && !authenticatedUser.hasRole(ADMINISTRATOR)) {
            event.rerouteTo(UserListView.class);
        }
    }
}
