package demo.benchmarksHumaneval.NEq.CONCATENATE;

public class newV {
    public static String concatenate(String[] strings) {
        String result = "";
        for (String string : strings)
            result += string;
        return result;
    }
}
