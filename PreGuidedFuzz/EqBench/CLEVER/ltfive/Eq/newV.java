package demo.EqBench.CLEVER.ltfive.Eq;
public class newV {
	static int client(int x){
		if (x < 0){
		x=(-x)*5;
		if (x < 0)
			return -(0/5);
		else
			return -(x/5);
		}else{
		x=(x+1)*5;
		if (x < 0)
			return (0/5) - 1;
		else
			return (x/5) - 1;
		}
	}
}