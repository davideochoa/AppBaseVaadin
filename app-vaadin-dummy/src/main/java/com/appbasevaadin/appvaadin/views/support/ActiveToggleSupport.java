package com.appbasevaadin.appvaadin.views.support;

import com.appbasevaadin.appvaadin.client.ApiException;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;

import java.util.function.Consumer;

/**
 * Shared shape for a Grid's inline "active" checkbox column: build a checkbox seeded with the
 * current value, and on change persist it — showing a notification and refreshing the grid either
 * way. {@link com.appbasevaadin.appvaadin.views.users.UserListView} and
 * {@link com.appbasevaadin.appvaadin.views.usertypes.UserTypeListView} both need exactly this;
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
            Notification.show(e.getApiError() != null ? e.getApiError().message() : e.getMessage());
        } finally {
            refresh.run();
        }
    }
}
