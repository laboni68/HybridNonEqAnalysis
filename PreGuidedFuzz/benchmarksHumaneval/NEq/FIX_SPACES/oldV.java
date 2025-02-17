package demo.benchmarksHumaneval.NEq.FIX_SPACES;

/* Given a string text, replace all spaces in it with underscores, 
and if a string has more than 2 consecutive spaces, 
then replace all consecutive spaces with - 

fix_spaces("Example") == "Example"
fix_spaces("Example 1") == "Example_1"
fix_spaces(" Example 2") == "_Example_2"
fix_spaces(" Example   3") == "_Example-3" */

public class oldV {
    public static String fix_spaces(String text) {
        String new_text = "";
        int i = 0;
        int start = 0;
        int end = 0;
        while (i < text.length()) {
            if (text.charAt(i) == ' ') end += 1;
            else {
                if (end - start > 2) new_text += "-" + text.substring(i, i + 1);
                else if (end - start > 0) {
                    for (int j = start; j < end; j += 1) {
                        new_text += "_";
                    }
                    new_text += text.substring(i, i + 1);
                } else {
                    new_text += text.substring(i, i + 1);
                }
                start = i + 1;
                end = i + 1;
            }
            i += 1;
        }

        return new_text;
    }
}
