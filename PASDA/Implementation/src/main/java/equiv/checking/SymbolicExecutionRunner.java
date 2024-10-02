//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package equiv.checking;

import differencing.IgnoreUnreachablePathsListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.symbc.SymbolicListener;

import java.io.*;

public class SymbolicExecutionRunner {
    /**
     * This class runs the symbolic execution with JPF-symbc
     **/
    protected String oldFileName;
    protected String newFileName;
    protected String targetMethod;
    protected String path;
    protected int methodInputs;
    protected int bound;
    protected int timeout;
    protected String SMTSolver;
    protected int minInt, maxInt;
    protected double minDouble, maxDouble;
    protected long minLong, maxLong;
    protected String packageName;

    public SymbolicExecutionRunner(
        String path,
        String packageName,
        String oldFile,
        String newFile,
        String targetMethod,
        int methodInputs,
        int bound,
        int timeout,
        String SMTSolver,
        int minInt,
        int maxInt,
        double minDouble,
        double maxDouble,
        long minLong,
        long maxLong
    ) {
        this.oldFileName = oldFile;
        this.newFileName = newFile;
        this.path = path + "/";
        this.packageName = packageName;
        this.targetMethod = targetMethod;
        this.methodInputs = methodInputs;
        this.bound = bound;
        this.timeout = timeout;
        this.SMTSolver = SMTSolver;
        this.minInt = minInt;
        this.maxInt = maxInt;
        this.minDouble = minDouble;
        this.maxDouble = maxDouble;
        this.minLong = minLong;
        this.maxLong = maxLong;
    }

    /**
     * This functions creates the .jpf files for symbolic execution
     */
    public void creatingJpfFiles() throws IOException {
        String JPFMethodInputs = createSymbolicInputParametersForInstrumentedJPF();
        String fixed = "classpath=" + ProjectPaths.classpath.replace("\\", "\\\\") + " \n" +
            "symbolic.min_int=" + this.minInt + "\n" +
            "symbolic.max_int=" + this.maxInt + "\n" +
            "symbolic.min_long=" + this.minLong + "\n" +
            "symbolic.max_long=" + this.maxLong + "\n" +
            "symbolic.min_double=" + this.minDouble + "\n" +
            "symbolic.max_double=" + this.maxDouble + "\n" +
            "symbolic.debug = false \n" +
            "symbolic.optimizechoices = false \n" +
            //"search.class = .search.heuristic.BFSHeuristic \n"+
            "symbolic.lazy=on \n" +
            "symbolic.arrays=true \n" +
            "symbolic.strings = true \n" +
            "symbolic.dp=" + this.SMTSolver + " \n" +
            "symbolic.string_dp_timeout_ms=" + this.timeout + "\n" +
            "search.depth_limit=" + this.bound + "\n" +
            "search.multiple_errors=true \n" +
            "search.class = .search.CustomSearch \n";

        File newFile = new File(path + this.oldFileName + ".jpf");
        newFile.getParentFile().mkdir();
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        FileWriter fwOld = new FileWriter(newFile);
        BufferedWriter writerOld = new BufferedWriter(fwOld);
        String target = "target = " + packageName + "." + this.oldFileName + "\n";
        String symbolic_method = "symbolic.method = " + packageName + "." + oldFileName + "." + targetMethod + JPFMethodInputs + "\n";
        String jpfString = target + symbolic_method + fixed;
        writerOld.write(jpfString);
        writerOld.close();
        fwOld.close();
        newFile = new File(path + this.newFileName + ".jpf");
        newFile.getParentFile().mkdir();
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        FileWriter fwNew = new FileWriter(newFile);
        BufferedWriter writerNew = new BufferedWriter(fwNew);
        target = "target = " + this.packageName + "." + this.newFileName + "\n";
        symbolic_method = "symbolic.method = " + packageName + "." + newFileName + "." + targetMethod + JPFMethodInputs + "\n";
        jpfString = target + symbolic_method + fixed;
        writerNew.write(jpfString);
        writerNew.close();
        fwNew.close();
    }

    /**
     * This function creates the proper JPF representation for the target method
     * @return (sym#sym#sym) for a method with 3 arguments
     */
    public String createSymbolicInputParametersForInstrumentedJPF() {
        //(sym#sym#sym#sym#sym#sym#sym#sym)
        String result = "(";
        ////////////////////////////////////////
        //adding input parameters
        if (methodInputs > 0) {
            for (int i = 0; i < methodInputs - 1; i++) {
                result = result + "sym#";
            }
            result = result + "sym";
        }
        ////////////////////////
        result = result + ")";
        return result;
    }

    /**
     * This functions runs JPF on both programs
     */
    public void runningJavaPathFinder() throws IOException {
        runningOnProgram(oldFileName);
        runningOnProgram(newFileName);
    }

    /**
     * This function runs JPF on a program
     * @param fileName the program
     */
    public void runningOnProgram(String fileName) throws IOException {
        PrintStream systemOutputStream = System.out;
        PrintStream systemErrorStream = System.err;

        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(this.path + fileName + "JPFOutput.txt"))));
        System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(this.path + fileName + "JPFError.txt"))));

        try {
            File configFile = new File(this.path + fileName + ".jpf");
            Config config = JPF.createConfig(new String[]{configFile.getAbsolutePath()});
            JPF jpf = new JPF(config);

            jpf.addListener(new IgnoreUnreachablePathsListener(this.timeout / 1000));
            jpf.addListener(new SymbolicListener(config, jpf));
            jpf.run();
        } finally {
            System.setOut(systemOutputStream);
            System.setErr(systemErrorStream);
        }
    }
}
