package demo.benchmarksHumaneval.NEq.COUNT_DISTINCT_CHARACTERS;

import java.util.HashSet;

public class newV {
    public static int count_distinct_characters(String string) {
        HashSet<Character> distinct = new HashSet<Character>();

        for (char c : string.toCharArray()) {
            if (! distinct.contains(Character.toLowerCase(c)))
                distinct.add(Character.toLowerCase(c));
        }
        return distinct.size();
    }
}
