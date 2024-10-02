package differencing.repositories;

import differencing.models.Run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RunRepository extends Repository {
    private static final String INSERT_OR_UPDATE = "" +
        "INSERT INTO run(" +
        "id, " +
        "benchmark, " +
        "result, " +
        "has_timed_out, " +
        "is_depth_limited, " +
        "has_uif, " +
        "iteration_count, " +
        "result_iteration, " +
        "runtime, " +
        "errors" +
        ") " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON CONFLICT DO UPDATE SET " +
        "benchmark = excluded.benchmark, " +
        "result = excluded.result, " +
        "has_timed_out = excluded.has_timed_out, " +
        "is_depth_limited = excluded.is_depth_limited, " +
        "has_uif = excluded.has_uif, " +
        "iteration_count = excluded.iteration_count, " +
        "result_iteration = excluded.result_iteration, " +
        "runtime = excluded.runtime, " +
        "errors = excluded.errors";

    public static void insertOrUpdate(Run run) {
        try (
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                INSERT_OR_UPDATE,
                PreparedStatement.RETURN_GENERATED_KEYS
            )
        ) {
            ps.setObject(1, run.id);
            ps.setObject(2, run.benchmark);
            ps.setObject(3, run.result == null ? null : run.result.toString());
            ps.setObject(4, run.hasTimedOut);
            ps.setObject(5, run.isDepthLimited);
            ps.setObject(6, run.hasUif);
            ps.setObject(7, run.iterationCount);
            ps.setObject(8, run.resultIteration);
            ps.setObject(9, run.runtime);
            ps.setObject(10, run.errors);
            ps.execute();

            if (run.id == null) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    run.id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String DELETE = "" +
        "DELETE FROM run " +
        "WHERE benchmark = ? " +
        "AND tool = ?";


    // @TODO: Think about how to handle deletes.
    //   Probably, there shouldn't be any more automatic deletes at all.
    //   After all, we want to store multiple runs, however many that may be.
    //   -
    //   Manual deletes should, of course, still be somewhat convenient, i.e.,
    //   work by just deleting the run and then cascade-deleting everything else.
    //   -
    //   @TODO: Check how cascade deletes are handled by IntelliJ.
    //     => I think SQLite needed some flag for deletes to cascade, so how to pass this via IntelliJ?

//    public static void delete(Run run) {
//        delete(run.benchmark, run.tool);
//    }
//
//    public static void delete(String benchmark, String tool) {
//        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(DELETE)) {
//            ps.setObject(1, benchmark);
//            ps.setObject(2, tool);
//            ps.execute();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
