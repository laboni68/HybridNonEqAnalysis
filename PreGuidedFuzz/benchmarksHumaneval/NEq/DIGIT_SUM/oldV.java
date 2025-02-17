package demo.benchmarksHumaneval.NEq.DIGIT_SUM;

/* Task
Write a function that takes a string as input and returns the sum of the upper characters only'
ASCII codes.

Examples:
    digitSum("") => 0
    digitSum("abAB") => 131
    digitSum("abcCd") => 67
    digitSum("helloE") => 69
    digitSum("woArBld") => 131
    digitSum("aAaaaXa") => 153 */

public class oldV {
    public static int digit_sum(String s) {
        int result = 0;
        for (char c : s.toCharArray()) {
            if (c == ' ')
                break;
            if ('A' <= c && c <= 'Z')
                result += (int) c;
        }
        return result;
    }
}
