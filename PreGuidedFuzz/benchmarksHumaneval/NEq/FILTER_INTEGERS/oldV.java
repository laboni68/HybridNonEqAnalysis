package demo.benchmarksHumaneval.NEq.FILTER_INTEGERS;

import java.util.ArrayList;
import java.util.List;

public class oldV {
    public static List<Integer> filter_integers(List<Object> values) {
        List<Integer> result = new ArrayList<Integer>();

        for (Object value : values){
            result.add((Integer) value);
        }
        return result;
    }
}
