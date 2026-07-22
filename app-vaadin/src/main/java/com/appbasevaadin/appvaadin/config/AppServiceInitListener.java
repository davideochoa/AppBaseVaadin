package com.appbasevaadin.appvaadin.config;

import com.appbasevaadin.appvaadin.auth.AuthBeforeEnterListener;
import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class AppServiceInitListener implements VaadinServiceInitListener {

    private final AuthenticatedUser authenticatedUser;

    public AppServiceInitListener(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInitEvent ->
                uiInitEvent.getUI().addBeforeEnterListener(new AuthBeforeEnterListener(authenticatedUser)));
    }
}
