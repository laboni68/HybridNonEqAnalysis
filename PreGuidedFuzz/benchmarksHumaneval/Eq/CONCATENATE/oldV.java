package demo.benchmarksHumaneval.NEq.CONCATENATE;

public class oldV {
    public static String concatenate(String[] strings) {
        String result = null;
        for (String string : strings)
            result += string;
        return result;
    }
}
