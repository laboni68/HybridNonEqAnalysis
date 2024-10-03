package demo.EqBench.REVE.digits10.Eq;
public class oldV {
	static int f(int n) {
		int result = 1;
		n = n/10;
		while (n > 0) {
		  result++;
		  n = n / 10;
		  if (n > 0) {
			result++;
			n = n / 10;
			if (n > 0) {
			  result++;
			  n = n / 10;
			  if (n > 0) {
				result++;
				n = n / 10;
			  }
			}
		  }
		}
		return result;
	  }
}