package differencing.repositories;

import differencing.models.Benchmark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BenchmarkRepository extends Repository {
    private static final String INSERT_OR_UPDATE = "" +
        "INSERT INTO benchmark(" +
        "benchmark, " +
        "expected" +
        ") " +
        "VALUES (?, ?) " +
        "ON CONFLICT DO UPDATE SET " +
        "expected = excluded.expected";

    public static void insertOrUpdate(Benchmark benchmark) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(INSERT_OR_UPDATE)) {
            ps.setObject(1, benchmark.benchmark);
            ps.setObject(2, benchmark.expected);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String DELETE = "" +
        "DELETE FROM benchmark " +
        "WHERE benchmark = ?";

    public static void delete(Benchmark benchmark) {
        delete(benchmark.benchmark);
    }

    public static void delete(String benchmark) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setObject(1, benchmark);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
