package demo.benchmarksHumaneval.NEq.MEAN_ABSOLUTE_DEVIATION;

import java.util.List;

public class oldV {
    public static double mean_absolute_deviation(List<Double> numbers) {
        double sum = 0.;
        for (Double number : numbers){
            sum += number;
        }
        double mean = sum / numbers.size();
        double deviation_sum = 0.;
        for (Double number : numbers){
            deviation_sum += (number - mean) * (number - mean);
        }
        return deviation_sum / numbers.size();
    }
}
