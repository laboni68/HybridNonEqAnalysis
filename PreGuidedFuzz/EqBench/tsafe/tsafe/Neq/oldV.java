package demo.EqBench.tsafe.tsafe.Neq;
public class oldV{
    public static double conflict (double psi1, double vA, double vC, double xC0, double yC0, double psiC, double bank_ang, double degToRad, double g ) { //degToRad and g are global vars
        double dmin = 999;
        double dmst = 2;
        double psiA = psi1 * degToRad;
        double signA = 1;
        double signC = 1;
        if (psiA < 0) {
          signA = -1;
        }
        double rA = Math.pow(vA, 2.0) / Math.tan(bank_ang*degToRad) / g;
        double rC = Math.pow(vC, 2.0) / Math.tan(bank_ang*degToRad) / g; 
        double t1 = Math.abs(psiA) * rA / vA;
        double dpsiC = signC * t1 * vC/rC; 
        double xA = signA*rA*(1-Math.cos(psiA));      
        double yA = rA*signA*Math.sin(psiA);
        double xC = xC0 + signC*rC* (Math.cos(psiC)-Math.cos(psiC+dpsiC));
        double yC = yC0 - signC*rC*(Math.sin(psiC)-Math.sin(psiC+dpsiC));                         
        double xd1 = xC - xA;
        double yd1 = yC - yA;                  
        double d = Math.sqrt(Math.pow(xd1, 2.0) + Math.pow(yd1, 2.0));
        double minsep =0;
        if (d < dmin) {
          dmin = d;
        } 
        if (dmin < dmst) {
          minsep = dmin;
        }
        else {
          minsep = dmst;
        }
        return minsep;
      } 
    public static double snippet (double x0, double y0, double gspeed, double x1, double y1, double x2, double y2, double dt) {
        double twoPi = Math.PI * 2;
        double deg = Math.PI / 180;
        double gacc = 32.0;
        double dx = x0 - x1;
        double dy = y0 - y1;
        if (dx == 0 && dy == 0)
            return 0.0;
         double instHdg = 90 * deg - Math.tan(dy/dx);
        if (instHdg < 0.)
            instHdg += twoPi;
        if (instHdg > 2 * Math.PI)
            instHdg += twoPi;
        dx = x1 - x2;
        dy = y1 - y2;
        if (dx == 0 && dy == 0)
            return 0.0;
        double instHdg0 = 90 * deg - Math.tan(dy/dx);
        if (instHdg0 < 0.)
            instHdg0 += 360 * deg;
        if (instHdg0 > 2 * Math.PI)
            instHdg0 -= 360 * deg;
        double hdg_diff = normAngle(instHdg - instHdg0);
        double phi = Math.tan((hdg_diff * gspeed)/(gacc * dt));
        return phi / deg;
    }
    private static double normAngle(double angle) {
        double twoPi = Math.PI * 2;
        if (angle < -Math.PI) {
                return angle + twoPi;
            }
        if (angle > Math.PI) {
                return angle - twoPi;
        }
        return angle;
      } 
}