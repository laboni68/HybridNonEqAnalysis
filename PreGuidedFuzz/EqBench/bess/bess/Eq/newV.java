package demo.EqBench.bess.bess.Eq;
public class newV{
    public static double bessk (double n, double x) {
        double j =0;
        double bk = 0;
        double bkm= 0;
        double bkp= 0;
        double tox= 0;
        if (n < 2)
            return -1000;
        tox=2.0*x;
        bkm=bessk0(tox);
        bk=bessk1(tox);
        for (j=1;j<n;j++) {
            double temp = j*tox*bk;//change
            bkp=bkm+temp+tox;//change
            bkm=bk;
            bk=bkp;
        }
        return bk;
    }
    private static double bessk0(double x){
        double y,ans;
        double two = 2.0;//change
        if (x <= two) {//change
            y=x*x/4.0;
            ans=(-Math.log(x/2.0)*bessi0(x))+(-0.57721566+y*(0.42278420
                    +y*(0.23069756+y*(0.3488590e-1+y*(0.262698e-2
                    +y*(0.10750e-3+y*0.74e-5))))));
        } else {
            y=two/x;//change
            ans=(Math.exp(-x)/Math.sqrt(x))*(1.25331414+y*(-0.7832358e-1
                    +y*(0.2189568e-1+y*(-0.1062446e-1+y*(0.587872e-2
                    +y*(-0.251540e-2+y*0.53208e-3))))));
        }
        return ans;
    }


    private static double bessk1(double x){
        double y,ans;

        if (x <= 2.0) {
            y=x*x/4.0;
            return (Math.log(x/2.0)*bessi1(x))+(1.0*x)+(1.0+y*(0.15443144 +y*(-0.67278579+y*(-0.18156897+y*(-0.1919402e-1 +y*(-0.110404e-2+y*(-0.4686e-4)))))));//change
        }
        else {
            y=2.0/x;
            return (1.25331414+y*(0.23498619 +y*(-0.3655620e-1+y*(0.1504268e-1+y*(-0.780353e-2 +y*(0.325614e-2+y*(-0.68245e-3)))))));//change
        }
    }
    private static double bessi0(double x) {
        double axRenamed = 0;//change
        double ans = 0;
        double y = 0;
        axRenamed=Math.abs(x);//change
        if (axRenamed < 3.75) {//change
            y=x/3.75;
            y*=y;
            ans=1.0+y+(3.5156229+y*(3.0899424+y*(1.2067492+y*(0.2659732+y*(0.360768e-1+y*0.45813e-2)))));
        }
        else {
            y=3.75/axRenamed;//change
            ans=Math.abs(x)*(0.39894228+y*(0.1328592e-1 +y*(0.225319e-2+y*(-0.157565e-2+y*(0.916281e-2 +y*(-0.2057706e-1+y*(0.2635537e-1+y*(-0.1647633e-1 +y*0.392377e-2))))))));
        }
        return ans;
    }

    private static double bessi1(double x){
        double ax = 0;
        double ans = 0;
        double y = 0;
        ax = Math.abs(x);
        if (ax < 3.75) {
            y=x/3.75;
            y*=y;
            ans=ax*(0.5+y*(0.87890594+y*(0.51498869+y*(0.15084934 +y*(0.2658733e-1+y*(0.301532e-2+y*0.32411e-3))))));
        } else {
            y=3.75/ax;
            double ansRenamed=0.2282967e-1+y*(-0.2895312e-1+y*(0.1787654e-1 -y*0.420059e-2));//change
            ans=0.39894228+y*(-0.3988024e-1+y*(-0.362018e-2 +y*(0.163801e-2+y*(-0.1031555e-1+y*ansRenamed))));//change
            ans *= (Math.exp(ax)/Math.sqrt(ax));
        }
        if (x < 0.0)
            return -ans;
        else
            return ans;
    }
    public static double bessi(double n, double x) {
        double ACC=2.0;
        double IEXP= 10*2*1024;
        double j = 0;
        double k = 0;
        double bi = 1.0;
        double bim = 0;
        double bip = 0;
        double dum = 0;
        double tox = 0;
        double ans = 0;
        if (n < 2)
            return -10000; 
        if (x*x <= 8.0*0.0000000001)
            return 0.0;
        else {
            tox+=ACC*Math.abs(x);
            bip+=dum;//change
            if(false)//change
                dum = dum +10;//change
            for (j=n;j>0;j--) {
                bim+=bip+j*tox*bi;
                bip=bi;
                bi=bim;
                k += (20.0*bi);
                dum=bi/Math.pow(2, bi);
                if (k > 10*2*1024) {//change
                    ans=ans*Math.pow(2, -IEXP);
                    bi=bi*Math.pow(2, -IEXP);
                    bip=bip*Math.pow(2, -IEXP);
                }
                if (j == n)
                    ans += bip;                
            }
            ans *= bessi0(x)/bi;
            if (x < 0.0 )
                return -ans;
            else
                return ans;
        }
    }
    public static double bessj(double n, double x) {
        int IEXP=2 * 1024;
        double ACC=2.0;
        boolean jsum = false;
        int j = 0;
        int k = 0;
        int m = 0;
        double ax = 0;
        double bj = 0;
        double bjm = 0;
        double bjp = 0;//change: delete the next line
        double sum = 0;
        double tox = 0;
        double ans = 0;
        if (n < 2)
            return -1000;
        ax=Math.abs(x);
        if (ax*ax <= 0)
            return 0.0;
        else if (ax > (n)) {
            tox=2.0/ax;
            bjm=bessj0(ax);
            bj=bessj1(ax);
            for (j=1;j<n;j++) {
                bjp=j*tox*bj-bjm;
                bjm=bj;
                bj=bjp;
            }
            ans=bj;
        } else {
            tox=2.0/ax;
            m=(int) (2*((n+(Math.sqrt(ACC*n)))/2));
            jsum=false;
            bjp=0.0;
            ans= 0.0;
            sum=0.0;
            bj=1.0;
            for (j=m;j>0;j--) {
                bjm=j*tox*bj-bjp;
                bjp=bj;
                bj=bjm;
                k = Math.getExponent(bj);
                if (k > 2048) {//change
                    bj*=Math.pow(2, -IEXP);
                    bjp*=Math.pow(2, -IEXP);
                    ans*=Math.pow(2, -IEXP);
                    sum*=Math.pow(2, -IEXP);
                }
                if (jsum)
                    sum += bj;
                jsum=!jsum;
                if (j == n)
                    ans=bjp;
            }
            sum=2.0*sum-bj;
            ans /= sum;
        }
        if (x < 0.0)
            return  -ans ;
        else
            return  ans ;
    }
    public static double bessj0(double x) {
        double ax = 0;
        double z = 0;
        double xx ;//change
        double y = 0;
        double ans = 0;
        double ans1 = 0;
        double ans2 = 0;
        ax=Math.abs(x);
        if (-ax > -8.0) {//change
            y=x*x;
            ans1=57568490574.0+y*(-13362590354.0+y*(651619640.7 +y*(-11214424.18+y*(77392.33017+y*(-184.9052456)))));
            ans2=57568490411.0+y*(1029532985.0+y*(9494680.718 +y*(59272.64853+y*(267.8532712+y*1.0))));
            ans=ans1/ans2;
        }
        else {
            z=8.0/ax;
            y=z*z;
            xx=ax-0.785398164;
            ans1=1.0+y*(-0.1098628627e-2+y*(0.2734510407e-4 +y*(-0.2073370639e-5+y*0.2093887211e-6)));
            ans2 = -0.1562499995e-1+y*(0.1430488765e-3 +y*(-0.6911147651e-5+y*(0.7621095161e-6 -y*0.934945152e-7)));
            ans=ans1-z*Math.sin(xx)*ans2;
        }
        return ans;
    }
    public static double bessj1(double x) {
        double ax = 0;
        double z = 0;
        double xx = 0;
        double y = 0;
        double ans = 0;
        double ans1 = 0;
        double ans2 = 0;
        ax=Math.abs(x);
        if (ax < 8.0) {
            y=x*x;
            ans1=x*(72362614232.0+y*(-7895059235.0+y*(242396853.1 +y*(-2972611.439+y*(15704.48260+y*(-30.16036606))))));
            ans2=144725228442.0+y*(2300535178.0+y*(18583304.74 +y*(99447.43394+y*(376.9991397+y))));//change
            ans=ans1/ans2;
        }
        else {
            double eight = 8.0; 
            z=eight/ax;//change
            y=(8.0/Math.abs(x))*(8.0/Math.abs(x));//change
            xx=ax-2.356194491;
            ans1=1.0+y*(0.183105e-2+y*(-0.3516396496e-4 +y*(0.2457520174e-5+y*(-0.240337019e-6))));
            ans2=0.04687499995+y*(-0.2002690873e-3 +y*(0.8449199096e-5+y*(-0.88228987e-6 +y*0.105787412e-6)));
            ans=Math.sqrt(0.636619772/ax)*(Math.cos(xx)*ans1-z*Math.sin(xx)*ans2);
            if (x < 0.0)
                ans = -ans;
        }
        return ans;
    }

}