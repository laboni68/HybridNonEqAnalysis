package demo.EqBench.REVE.barthe.Neq;
public class oldV {
	static int f(int n, int c) {
		int i = 0;
		int j = 0;
		int x = 0;
		while(i < n) {
		   j = 5 * i + c;
		   x = x + j;
		   if (false) {
			 j = 10;
		   }
		   i++;
		}
		return x;
	}
}