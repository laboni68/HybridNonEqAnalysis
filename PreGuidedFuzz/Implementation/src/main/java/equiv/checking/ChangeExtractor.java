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

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import equiv.checking.custom.CustomJdtTreeGenerator;
import equiv.checking.custom.CustomSerializer;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * This class extracts the changes between the two programs in the form of an ArrayList containing the lines numbers
 */

public class ChangeExtractor implements Utils{
    protected String path1;
    protected String path2;
    protected String classpath;
    protected String oldVClassFile;
    protected String newVClassFile;
    protected String javaFileDirectory;
    protected String oldVJavaFile;
    protected String newVJavaFile;

    public String getClasspath() {
        return this.classpath;
    }

    public String getOldVClassFile() {
        return this.oldVClassFile;
    }

    public String getNewVClassFile() {
        return this.newVClassFile;
    }

    public String getJavaFileDirectory() {
        return this.javaFileDirectory;
    }

    public String getOldVJavaFile() {
        return this.oldVJavaFile;
    }

    public String getNewVJavaFile() {
        return this.newVJavaFile;
    }

    /**
     * This methods returns the list of changes given the paths to two programs
     * @param methodPath1 the path to the first program
     * @param methodPath2 the path to the second program
     * @return the list of changes result
     */
    public ArrayList<Integer> obtainChanges(String methodPath1, String methodPath2, String path) throws Exception {
        this.trimOriginalFiles(methodPath1, methodPath2, path);
        Map<Integer, String[]> changes = this.obtainMapChanges(this.path1, this.path2);
        this.modifyFiles(this.path1, this.path2, changes);
        changes.keySet().removeIf(entry -> {
            String[] info = changes.get(entry);
            return info != null && info[0].contains("C");
        });
        Set<Integer> keySet = changes.keySet();
        ArrayList<Integer> result = new ArrayList<>(keySet);
        Collections.sort(result);
        if (result.size() > 0) {
            result.remove(0);
        }

        String oldVClassName = FilenameUtils.getBaseName(methodPath1);
        String newVClassName = FilenameUtils.getBaseName(methodPath2);

        this.oldVClassFile = Paths.get(ProjectPaths.classpath, this.classpath, oldVClassName + ".class").toString();
        this.newVClassFile = Paths.get(ProjectPaths.classpath, this.classpath, newVClassName + ".class").toString();

        Path javaFileDirectoryPath = Paths.get(methodPath1).getParent().resolve("instrumented");
        this.javaFileDirectory = javaFileDirectoryPath.toString();
        this.oldVJavaFile = javaFileDirectoryPath.resolve(oldVClassName + ".java").toString() ;
        this.newVJavaFile = javaFileDirectoryPath.resolve(newVClassName + ".java").toString() ;

        return result;
    }

    /**
     * This method removes all empty lines from the two class files and copy the files under path/instrumented
     */
    public void trimOriginalFiles(String methodPath1, String methodPath2, String path) throws IOException {
        String prog1 = "", prog2 = "";
        File file1 = new File(methodPath1), file2 = new File(methodPath2);
        BufferedReader br1 = new BufferedReader(new FileReader(file1));
        BufferedReader br2 = new BufferedReader(new FileReader(file2));
        int index = methodPath1.lastIndexOf("/");
        int index2 = methodPath2.lastIndexOf("/");
        String dummy1 = path + "/" + methodPath1.substring(index + 1);
        String dummy2 = path + "/" + methodPath2.substring(index2 + 1);
        this.path1 = dummy1;
        this.path2 = dummy2;
        String line, packageLine = "";
        int i = 0;
        while ((line = br1.readLine()) != null) {
            if (!line.trim().isEmpty()) {//the first line is package or there is no package
                if (i == 0) {
                    String add = "package instrumented";
                    this.classpath = "instrumented";
                    if (line.contains("package")) {
                        add = line.split(";")[0] + ".instrumented";
                        this.classpath = add.split("package\\s*")[1].trim();
                        this.classpath = this.classpath.replace(".", "/");
                    }
                    add += ";\n";
                    packageLine = add;
                    prog1 += add;
                    prog1 += "import equiv.checking.symbex.UnInterpreted;\n";
                    if (!line.contains("package") && !line.trim().isEmpty())
                        prog1 += line + "\n";
                    i++;
                } else {
                    prog1 += line + "\n";
                }

            }
        }
        i = 0;
        while ((line = br2.readLine()) != null) {
            if (!line.trim().isEmpty()) {//the first line is package or there is no package
                if (i == 0) {
                    prog2 += packageLine;
                    prog2 += "import equiv.checking.symbex.UnInterpreted;\n";
                    if (!line.contains("package") && !line.trim().isEmpty())
                        prog2 += line + "\n";
                    i++;
                } else {
                    prog2 += line + "\n";
                }
            }

        }
        file1 = new File(dummy1);
        file2 = new File(dummy2);
        file1.getParentFile().mkdirs();
        if (!file1.exists()) {
            file1.createNewFile();
        }
        if (!file2.exists()) {
            file2.createNewFile();
        }
        FileWriter writer = new FileWriter(file1);
        writer.write(prog1);
        writer.close();
        writer = new FileWriter(file2);
        writer.write(prog2);
        writer.close();
    }

    /**
     * This method generates the modifications between two programs using GumTree
     * @param methodPath1 the path to the first program
     * @param methodPath2 the path to the second program
     * @return a map from line number to operations on the line (e.g 3 --> [U], where U stands for update)
     */
    public Map<Integer, String[]> obtainMapChanges(String methodPath1, String methodPath2) throws Exception {
        TreeContext srctxt = new CustomJdtTreeGenerator().generateFromFile(methodPath1), dsttxt = new CustomJdtTreeGenerator().generateFromFile(methodPath2);
        ITree src = srctxt.getRoot();
        ITree dst = dsttxt.getRoot();
        Matcher m = Matchers.getInstance().getMatcher(src, dst);
        m.match();
        MappingStore mappings = m.getMappings();
        ActionGenerator g = new ActionGenerator(src, dst, mappings);
        g.generate();
        String gumtree = this.path1.substring(0, this.path1.lastIndexOf("/") + 1) + "gumtree.txt";
        File newFile = new File(gumtree);
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        FileWriter writer = new FileWriter(newFile);
        Map<Integer, String[]> changes = new CustomSerializer(srctxt, dsttxt, g.getActions(), mappings).writeTo(writer);
        writer.close();
        return changes;
    }

    /**
     * This function serves to add dummy statements in the programs to make their lines match
     * @param path1 the path to the first program
     * @param path2 the path to the second program
     * @param changes the list of changed lines between the two programs
     * U,u,M stand for updated lines, I insertion, D deletion,
     * EI is the end of an inserted block and ED the end of a deleted block
     * C means that we only copy from one of the program (so it's technically not a change)
     * @throws IOException
     */
    public void modifyFiles(String path1, String path2, Map<Integer, String[]> changes) throws IOException {
        String prog1 = "";
        String prog2 = "";
        File file1 = new File(path1), file2 = new File(path2);
        BufferedReader br1 = new BufferedReader(new FileReader(file1));
        BufferedReader br2 = new BufferedReader(new FileReader(file2));
        int last = 1;
        for (Integer line : changes.keySet()) {
            for (int i = last; i < line; i++) {
                String toAdd1 = br1.readLine() + "\n";
                String toAdd2 = br2.readLine() + "\n";
                prog1 += toAdd1;
                prog2 += toAdd2;
            }
            last = line + 1;
            String[] info = changes.get(line);
            switch (info[0]) {
                case "U":
                case "u":
                case "M":
                    prog1 += br1.readLine() + "\n";
                    prog2 += br2.readLine() + "\n";
                    break;
                case "EI":
                case "CI":
                    String inserted = br2.readLine() + "\n";
                    prog1 += inserted;
                    prog2 += inserted;
                    break;
                case "ED":
                case "CD":
                    String delete = br1.readLine() + "\n";
                    prog1 += delete;
                    prog2 += delete;
                    break;
                case "I":
                    if (info.length == 2) {
                        if (info[1].equals("VariableDeclarationStatement") || info[1].equals("ReturnStatement")) {
                            String insert = br2.readLine() + "\n";
                            prog1 += insert;
                            prog2 += insert;
                        } else { //if statement
                            prog1 += info[1] + "\n";
                            prog2 += br2.readLine() + "\n";
                        }
                    } else {
                        prog1 += info[2] + " = " + info[2] + ";\n";
                        prog2 += br2.readLine() + "\n";
                    }
                    break;
                case "D":
                    if (info.length == 2) {
                        if (info[1].equals("VariableDeclarationStatement") || info[1].equals("ReturnStatement")) {
                            String deleted = br1.readLine() + "\n";
                            prog1 += deleted;
                            prog2 += deleted;
                        } else {
                            prog1 += br1.readLine() + "\n";
                            prog2 += info[1] + "\n";
                        }
                    } else { //Assignment deleted ?
                        prog1 += br1.readLine() + "\n";
                        prog2 += info[2] + " = " + info[2] + ";\n";
                    }
                    break;
            }
            /* break; */
        }
        String line = br1.readLine();
        while (line != null) {
            prog1 += line + "\n";
            prog2 += br2.readLine() + "\n";
            line = br1.readLine();
        }
        //maybe not, here assumption that even empty lines are the same in common blocks of code !
        line = br2.readLine();
        while (line != null) {
            prog2 += line + "\n";
            line = br2.readLine();
        }
        //Here maybe overwrite the files ?
        //here add the code to change the class name and the file name
        FileWriter writer = new FileWriter(file1);
        writer.write(prog1);
        writer.close();
        writer = new FileWriter(file2);
        writer.write(prog2);
        writer.close();
        compile(ProjectPaths.classpath, file1);
        compile(ProjectPaths.classpath, file2);
    }
}
