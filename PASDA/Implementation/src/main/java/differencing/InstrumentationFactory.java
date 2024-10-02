package differencing;

import DSE.DSEInstrumentation;
import GradDiff.GradDiffInstrumentation;
import SE.SEInstrumentation;
import equiv.checking.ChangeExtractor;
import equiv.checking.SourceInstrumentation;

public class InstrumentationFactory {
    public static SourceInstrumentation create(
        String toolName,
        int timeout,
        ChangeExtractor changeExtractor
    ) {
        switch (toolName) {
            case "SE":
                return createSE(toolName, timeout, changeExtractor);
            case "DSE":
                return createDSE(toolName, timeout, changeExtractor);
            case "ARDiff":
                return createARDiff(toolName, timeout, changeExtractor);
            case "PASDA":
                return createPASDA(toolName, timeout, changeExtractor);
            default:
                throw new RuntimeException("Cannot create instrumentation for unknown tool '" + toolName + "'.");
        }
    }

    private static SEInstrumentation createSE(
        String toolName,
        int timeout,
        ChangeExtractor changeExtractor
    ) {
        return new SEInstrumentation(
            toolName,
            changeExtractor.getJavaFileDirectory(),
            changeExtractor.getOldVClassFile(),
            changeExtractor.getNewVClassFile(),
            changeExtractor.getOldVJavaFile(),
            changeExtractor.getNewVJavaFile(),
            timeout * 1000
        );

    }

    private static DSEInstrumentation createDSE(
        String toolName,
        int timeout,
        ChangeExtractor changeExtractor
    ) {
        return new DSEInstrumentation(
            toolName,
            changeExtractor.getJavaFileDirectory(),
            changeExtractor.getOldVClassFile(),
            changeExtractor.getNewVClassFile(),
            changeExtractor.getOldVJavaFile(),
            changeExtractor.getNewVJavaFile(),
            timeout * 1000
        );
    }

    private static GradDiffInstrumentation createARDiff(
        String toolName,
        int timeout,
        ChangeExtractor changeExtractor
    ) {
        return new GradDiffInstrumentation(
            toolName,
            changeExtractor.getJavaFileDirectory(),
            changeExtractor.getOldVClassFile(),
            changeExtractor.getNewVClassFile(),
            changeExtractor.getOldVJavaFile(),
            changeExtractor.getNewVJavaFile(),
            timeout * 1000,
            true,
            true,
            true,
            true,
            "H123"
        );
    }

    private static PASDAInstrumentation createPASDA(
        String toolName,
        int timeout,
        ChangeExtractor changeExtractor
    ) {
        return new PASDAInstrumentation(
            createSE(toolName, timeout, changeExtractor),
            createARDiff(toolName, timeout, changeExtractor)
        );
    }
}
