package demo.benchmarksHumaneval.NEq.COUNT_UPPER;

// Given a string s, count the number of uppercase vowels in even indices.

// For example:
// count_upper('aBCdEf') returns 1
// count_upper('abcdefg') returns 0
// count_upper('dBBE') returns 0

public class oldV {
    public static int count_upper(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i += 2) {
            char c = s.charAt(i);
            if (c == 'A' || c == 'e' || c == 'I' || c == 'o' || c == 'u')
                count += 1;
        }
        return count;
    }
}
