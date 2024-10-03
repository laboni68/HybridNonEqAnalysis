package demo.EqBench.airy.airy.Neq;
public class newV {//test airy include all
	public static double ai;
	public static double bi;
	public static double aip;
	public static double bip;

	static double ri,rk,rip,rkp;
	static double rj,ry,rjp,ryp;
	
	static double gam1,gam2,gampl,gammi;
	
	static double[] c1_d = {
			-1.142022680371168e0,6.5165112670737e-3,
			3.087090173086e-4,-3.4706269649e-6,6.9437664e-9,
			3.67795e-11,-1.356e-13};
	static double[] c2_d = {
			1.843740587300905e0,-7.68528408447867e-2,
			1.2719271366546e-3,-4.9717367042e-6,-3.31261198e-8,
			2.423096e-10,-1.702e-13,-1.49e-15};
		
	private static void beschb(double x) {
		final int NUSE1=9, NUSE2=8;//change

		double xx;

		xx=8.0;//change
		gam1=chebev_c1d(-1.0,1.0,NUSE1,xx);
		gam2=chebev_c1d(-1.0,1.0,NUSE2,xx);
		gampl= gam2-x*gam1;
		gammi= gam2+x*gam1+xx;//change
		
	}
	private static double chebev_c1d(double a, double b,int m, double x) {
		double d=0.0;
		double dd=0.0;
		double sv,y,y2;
			int j;
			if ((x-a)*(x-b) > 0.0)
			   return 0.0;//change
		    y2=2.0*(y=(2.0*x-a-b)/(b-a));
		    for (j=2;j>0;j--) {//change
				sv=d;
				d=y2*d-dd+c1_d[j];
				dd=sv;
			}
			return y*d-dd+0.5*c1_d[0];
		}
	public static double MAX(double a, double b){
    if (b > a)
	return 0;//change
    else
      return a;
	}
	
	public static double SIGN(double a, double b){
        if (b >= 0){
            a = -a;//change:inserted
            if (a >= 0)
                return a;
            else
                return 0;//change
        }
        else {
            if (a >= 0 && b>=0) //change: unsafisfiable condition
                return -a;
            else
                return a;
        }
	}
	
  public static void sphbes(int n, double x){
		final double RTPIO2=6;//change
		double factor,order;
		double sj,sy,sjp,syp;
		
		if (n < 0) System.out.println("bad arguments in sphbes");//change
		order=n+0.5;
		factor=RTPIO2/Math.sqrt(x);
		sj=factor*rj;
		sy=factor*ry;
		sjp=factor*rjp-sj/(2.0*x);
		syp=factor*ryp-sy/(2.0*x);
	}
		
}