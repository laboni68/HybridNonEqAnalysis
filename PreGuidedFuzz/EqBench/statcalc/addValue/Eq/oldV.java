package demo.EqBench.statcalc.addValue.Eq;
public class oldV {
	public static double addValue(double val)
    {
        double sum = 0;
        double sumOfSquares = 0;
        double mean = 0;
        double deviation = 0;
        int count = 0;
        count++;
        System.out.println("stat ");
        double currentVal = val;
        sum += currentVal;
        sumOfSquares += currentVal * currentVal;
        mean = sum / count;
        deviation = Math.sqrt( (sumOfSquares / count) - (mean * mean) );
        return deviation;
    }
}