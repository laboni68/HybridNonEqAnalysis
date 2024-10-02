package differencing.repositories;

import differencing.models.Settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SettingsRepository extends Repository {
    private static final String INSERT_OR_UPDATE = "" +
        "INSERT INTO settings(" +
        "run_id, " +
        "tool, " +
        "run_timeout, " +
        "iteration_timeout, " +
        "solver_timeout, " +
        "depth_limit " +
        ") " +
        "VALUES (?, ?, ?, ?, ?, ?) " +
        "ON CONFLICT DO UPDATE SET " +
        "tool = excluded.tool, " +
        "run_timeout = excluded.run_timeout, " +
        "iteration_timeout = excluded.iteration_timeout, " +
        "solver_timeout = excluded.solver_timeout, " +
        "depth_limit = excluded.depth_limit";

    public static void insertOrUpdate(Iterable<Settings> settings) {
        for (Settings setting : settings) {
            insertOrUpdate(setting);
        }
    }

    public static void insertOrUpdate(Settings settings) {
        try (
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(INSERT_OR_UPDATE)
        ) {
            ps.setObject(1, settings.runId);
            ps.setObject(2, settings.tool);
            ps.setObject(3, settings.runTimeout);
            ps.setObject(4, settings.iterationTimeout);
            ps.setObject(5, settings.solverTimeout);
            ps.setObject(6, settings.depthLimit);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
