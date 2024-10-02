package differencing.repositories;

import differencing.models.PartitionInstruction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PartitionInstructionRepository extends Repository {
    private static final String INSERT_OR_UPDATE = "" +
        "INSERT INTO partition_instruction(" +
        "id, " +
        "partition_id, " +
        "instruction_id, " +
        "version, " +
        "execution_index, " +
        "state, " +
        "choice" +
        ") " +
        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
        "ON CONFLICT DO UPDATE SET " +
        "partition_id = excluded.partition_id, " +
        "instruction_id = excluded.instruction_id, " +
        "version = excluded.version, " +
        "execution_index = excluded.execution_index, " +
        "state = excluded.state, " +
        "choice = excluded.choice";

    public static void insertOrUpdate(Iterable<PartitionInstruction> partitionInstructions) {
        for (PartitionInstruction partitionInstruction: partitionInstructions) {
            insertOrUpdate(partitionInstruction);
        }
    }

    public static void insertOrUpdate(PartitionInstruction partitionInstruction) {
        try (
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                INSERT_OR_UPDATE,
                PreparedStatement.RETURN_GENERATED_KEYS
            )
        ) {
            ps.setObject(1, partitionInstruction.id);
            ps.setObject(2, partitionInstruction.partitionId);
            ps.setObject(3, partitionInstruction.instructionId);
            ps.setObject(4, partitionInstruction.version);
            ps.setObject(5, partitionInstruction.executionIndex);
            ps.setObject(6, partitionInstruction.state);
            ps.setObject(7, partitionInstruction.choice);
            ps.execute();

            if (partitionInstruction.id == null) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    partitionInstruction.id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
