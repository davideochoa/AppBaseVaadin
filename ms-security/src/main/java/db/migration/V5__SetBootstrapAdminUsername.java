package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;

/**
 * V4 backfilled `username` for every existing row from the email local-part
 * (safe generic default). This migration overrides that backfill for the
 * bootstrap admin specifically, honoring the same env-var-configurable
 * pattern V3 already uses for the email/password, so a deployment that set
 * BOOTSTRAP_ADMIN_EMAIL to something other than "admin@local" still ends up
 * with the intended login username rather than whatever the email happens
 * to derive to.
 */
public class V5__SetBootstrapAdminUsername extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String email = System.getenv().getOrDefault("BOOTSTRAP_ADMIN_EMAIL", "admin@local");
        String username = System.getenv().getOrDefault("BOOTSTRAP_ADMIN_USERNAME", "admin");

        try (PreparedStatement statement = context.getConnection().prepareStatement("""
                UPDATE user_security SET username = ? WHERE email = ?
                """)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.execute();
        }
    }
}
