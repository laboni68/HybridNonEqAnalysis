package demo.benchmarksHumaneval.NEq.SORT_THIRD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class oldV {
    public static List<Integer> sort_third(List<Integer> l) {
        List<Integer> result = new ArrayList<Integer>();

        List<Integer> thirds = new ArrayList<Integer>();
        for (int i = 0; i < l.size(); i += 3){
            thirds.add(l.get(i));
        }
        Collections.sort(thirds);

        for (int i = 0; i < l.size(); i += 1){
            if (i % 3 == 0){
                result.add(thirds.get(i));
            } else{
                result.add(l.get(i));
            }
        }
        return result;
    }
}
