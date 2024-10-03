package demo.EqBench.REVE.addhorn.Neq;
public class oldV {
	static int f(int i, int j)
	{
	  int r;
	  r = 0;
	  if (i == 0) {
		r = j;
	  } else {
		if (false) {
			r = j + 1;
		  } else { 
			if (false) {
			r = j;
		  } else {
			r = f(i - 1, j + 1);
		  }}
	  }
	  return r;
	}
}