package demo.EqBench.CLEVER.pos.Eq;
public class newV {
	static int client(int x){
		if (x > 0) {
			return -lib(-x);
		}else{
			return lib(x);
		}
	}
	private static int lib(int x) {
		if (x < 0){
			return -x;
		}else{
			return x;}
	}
	
}