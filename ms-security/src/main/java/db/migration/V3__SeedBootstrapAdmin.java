package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;

/**
 * Seeds the single bootstrap admin account (username "admin", password
 * from BOOTSTRAP_ADMIN_PASSWORD, or "admin" if that env var is unset/blank)
 * so there is a way to log in before any registration flow exists.
 * must_reset_password=TRUE forces the login-time password-reset flow (see
 * AuthService#login/#completePasswordReset) on its very first use — nothing
 * keeps the bootstrap password valid beyond that first login.
 * user_id is left NULL: this bootstrap identity may have no matching
 * profile in ms-users yet (Lesson 5 forbids ms-security from depending on
 * ms-users being reachable during startup).
 *
 * <p>Flyway instantiates Java migrations directly (no Spring DI available
 * here), so the override is read straight from the process environment
 * rather than a {@code @Value}-injected property.
 */
public class V3__SeedBootstrapAdmin extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String bootstrapPassword = System.getenv("BOOTSTRAP_ADMIN_PASSWORD");
        if (bootstrapPassword == null || bootstrapPassword.isBlank()) {
            bootstrapPassword = "admin";
        }
        String passwordHash = new BCryptPasswordEncoder().encode(bootstrapPassword);

        try (PreparedStatement statement = context.getConnection().prepareStatement("""
                INSERT INTO user_security
                    (user_id, username, email, password_hash, role, auth_provider, active, must_reset_password)
                VALUES (NULL, 'admin', 'admin@local', ?, 'ADMINISTRATOR', 'LOCAL', TRUE, TRUE)
                """)) {
            statement.setString(1, passwordHash);
            statement.execute();
        }
    }
}
