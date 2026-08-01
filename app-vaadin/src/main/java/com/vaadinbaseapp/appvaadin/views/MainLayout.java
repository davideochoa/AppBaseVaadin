package com.vaadinbaseapp.appvaadin.views;

import com.vaadinbaseapp.appvaadin.auth.AuthenticatedUser;
import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadinbaseapp.appvaadin.facade.AuthFacade;
import com.vaadinbaseapp.appvaadin.facade.UserFacade;
import com.vaadinbaseapp.appvaadin.i18n.SimpleI18NProvider;
import com.vaadinbaseapp.appvaadin.views.audit.AuditLogView;
import com.vaadinbaseapp.appvaadin.views.login.LoginView;
import com.vaadinbaseapp.appvaadin.views.users.UserListView;
import com.vaadinbaseapp.appvaadin.views.usertypes.UserTypeListView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinSession;

import java.util.Locale;

public class MainLayout extends AppLayout {

    private final AuthFacade authFacade;
    private final UserFacade userFacade;
    private final AuthenticatedUser authenticatedUser;

    public MainLayout(AuthFacade authFacade, UserFacade userFacade, AuthenticatedUser authenticatedUser) {
        this.authFacade = authFacade;
        this.userFacade = userFacade;
        this.authenticatedUser = authenticatedUser;
        createHeader();
        addDrawerContent(authenticatedUser);
    }

    private void createHeader() {
        Select<Locale> localeSelect = new Select<>();
        localeSelect.setItems(SimpleI18NProvider.ENGLISH, SimpleI18NProvider.SPANISH);
        localeSelect.setItemLabelGenerator(locale -> locale.getLanguage().toUpperCase());
        localeSelect.setValue(UI.getCurrent().getLocale());
        localeSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                VaadinSession.getCurrent().setAttribute(Locale.class, e.getValue());
                UI.getCurrent().setLocale(e.getValue());
                UI.getCurrent().getPage().reload();
            }
        });

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), localeSelect);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle().set("justify-content", "space-between");
        addToNavbar(header);
    }

    private void addDrawerContent(AuthenticatedUser authenticatedUser) {
        Span appName = new Span("AppBaseVaadin");
        appName.getStyle().set("font-weight", "600").set("font-size", "var(--lumo-font-size-l)");
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation(authenticatedUser));

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation(AuthenticatedUser authenticatedUser) {
        SideNav nav = new SideNav();
        nav.addItem(
                new SideNavItem(getTranslation("nav.users"), UserListView.class, VaadinIcon.USER.create()),
                new SideNavItem(getTranslation("nav.userTypes"), UserTypeListView.class, VaadinIcon.TAGS.create()));
        if (authenticatedUser.hasRole("ADMINISTRATOR")) {
            nav.addItem(new SideNavItem(getTranslation("nav.audit"), AuditLogView.class,
                    VaadinIcon.CLIPBOARD_TEXT.create()));
        }
        nav.getStyle().set("margin", "var(--vaadin-gap-s)");
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        if (authenticatedUser.isAuthenticated()) {
            String email = authenticatedUser.getEmail().orElse("");
            String displayName = resolveDisplayName(email);

            Avatar avatar = new Avatar(displayName);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userItem = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(new Span(displayName));
            div.add(VaadinIcon.CHEVRON_DOWN_SMALL.create());
            div.getStyle().set("display", "flex").set("align-items", "center").set("gap", "var(--lumo-space-s)");
            userItem.add(div);
            userItem.getSubMenu().addItem(getTranslation("nav.logout"), e -> doLogout());

            layout.add(userMenu);
        } else {
            layout.add(new Anchor("login", getTranslation("login.title")));
        }

        return layout;
    }

    private String resolveDisplayName(String email) {
        try {
            var user = userFacade.getByEmail(email);
            return user.firstName() + " " + user.lastName();
        } catch (ApiException e) {
            return email;
        }
    }

    private void doLogout() {
        authFacade.logout();
        UI.getCurrent().getPage().setLocation("login");
    }
}
