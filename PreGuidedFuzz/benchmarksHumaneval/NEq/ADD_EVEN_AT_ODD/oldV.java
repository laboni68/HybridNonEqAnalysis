package demo.benchmarksHumaneval.NEq.ADD_EVEN_AT_ODD;

/* Given a non-empty list of integers lst. add the even elements that are at odd indices..
Examples:
    add([4, 2, 6, 7]) ==> 2 */

public class oldV {
    public static int add_even_at_odd(int[] lst) {
        int sum = 0;
        for (int i = 0; i < lst.length; i += 1) {
            if (lst[i] % 2 == 0)
                sum += lst[i];
        }
        return sum;
    }
    public static void main(String[] args) {
        int[] lst = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            lst[i] = Integer.parseInt(args[i]);
        }
        System.out.println(add_even_at_odd(lst));
    }
}
