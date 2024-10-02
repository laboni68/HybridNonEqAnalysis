package differencing.repositories;

import differencing.models.Partition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PartitionRepository extends Repository {
    private static final String SELECT_ID = "" +
        "SELECT id FROM partition " +
        "WHERE iteration_id = ? " +
        "AND partition = ?";

    public static Integer getId(Partition partition) {
        try (
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(SELECT_ID)
        ) {
            ps.setObject(1, partition.iterationId);
            ps.setObject(2, partition.partition);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertOrUpdate(Iterable<Partition> partitions) {
        for (Partition partition: partitions) {
            insertOrUpdate(partition);
        }
    }

    public static void insertOrUpdate(Partition partition) {
        if (partition.result != null) {
            insertOrUpdateFull(partition);
        } else {
            insertOrUpdatePartial(partition);
        }
    }

    private static final String INSERT_OR_UPDATE_FULL = "" +
        "INSERT INTO partition(" +
        "id, " +
        "iteration_id, " +
        "partition, " +
        "result, " +
        "pc_status, " +
        "pc_model, " +
        "pc_reason_unknown, " +
        "pc_statistics, " +
        "neq_status, " +
        "neq_model, " +
        "neq_result_v1, " +
        "neq_result_v2, " +
        "neq_reason_unknown, " +
        "neq_statistics, " +
        "eq_status, " +
        "eq_model, " +
        "eq_result_v1, " +
        "eq_result_v2, " +
        "eq_reason_unknown, " +
        "eq_statistics, " +
        "has_uif, " +
        "has_uif_pc, " +
        "has_uif_v1, " +
        "has_uif_v2, " +
        "constraint_count, " +
        "runtime, " +
        "errors" +
        ") " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON CONFLICT DO UPDATE SET " +
        "result = excluded.result, " +
        "pc_status = excluded.pc_status, " +
        "pc_model = excluded.pc_model, " +
        "pc_reason_unknown = excluded.pc_reason_unknown, " +
        "pc_statistics = excluded.pc_statistics, " +
        "neq_status = excluded.neq_status, " +
        "neq_model = excluded.neq_model, " +
        "neq_result_v1 = excluded.neq_result_v1, " +
        "neq_result_v2 = excluded.neq_result_v2, " +
        "neq_reason_unknown = excluded.neq_reason_unknown, " +
        "neq_statistics = excluded.neq_statistics, " +
        "eq_status = excluded.eq_status, " +
        "eq_model = excluded.eq_model, " +
        "eq_result_v1 = excluded.eq_result_v1, " +
        "eq_result_v2 = excluded.eq_result_v2, " +
        "eq_reason_unknown = excluded.eq_reason_unknown, " +
        "eq_statistics = excluded.eq_statistics, " +
        "has_uif = excluded.has_uif, " +
        "has_uif_pc = excluded.has_uif_pc, " +
        "has_uif_v1 = excluded.has_uif_v1, " +
        "has_uif_v2 = excluded.has_uif_v2, " +
        "constraint_count = excluded.constraint_count, " +
        "runtime = excluded.runtime, " +
        "errors = excluded.errors";

    private static void insertOrUpdateFull(Partition partition) {
        assert partition.result != null;
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(INSERT_OR_UPDATE_FULL)) {
            ps.setObject(1, partition.id);
            ps.setObject(2, partition.iterationId);
            ps.setObject(3, partition.partition);
            ps.setObject(4, partition.result.toString());
            ps.setObject(5, partition.pcResult == null ? null : partition.pcResult.status.toInt());
            ps.setObject(6, partition.pcResult == null ? null : partition.pcResult.model);
            ps.setObject(7, partition.pcResult == null ? null : partition.pcResult.reasonUnknown);
            ps.setObject(8, partition.pcResult == null ? null : partition.pcResult.statistics);
            ps.setObject(9, partition.neqResult == null ? null : partition.neqResult.status.toInt());
            ps.setObject(10, partition.neqResult == null ? null : partition.neqResult.model);
            ps.setObject(11, partition.neqResult == null ? null : partition.neqResult.v1Result);
            ps.setObject(12, partition.neqResult == null ? null : partition.neqResult.v2Result);
            ps.setObject(13, partition.neqResult == null ? null : partition.neqResult.reasonUnknown);
            ps.setObject(14, partition.neqResult == null ? null : partition.neqResult.statistics);
            ps.setObject(15, partition.eqResult == null ? null : partition.eqResult.status.toInt());
            ps.setObject(16, partition.eqResult == null ? null : partition.eqResult.model);
            ps.setObject(17, partition.eqResult == null ? null : partition.eqResult.v1Result);
            ps.setObject(18, partition.eqResult == null ? null : partition.eqResult.v2Result);
            ps.setObject(19, partition.eqResult == null ? null : partition.eqResult.reasonUnknown);
            ps.setObject(20, partition.eqResult == null ? null : partition.eqResult.statistics);
            ps.setObject(21, partition.hasUif);
            ps.setObject(22, partition.hasUifPc);
            ps.setObject(23, partition.hasUifV1);
            ps.setObject(24, partition.hasUifV2);
            ps.setObject(25, partition.constraintCount);
            ps.setObject(26, partition.runtime);
            ps.setObject(27, partition.errors);
            ps.execute();

            // TODO: Get + set ID if inserted.
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String INSERT_OR_UPDATE_PARTIAL = "" +
        "INSERT INTO partition(" +
        "id, " +
        "iteration_id, " +
        "partition " +
        ") " +
        "VALUES (?, ?, ?) " +
        "ON CONFLICT DO NOTHING;";

    private static void insertOrUpdatePartial(Partition partition) {
        try (
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                INSERT_OR_UPDATE_PARTIAL,
                PreparedStatement.RETURN_GENERATED_KEYS
            )
        ) {
            ps.setObject(1, partition.id);
            ps.setObject(2, partition.iterationId);
            ps.setObject(3, partition.partition);
            ps.execute();

            if (partition.id == null) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    partition.id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
