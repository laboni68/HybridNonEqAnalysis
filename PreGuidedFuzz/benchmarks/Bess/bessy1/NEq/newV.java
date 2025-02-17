package demo.benchmarks.Bess.bessy1.NEq;
public class newV{
    public static double snippet (double x) {
        double z = 0;
        double xx = 0;
        double y = 0;
        double ans = 0;
        double ans1= 0;
        double ans2= 0;
        if (x > 8.0 ) {//change
            if(x!=0){
            y=x*x;
            ans1=x*(-0.4900604943e13+y*(0.1275274390e13 +y*(-0.5153438139e11+y*(0.7349264551e9 +y*(-0.4237922726e7+y*0.8511937935e4)))));
            ans2=0.2499580570e14+y*(0.4244419664e12 +y*(0.3733650367e10+y*(0.2245904002e8 +y*(0.1020426050e6+y*(0.3549632885e3+y)))));
            ans=(ans1/ans2)+0.636619772*(bessj1(x)*Math.log(x)-(1.0/x));
            }
        }
        else {
            z=8.0/x;
            y=z*z;
            xx=x-2.356194491;
            ans1=1.0+y*(0.183105e-2+y*(-0.3516396496e-4 +y*(0.2457520174e-5+y*(-0.240337019e-6))));
            ans2=0.04687499995+y*(-0.2002690873e-3 +y*(0.8449199096e-5+y*(-0.88228987e-6 +y*0.105787412e-6)));
            ans=Math.sqrt(0.636619772/x)*(Math.sin(xx)*ans1+z*Math.cos(xx)*ans2);
        }
        return ans + xx;//change
    }
    private static double bessj1(double x){
        double  ax,z,xx,y,ans,ans1,ans2;

        if ((ax=Math.abs(x)) < 8.0) {
            y=x*x;
            ans1=x*(72362614232.0+y*(-7895059235.0+y*(242396853.1
                    +y*(-2972611.439+y*(15704.48260+y*(-30.16036606))))));
            ans2=144725228442.0+y*(2300535178.0+y*(18583304.74
                    +y*(99447.43394+y*(376.9991397+y*1.0))));
            ans=ans1/ans2;
        } else {
            z=8.0/ax;
            y=z*z;
            xx=ax-2.356194491;
            ans1=1.0+y*(0.183105e-2+y*(-0.3516396496e-4
                    +y*(0.2457520174e-5+y*(-0.240337019e-6))));
            ans2=0.04687499995+y*(-0.2002690873e-3
                    +y*(0.8449199096e-5+y*(-0.88228987e-6
                    +y*0.105787412e-6)));
            ans=Math.sqrt(0.636619772/ax)*(Math.cos(xx)*ans1-z*Math.sin(xx)*ans2);
            if (x < 0.0) ans = -ans;
        }
        return ans;
    }
}