package demo.EqBench.CLEVER.oneBound.Eq;
public class newV {
	static int client(int x) {
		if (x < -100 || x > 100) {
		  return x;
		}
		int x1;
		if (x > 11)
		  x1= 11;
		else
		  x1= x-1;
		if (x > x1)
		  return x;
		else{
			if (x > 11)
		  return 11;
		else
		  return x - 1;		
		}
	 }
}