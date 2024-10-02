package differencing.repositories;

import org.sqlite.SQLiteConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public abstract class Repository {
    public static final Path DB_PATH = Paths.get("analysis/results/sqlite.db");

    protected static Connection connect() {
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            Properties properties = config.toProperties();

            return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH, properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
