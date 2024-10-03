package demo.benchmarksHumaneval.NEq.CORRECT_BRACKETING;

/* brackets is a string of "<" and ">".
return True if every opening bracket has a corresponding closing bracket.

>>> newV_bracketing("<")
False
>>> newV_bracketing("<>")
True
>>> newV_bracketing("<<><>>")
True
>>> newV_bracketing("><<>")
False */

public class newV {
    public static boolean newV_bracketing(String brackets) {
        int depth = 0;
        for (char b : brackets.toCharArray()) {
            if (b == '<')
                depth += 1;
            else
                depth -= 1;
            if (depth < 0)
                return false;
        }
        return depth == 0;
    }
}
