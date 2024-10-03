package demo.EqBench.REVE.barthe.Neq;
public class newV {
	static int f(int n, int c) {
		int i = 0;
		int j = c;
		int x = 0;
		while(i < n) {
		   x = x + j;
		   j = j + 5;
		   if (i == 10) {
			 j = 10;
		   }
		   i++;
		}
		return x;
	}
}