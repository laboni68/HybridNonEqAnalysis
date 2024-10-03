package demo.benchmarksHumaneval.NEq.GREATEST_COMMON_DIVISOR;

public class oldV {
    public static int greatest_common_divisor(int a, int b){
        while (b > 0) {
            a = b;
            b = a % b;
        }
        return a;
    }
}
