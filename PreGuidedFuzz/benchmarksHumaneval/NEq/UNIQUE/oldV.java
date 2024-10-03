package demo.benchmarksHumaneval.NEq.UNIQUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class oldV {
    public static List<Integer> unique(List<Integer> l) {
        List<Integer> result = new ArrayList<Integer>();

        for (Integer n : l){
            result.add(n);
        }
        Collections.sort(result);

        return result;
    }
}
