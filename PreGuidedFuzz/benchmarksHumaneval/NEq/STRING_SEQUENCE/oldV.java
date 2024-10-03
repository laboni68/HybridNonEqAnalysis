package demo.benchmarksHumaneval.NEq.STRING_SEQUENCE;

public class oldV {
    public static String string_sequence(int n) {
        String result = "";
        for (int i = 0; i <= n; i += 1){
            result += i + " ";
        }
        return result;
    }
}
