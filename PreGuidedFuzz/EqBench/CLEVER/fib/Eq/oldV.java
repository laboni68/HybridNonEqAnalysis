package demo.EqBench.CLEVER.fib.Eq;
public class oldV {
	public static int fib(int x){
		if(x<5){
			return lib(x);
		}else{
			return 0;
		}
	}
	private static int lib(int n){
		if(n == 1){
			return 1;
		}else if(n < 1){
			return 0;
		}else{
			return lib(n - 1) + lib(n - 2);
		}
	}
}