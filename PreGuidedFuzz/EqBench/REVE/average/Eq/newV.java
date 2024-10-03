package demo.EqBench.REVE.average.Eq;
public class newV {
	static double average(int n, int a[]) {
		if (n <= 0) {
			return 0;
		}
		double sum = 0;
		for (int i = 0; (i < n); i++) {
			sum += (double)a[i] / n;
		}
		return sum;
	}
}