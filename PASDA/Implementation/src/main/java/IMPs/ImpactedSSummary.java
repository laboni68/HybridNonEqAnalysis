//MIT-LICENSE
//Copyright (c) 2020-, Sahar Badihi, The University of British Columbia, and a number of other of contributors
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
//to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package IMPs;

import equiv.checking.SMTSummary;

import java.util.ArrayList;

public class ImpactedSSummary extends SMTSummary {
    private final ArrayList<Integer> impacted; //the list of impacted statements

    public ImpactedSSummary(String path, String oldFileName, String newFileName, int timeout, ArrayList<Integer> impacted) {
        super(path, oldFileName, newFileName, timeout);
        this.impacted = impacted;
    }

    /**
     * This functions parse a constraint if it is linked to an impacted statement, returns null otherwise
     * @param st a JPF constraint as a string
     * @return a string or null
     */
    @Override
    public String obtainConstraint(String st) {
        //here we need to keep only what is impacted
        String[] split = st.split(":");
        //check if the line is in impacted
        if (this.impacted.contains(Integer.parseInt(split[0]))) {
            return split[1].split("&&")[0];
        }
        return null;
    }

    /**
     * This functions parse a return constraint if it is linked to an impacted statement, returns null otherwise
     * @return a string or null;
     */
    @Override
    public String obtainReturn(String st) {
        String[] split = st.split(": Ret = ");
        if (this.impacted.contains(Integer.parseInt(split[0]))) {
            return split[1];
        }
        return null;
    }
}
