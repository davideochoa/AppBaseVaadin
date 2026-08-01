package com.vaadinbaseapp.appvaadin.testutil;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.mock.MockVaadinServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import jakarta.servlet.ServletException;

import java.lang.reflect.Proxy;

/**
 * Same as {@link MockVaadin#setup()}, but the mocked servlet reports
 * productionMode=true. VaadinServlet.verifyLicense(boolean) only logs a
 * warning on a failed license check in production mode; in development mode
 * (MockVaadin's default) the same failure throws and aborts servlet init,
 * which is what happens in CI sandboxes with no browser to complete Vaadin's
 * online license validation. Tests here don't exercise production-only
 * behavior, so forcing this flag is safe.
 */
public final class KaribuTestSetup {

    private KaribuTestSetup() {
    }

    public static void setupProductionMode() {
        MockVaadin.setup(UI::new, new ProductionModeServlet());
    }

    private static final class ProductionModeServlet extends MockVaadinServlet {
        @Override
        protected DeploymentConfiguration createDeploymentConfiguration() throws ServletException {
            DeploymentConfiguration delegate = super.createDeploymentConfiguration();
            return (DeploymentConfiguration) Proxy.newProxyInstance(
                    DeploymentConfiguration.class.getClassLoader(),
                    new Class<?>[] {DeploymentConfiguration.class},
                    (proxy, method, args) -> "isProductionMode".equals(method.getName())
                            ? Boolean.TRUE
                            : method.invoke(delegate, args));
        }
    }
}
