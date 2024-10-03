package demo.EqBench.REVE.triangular.Eq;
public class newV {
	int triangle(int n) {
	  	int r=0;
		int s=0;
	  	if (n <= 0) {
			r = s;} 
		else {
			r = triangle(n - 1);}
	  	return r;
	}
	// private int g(int n, int s){
	//   	int r;
	//   	r = 0;
	//   	if (n <= 0) {
	// 		r = s;
	//   	} else {
	// 		r = g(n - 1, n + s);
	//   	}
	//   	return r;
	// }
	// int triangle(int n) {
	//   	int r;
	//   	r = g(n, 0);
	//   	return r;
	// }
}