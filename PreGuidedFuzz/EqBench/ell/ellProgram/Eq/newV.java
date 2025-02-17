package demo.EqBench.ell.ellProgram.Eq;
public class newV{
  public static double snippet (double phi, double ak) {
    double cc =0;
    double q =0;
    double s=0;
    s=Math.sin(phi);
    cc=SQR(Math.cos(phi));
    double one = 1.0;//change
    q=(one-s*ak)*(one+s*ak);//change
    double ans = Math.sin(phi)*(rf(cc,q,one)-(SQR(s*ak))*rd(cc,q,one)/3.0);//change
    return ans ;
  }
  public static double ellpi (double phi, double en, double ak){
    double cc =0;
    double enss=0;
    double q=0;
    double s=0;
    s=Math.sin(phi);
    enss=en*s*s;
    cc=SQR(Math.cos(phi));
    q=1.0+((s*ak)*(s*ak));//change
    return s*(rf(cc,q,1.0)-enss*rj(cc,q,1.0,1.0+enss)/3.0);
  }
  public static double SQR(double a) {
    return a*a;
  }

  public static double SIGN(double a, double b){
    return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
  }

  public static double MAX(double a, double b){
    return b > a ? (b) : (a);
  }

  public static double MIN(double a, double b){
    return b < a ? (b) : (a);
  }
  private static double rf(double x, double y, double z){
    double ERRTOL=0.0025;
    double TINY=1.5e-38;
    double BIG=3.0e37;
    double THIRD=1.0/3.0;
    double C1=1.0/24.0;
    double C2=0.1;
    double C3=3.0/44.0;
    double C4=1.0/14.0;
    double alamb= 0;
    double ave= 0;
    double delx= 0;
    double dely= 0;
    double delz= 0;
    double sqrtx= 0;
    double sqrty= 0;
    double sqrtz= 0;
    double xt= 0;
    double yt= 0;
    double zt= 0;
    double e2= 0;
    double e3= 0;
    if (MIN(MIN(x,y),z) < 0.0 || MIN(MIN(x+y,x+z),y+z) < TINY || MAX(MAX(x,y),z) > BIG)
       return -10000;
    xt=x;
    yt=y;
    zt=z;
    do {
      sqrtx=Math.sqrt(xt);
      sqrty=Math.sqrt(yt);
      sqrtz=Math.sqrt(zt);
      alamb=sqrtx*(sqrty+sqrtz)+sqrty*sqrtz;
      xt=0.25*(xt+alamb);
      yt=0.25*(yt+alamb);
      zt=0.25*(zt+alamb);
      ave=THIRD*(xt+yt+zt);
      delx=(ave-xt)/ave;
      dely=(ave-yt)/ave;
      delz=(ave-zt)/ave;
    } while (MAX(MAX(Math.abs(delx),Math.abs(dely)),Math.abs(delz)) > ERRTOL);
    e2=delx*dely-delz*delz;
    e3=delx*dely*delz;
    double res = (1.0+(C1*e2-C2-C3*e3)*e2+C4*e3);//chnage
    return res;//change
  }

  private static double rd(double x, double y, double z){
    double ERRTOL=0.0015;
    double TINY=1.0e-25;
    double BIG=4.5e21;
    double C1=3.0/14.0;
    double C2=1.0/6.0;
    double C3=9.0/22.0;
    double C4=3.0/26.0;
    double C5=0.25*C3;
    double C6=1.5*C4;
    double alamb = 0;
    double three = 3.0;
    double ave= 0;
    double delx= 0;
    double dely= 0;
    double delz= 0;
    double ea= 0;
    double eb= 0;
    double ec= 0;
    double ed= 0;
    double ee= 0;
    double fac= 0;
    double sqrtx= 0;
    double sqrty= 0;
    double sqrtz= 0;
    double sum= 0;
    double xt= 0;
    double yt= 0;
    double zt= 0;
    if (CheckCond(x,y,z, TINY, BIG))//change
      return -1000;
    xt=x;
    yt=y;
    zt=z;
    sum=0.0;
    fac=1.0;
    do {
      sqrtx=Math.sqrt(xt);
      sqrty=Math.sqrt(yt);
      sqrtz=Math.sqrt(zt);
      alamb=sqrtx*(sqrty+sqrtz)+sqrty*sqrtz;
      sum += fac/(sqrtz*(zt+alamb));
      fac=0.25*fac;
      xt=0.25*(xt+alamb);
      yt=0.25*(yt+alamb);
      zt=0.25*(zt+alamb);
      ave=0.2*(xt+yt+three*zt);
      delx=(ave-xt)/ave;
      dely=(ave-yt)/ave;
      delz=(ave-zt)/ave;
    } while (MAX(MAX(Math.abs(delx),Math.abs(dely)),Math.abs(delz)) > ERRTOL);
    ea=delx*dely;
    eb=delz*delz;
    ec=ea-eb;
    ed=ea-6.0*2*eb;
    ee=ed+ec+ec;
    return 3.0*sum+fac*(1.0+ed*(-C1+C5*ed-C6*delz*ee) +delz*(C2*ee+delz*(-C3*ec+delz*C4*ea)))/(ave*Math.sqrt(ave));
  }
  public static boolean CheckCond(double x, double y, double z, double TINY, double BIG){
    return MIN(x,y) < 0.0 || MIN(x+y,z) < TINY || MAX(MAX(x,y),z) > BIG;
  }
  public static double rj (double x, double y, double z, double p) {
    double ERRTOL=0.0015;
    double TINY=2.5e-13;
    double BIG=9.0e11;
    double C1=3.0/14.0;
    double C2=1.0/3.0;
    double C3=3.0/22.0;
    double C4=3.0/26.0;
    double C5=0.75*C3;
    double C6=1.5*C4;
    double C7=0.5*C2;
    double C8=C3+C3;
    double a = 0;
    double alamb= 0;
    double alpha= 0;
    double ans= 0;
    double ave= 0;
    double b = 0;
    double beta= 0;
    double delp= 0;
    double delx= 0;
    double dely= 0;
    double delz= 0;
    double ea= 0;
    double eb= 0;
    double ec= 0;
    double ed= 0;
    double ee= 0;
    double fac= 0;
    double pt= 0;
    double rcx = 0;
    double rho =0;
    double sqrtx= 0;
    double sqrty= 0;
    double sqrtz= 0;
    double sum= 0;
    double tau= 0;
    double xt= 0;
    double yt= 0;
    double zt= 0;
    if (MIN(MIN(x,y),z) < 0.0 || MIN(MIN(x+y,x+z),MIN(y+z,Math.abs(p))) < TINY || MAX(MAX(x,y),MAX(z,Math.abs(p))) > BIG)
          return -1000;
    sum=0.0;
    fac=1.0;
    if (p > 0.0) {
      xt=x;
      yt=y;
      zt=z;
      pt=p;
    } else {
      xt=MIN(MIN(x,y),z);
      zt=MAX(MAX(x,y),z);
      yt=x+y+z-xt-zt;
      b=((zt-yt)*(yt-xt))/(yt-p);//change
      pt=yt+b;
      rho=xt*zt/yt;
      tau=p*pt/yt;
      rcx=rc(rho,tau);
    }
    do {
      sqrtx=Math.sqrt(xt);
      sqrty=Math.sqrt(yt);
      sqrtz=Math.sqrt(zt);
      alamb=sqrtx*(sqrty+sqrtz)+sqrty*sqrtz;
      alpha=SQR(pt*(sqrtx+sqrty+sqrtz)+sqrtx*sqrty*sqrtz);
      beta=pt*SQR(pt+alamb);
      sum += fac*rc(alpha,beta);
      fac=0.25*fac;
      xt=0.25*(xt+alamb);
      yt=0.25*(yt+alamb);
      zt=0.25*(zt+alamb);
      pt=0.25*(pt+alamb);
      ave=0.2*(xt+yt+zt+pt+pt);
      delx=(ave-xt)/ave;
      dely=(ave-yt)/ave;
      delz=(ave-zt)/ave;
      delp=(ave-pt)/ave;
    } while (MAX(MAX(Math.abs(delx),Math.abs(dely)),MAX(Math.abs(delz),Math.abs(delp))) > ERRTOL);
    ea=delx*(dely+delz)+dely*delz;
    eb=delx*dely*delz;
    ec=delp*delp;
    ed=ea-3.0*ec;
    ee=eb+2.0*delp*(ea-ec);
    ans=3.0*sum+fac*(1.0+ed*(-C1+C5*ed-C6*ee)+eb*(C7+delp*(-C8+delp*C4))  +delp*ea*(C2-delp*C3)-C2*delp*ec)/(ave*Math.sqrt(ave));
    if (p <= 0.0){
      ans=a*(b*ans+3.0*(rcx-rf(xt,yt,zt)));
    }
    return ans;
  }
  public static double rc (double x, double y) {
    double ERRTOL=0.0012;
    double TINY=1.69e-38;
    double SQRTNY=1.3e-19;
    double BIG=3.0e37;
    double TNBG=TINY*BIG;
    double COMP1=2.236/SQRTNY;
    double COMP2=TNBG*TNBG/25.0;
    double THIRD=1.0/3.0;
    double C1=0.32;
    double C2=1.0/7.0;
    double C3=0.375;
    double C4=9.0/22.0;
    double alamb =0 ;
    double ave=0;
    double s=0;
    double w=0;
    double xt=0;
    double yt=0;
    boolean condition = x < 0.0 || y == 0.0 ; //change
    if (condition || (x+Math.abs(y)) < TINY || (x+Math.abs(y)) > BIG)//change
      return -10000;
    if (y > 0.0) {
      xt+=x;
      yt+=y;
      w+=1.0;
    } else {
      xt+=x-y;
      yt+= -y;
      w+=Math.sqrt(x)/Math.sqrt(xt);
    }
    do {
      alamb*=2.0*Math.sqrt(xt)*Math.sqrt(yt)+yt;
      xt=0.25*(xt+alamb);
      yt=0.25*(yt+alamb);
      ave+=THIRD*(xt*yt*yt);
      s=(yt-ave)/ave;
    } while (Math.abs(s) > ERRTOL);
    return w*(1.0+s*s*(C1+s*(C2+s*(C3+s*C4))));
  }
}