package com.vaadinbaseapp.appvaadin.views.support;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.function.Consumer;

/**
 * Builds a debounced (LAZY) filter {@link TextField}: placeholder, clear button, and a callback
 * fired after the user pauses typing. {@link com.vaadinbaseapp.appvaadin.views.users.UserListView}
 * and {@link com.vaadinbaseapp.appvaadin.views.audit.AuditLogView} each wired this up by hand
 * before.
 */
public final class SearchFieldSupport {

    private SearchFieldSupport() {
    }

    public static TextField buildFilterField(String placeholder, Consumer<String> onValueChanged) {
        TextField field = new TextField();
        field.setPlaceholder(placeholder);
        field.setClearButtonVisible(true);
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.addValueChangeListener(e -> onValueChanged.accept(e.getValue()));
        return field;
    }
}
