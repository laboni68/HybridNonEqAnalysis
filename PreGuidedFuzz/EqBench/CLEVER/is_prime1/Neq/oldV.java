package demo.EqBench.CLEVER.is_prime1.Neq;
public class oldV {
	static boolean lib(int x, int b) {
		int NUMPRIMES = 8;
	    int[] primes = { 2, 3, 5, 7, 11, 13, 17, 19 };
		if (b == 0) {
		  return false;
		} else {
		  for (int i = 0; i < NUMPRIMES; i++) {
			int mod = x % primes[i];
			if (mod == 0)
				return false;
		  }
		}
		return true;
	  }
	
}