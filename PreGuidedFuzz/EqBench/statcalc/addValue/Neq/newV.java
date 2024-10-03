package demo.EqBench.statcalc.addValue.Neq;
public class newV {
    static double sum = 0;
    static double sumOfSquares = 0;
    static double mean = 0;
    static int count = 0;
	public static double addValue(double val)
    {
        double deviation = 0;
        count++;
        //System.out.println("stat ");
        double currentVal = val;
        sum += currentVal;
        sumOfSquares += currentVal * currentVal;
        mean = sum / count;
        deviation = Math.sqrt( (sumOfSquares * count) - (mean * mean) );//change
        return deviation;
    }
}