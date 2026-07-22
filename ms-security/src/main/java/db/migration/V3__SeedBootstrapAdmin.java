package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;

/**
 * Seeds one LOCAL admin account so there is a way to log in before any
 * registration flow exists. {@code user_id} is left NULL: this bootstrap
 * identity may have no matching profile in ms-users yet (Lesson 5 forbids
 * ms-security from depending on ms-users being reachable during startup).
 */
public class V3__SeedBootstrapAdmin extends BaseJavaMigration {

    private static final Logger log = LoggerFactory.getLogger(V3__SeedBootstrapAdmin.class);
    private static final String DEFAULT_LOCAL_DEV_PASSWORD = "admin123";

    @Override
    public void migrate(Context context) throws Exception {
        String email = System.getenv().getOrDefault("BOOTSTRAP_ADMIN_EMAIL", "admin@local");
        String rawPassword = System.getenv("BOOTSTRAP_ADMIN_PASSWORD");

        if (rawPassword == null || rawPassword.isBlank()) {
            log.warn("BOOTSTRAP_ADMIN_PASSWORD is not set — using an insecure default bootstrap "
                    + "password for local development only. Never rely on this in a shared environment.");
            rawPassword = DEFAULT_LOCAL_DEV_PASSWORD;
        }

        String passwordHash = new BCryptPasswordEncoder().encode(rawPassword);

        try (PreparedStatement statement = context.getConnection().prepareStatement("""
                INSERT INTO user_security (user_id, email, password_hash, role, auth_provider, active)
                VALUES (NULL, ?, ?, 'ADMINISTRATOR', 'LOCAL', TRUE)
                """)) {
            statement.setString(1, email);
            statement.setString(2, passwordHash);
            statement.execute();
        }
    }
}
