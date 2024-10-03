package demo.benchmarksHumaneval.NEq.GET_POSITIVE;

import java.util.ArrayList;
import java.util.List;

public class newV {
    public static List<Integer> get_positive(List<Integer> numbers) {
        List<Integer> result = new ArrayList<Integer>();

        for (Integer number : numbers) {
            if (number > 0){
                result.add(number);
            }
        }
        return result;
    }
}
