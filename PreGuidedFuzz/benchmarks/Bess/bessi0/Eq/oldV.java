package demo.benchmarks.Bess.bessi0.Eq;
public class oldV{
    public static double snippet(double x) {
        double ax = 0;
        double ans = 0;
        double y = 0;
        ax=Math.abs(x);
        if (ax < 3.75) {
            y=x/3.75;
            y*=y;
            ans=1.0+y+(3.5156229+y*(3.0899424+y*(1.2067492+y*(0.2659732+y*(0.360768e-1+y*0.45813e-2)))));
        }
        else {
            y=3.75/ax;
            ans=Math.abs(x)*(0.39894228+y*(0.1328592e-1 +y*(0.225319e-2+y*(-0.157565e-2+y*(0.916281e-2 +y*(-0.2057706e-1+y*(0.2635537e-1+y*(-0.1647633e-1 +y*0.392377e-2))))))));
        }
        return ans;
    }
}