//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package PASDA;

import differencing.StopWatches;
import equiv.checking.ChangeExtractor;
import equiv.checking.SMTSummary;

import java.util.ArrayList;

public class PASDA {
    protected String path;
    protected String methodPathOld;
    protected String methodPathNew;
    protected String classPathOld;
    protected String classPathNew;
    protected String toolName;

    public PASDA(
        String path,
        String path1,
        String path2,
        String tool
    ) {
        this.methodPathOld = path1;
        this.methodPathNew = path2;
        this.path = path;
        this.toolName = tool;
    }

    public void setPathToDummy(String classpath) {
        this.path = this.path + "instrumented";
        int index = this.methodPathOld.lastIndexOf("/");
        int index2 = this.methodPathNew.lastIndexOf("/");
        String package1 = this.methodPathOld.substring(index + 1);
        String package2 = this.methodPathNew.substring(index2 + 1);
        this.methodPathOld = this.path + "/" + package1;
        this.methodPathNew = this.path + "/" + package2;
        this.classPathOld = "target/classes/" + classpath + "/" + package1.split("\\.")[0] + ".class";
        this.classPathNew = "target/classes/" + classpath + "/" + package2.split("\\.")[0] + ".class";
    }

    public SMTSummary runTool() throws Exception {
        try {
            int iteration = 1;

            ChangeExtractor changeExtractor = new ChangeExtractor();
            ArrayList<Integer> changes = changeExtractor.obtainChanges(this.methodPathOld, this.methodPathNew, this.path + "instrumented");
            this.setPathToDummy(changeExtractor.getClasspath());

            PASDAInstrumentation instrumentation = new PASDAInstrumentation(
                this.toolName, this.path,
                this.classPathOld, this.classPathNew,
                this.methodPathOld, this.methodPathNew
            );

            StopWatches.stop("run:initialization");
            StopWatches.start("iteration-" + iteration);
            StopWatches.start("iteration-" + iteration + ":instrumentation");

            instrumentation.runInstrumentation(iteration, changes);

            StopWatches.stop("iteration-" + iteration + ":instrumentation");
            StopWatches.stop("iteration-" + iteration);
            StopWatches.start("run:finalization");

            return null;
        } catch (Exception e) {
            System.out.println("An error/exception occurred when instrumenting the files or running the equivalence checking. Please report this issue to us.\n\n");
            e.printStackTrace();
            throw e;
        }
    }
}
