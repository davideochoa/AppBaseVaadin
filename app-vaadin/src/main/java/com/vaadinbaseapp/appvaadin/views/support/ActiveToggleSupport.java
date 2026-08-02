package com.vaadinbaseapp.appvaadin.views.support;

import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;

import java.util.function.Consumer;

/**
 * Shared shape for a Grid's inline "active" checkbox column: build a checkbox seeded with the
 * current value, and on change persist it — showing a notification and refreshing the grid either
 * way. {@link com.vaadinbaseapp.appvaadin.views.users.UserListView} and
 * {@link com.vaadinbaseapp.appvaadin.views.usertypes.UserTypeListView} both need exactly this;
 * only *how to persist* the new value differs between them, which is why that part stays a
 * caller-supplied {@link Runnable}.
 */
public final class ActiveToggleSupport {

    private ActiveToggleSupport() {
    }

    public static Checkbox buildToggle(boolean currentValue, Consumer<Boolean> onChange) {
        Checkbox checkbox = new Checkbox(currentValue);
        checkbox.addValueChangeListener(e -> onChange.accept(e.getValue()));
        return checkbox;
    }

    public static void persistAndRefresh(Runnable persist, Runnable refresh) {
        try {
            persist.run();
        } catch (ApiException e) {
            Notification.show(errorMessage(e));
        } finally {
            refresh.run();
        }
    }

    /**
     * {@code ApiException} is always built from a structured {@code ApiError}
     * (see {@code ApiClientSupport.handleError}), so getApiError() is never
     * actually null today — this is a defensive fallback, not a live path, but
     * it must stay a safe generic message rather than the raw local exception
     * text (e.getMessage()), which could otherwise leak HTTP client internals.
     */
    private static String errorMessage(ApiException e) {
        return e.getApiError() != null
                ? e.getApiError().message()
                : UI.getCurrent().getTranslation("common.unexpectedError");
    }
}
