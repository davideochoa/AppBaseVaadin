package com.appbasevaadin.appvaadin.views.audit;

import com.appbasevaadin.appvaadin.dto.AuditEventResponse;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import com.appbasevaadin.appvaadin.facade.AuditFacade;
import com.appbasevaadin.appvaadin.views.MainLayout;
import com.appbasevaadin.appvaadin.views.support.SearchFieldSupport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.stream.Stream;

@Route(value = "audit", layout = MainLayout.class)
@PageTitle("Audit log")
public class AuditLogView extends VerticalLayout {

    private static final int PAGE_SIZE = 20;

    private final AuditFacade auditFacade;

    private String typeFilter = "";
    private String emailFilter = "";
    private final Grid<AuditEventResponse> grid = new Grid<>(AuditEventResponse.class, false);

    public AuditLogView(AuditFacade auditFacade) {
        this.auditFacade = auditFacade;

        setSizeFull();
        add(buildToolbar());
        add(buildGrid());
        setFlexGrow(1, grid);
    }

    private HorizontalLayout buildToolbar() {
        var typeField = SearchFieldSupport.buildFilterField(getTranslation("audit.type"), value -> {
            typeFilter = value;
            grid.getDataProvider().refreshAll();
        });
        var emailField = SearchFieldSupport.buildFilterField(getTranslation("audit.email"), value -> {
            emailFilter = value;
            grid.getDataProvider().refreshAll();
        });

        return new HorizontalLayout(typeField, emailField);
    }

    private Grid<AuditEventResponse> buildGrid() {
        grid.addColumn(AuditEventResponse::type).setHeader(getTranslation("audit.type"));
        grid.addColumn(AuditEventResponse::email).setHeader(getTranslation("audit.email"));
        grid.addColumn(AuditEventResponse::ipAddress).setHeader(getTranslation("audit.ip"));
        grid.addColumn(AuditEventResponse::occurredAt).setHeader(getTranslation("audit.occurredAt"));
        grid.setSizeFull();
        grid.setItems(fetchQuery -> fetchPage(fetchQuery.getOffset(), fetchQuery.getLimit()),
                countQuery -> countTotal());
        return grid;
    }

    private Stream<AuditEventResponse> fetchPage(int offset, int limit) {
        int page = offset / limit;
        return currentPage(page, limit).content().stream();
    }

    private int countTotal() {
        return (int) currentPage(0, PAGE_SIZE).totalElements();
    }

    private PageResponse<AuditEventResponse> currentPage(int page, int size) {
        return auditFacade.search(typeFilter, emailFilter, page, size);
    }
}
