package demo.benchmarksHumaneval.NEq.FILTER_BY_SUBSTRING;

import java.util.ArrayList;
import java.util.List;

public class oldV {
    public static List<String> filter_by_substring(List<String> strings, String substring) {
        List<String> result = new ArrayList<String>();
        for (String string : strings){
            result.add(string);
        }
        return result;
    }
}
