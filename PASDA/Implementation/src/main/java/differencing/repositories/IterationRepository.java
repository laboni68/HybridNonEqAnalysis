package differencing.repositories;

import differencing.models.Iteration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IterationRepository extends Repository {
    private static final String INSERT_OR_UPDATE = "" +
        "INSERT INTO iteration(" +
        "id, " +
        "run_id, " +
        "iteration, " +
        "result, " +
        "has_timed_out, " +
        "is_depth_limited, " +
        "has_uif, " +
        "partition_count, " +
        "runtime, " +
        "errors" +
        ") " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON CONFLICT DO UPDATE SET " +
        "run_id = excluded.run_id, " +
        "iteration = excluded.iteration, " +
        "result = excluded.result, " +
        "has_timed_out = excluded.has_timed_out, " +
        "is_depth_limited = excluded.is_depth_limited, " +
        "has_uif = excluded.has_uif, " +
        "partition_count = excluded.partition_count, " +
        "runtime = excluded.runtime, " +
        "errors = excluded.errors";

    public static void insertOrUpdate(Iterable<Iteration> iterations) {
        for (Iteration iteration : iterations) {
            insertOrUpdate(iteration);
        }
    }

    public static void insertOrUpdate(Iteration iteration) {
        try (
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                INSERT_OR_UPDATE,
                PreparedStatement.RETURN_GENERATED_KEYS
            )
        ) {
            ps.setObject(1, iteration.id);
            ps.setObject(2, iteration.runId);
            ps.setObject(3, iteration.iteration);
            ps.setObject(4, iteration.result == null ? null : iteration.result.toString());
            ps.setObject(5, iteration.hasTimedOut);
            ps.setObject(6, iteration.isDepthLimited);
            ps.setObject(7, iteration.hasUif);
            ps.setObject(8, iteration.partitionCount);
            ps.setObject(9, iteration.runtime);
            ps.setObject(10, iteration.errors);
            ps.execute();

            if (iteration.id == null) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    iteration.id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
