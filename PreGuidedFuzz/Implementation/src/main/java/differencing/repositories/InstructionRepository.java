package differencing.repositories;

import differencing.models.Instruction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InstructionRepository extends Repository {
    private static final String INSERT_OR_UPDATE = "" +
        "INSERT INTO instruction(" +
        "id, " +
        "iteration_id, " +
        "method, " +
        "instruction_index, " +
        "instruction, " +
        "position, " +
        "source_file, " +
        "source_line" +
        ") " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON CONFLICT DO UPDATE SET " +
        "iteration_id = excluded.iteration_id, " +
        "method = excluded.method, " +
        "instruction_index = excluded.instruction_index, " +
        "instruction = excluded.instruction, " +
        "position = excluded.position, " +
        "source_file = excluded.source_file, " +
        "source_line = excluded.source_line";

    public static void insertOrUpdate(Iterable<Instruction> instructions) {
        for (Instruction instruction: instructions) {
            insertOrUpdate(instruction);
        }
    }

    public static void insertOrUpdate(Instruction instruction) {
        try (
            Connection conn = connect();
            PreparedStatement ps = conn.prepareStatement(
                INSERT_OR_UPDATE,
                PreparedStatement.RETURN_GENERATED_KEYS
            )
        ) {
            ps.setObject(1, instruction.id);
            ps.setObject(2, instruction.iterationId);
            ps.setObject(3, instruction.method);
            ps.setObject(4, instruction.instructionIndex);
            ps.setObject(5, instruction.instruction);
            ps.setObject(6, instruction.position);
            ps.setObject(7, instruction.sourceFile);
            ps.setObject(8, instruction.sourceLine);
            ps.execute();

            if (instruction.id == null) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    instruction.id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
