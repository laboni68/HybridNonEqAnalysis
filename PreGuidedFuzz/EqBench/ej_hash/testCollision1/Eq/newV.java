package demo.EqBench.ej_hash.testCollision1.Eq;
public class newV{
    private final int x;
    private final long y;
    private final int z;
    public newV(int x, long y, int z) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
    }
    @Override
    public int hashCode() {
	    int h = x;
	    h = h * 31 + (int) (y ^ (y >> 32));
	    h = h * 31 + z;
	    return h;
    }
    public static void testCollision1(int x1, long y1, int z1,int x2, long y2, int z2) {
	    newV o1 = new newV(x1, y1, z1);
	    newV o2 = new newV(x2, y2, z2);
	    if (o1.hashCode() == o2.hashCode()) {
          x1=x2;//change
	        System.out.println("Solved hash collision 1");
	    }
    }
}