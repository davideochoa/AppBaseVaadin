package com.appbasevaadin.appvaadin.views;

import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.appbasevaadin.appvaadin.facade.AuthFacade;
import com.appbasevaadin.appvaadin.i18n.SimpleI18NProvider;
import com.appbasevaadin.appvaadin.views.audit.AuditLogView;
import com.appbasevaadin.appvaadin.views.login.LoginView;
import com.appbasevaadin.appvaadin.views.users.UserListView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.RouterLink;

import java.util.Locale;

public class MainLayout extends AppLayout {

    private final AuthFacade authFacade;

    public MainLayout(AuthFacade authFacade, AuthenticatedUser authenticatedUser) {
        this.authFacade = authFacade;
        createHeader(authenticatedUser);
    }

    private void createHeader(AuthenticatedUser authenticatedUser) {
        H1 title = new H1("AppBaseVaadin");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0 1em 0 0");

        HorizontalLayout nav = new HorizontalLayout(new RouterLink(getTranslation("nav.users"), UserListView.class));
        if (authenticatedUser.hasRole("ADMINISTRATOR")) {
            nav.add(new RouterLink(getTranslation("nav.audit"), AuditLogView.class));
        }
        nav.getStyle().set("flex-grow", "1");

        Select<Locale> localeSelect = new Select<>();
        localeSelect.setItems(SimpleI18NProvider.ENGLISH, SimpleI18NProvider.SPANISH);
        localeSelect.setItemLabelGenerator(locale -> locale.getLanguage().toUpperCase());
        localeSelect.setValue(UI.getCurrent().getLocale());
        localeSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                UI.getCurrent().setLocale(e.getValue());
                UI.getCurrent().getPage().reload();
            }
        });

        Button logout = new Button(getTranslation("nav.logout"), e -> doLogout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, nav, localeSelect, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);
        addToNavbar(header);
    }

    private void doLogout() {
        authFacade.logout();
        UI.getCurrent().getPage().setLocation("login");
    }
}
