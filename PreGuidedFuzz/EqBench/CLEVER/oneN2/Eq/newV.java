package demo.EqBench.CLEVER.oneN2.Eq;
public class newV {
	static int client(int x){
		int x1;
		if (x > 11)//change
			x1= 11;
		else
			x1= x-1;//change
		if (x > x1)
			return x;
		else{
			if (x > 11)//change
				return 11;
			else
				return x-1;//change
		}
	}
}