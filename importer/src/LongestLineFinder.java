import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LongestLineFinder {
    private static final String FILE = "C:\\usr\\kkdict\\out\\dicts\\wiki\\work\\output-dict_xtr-result.wiki";
    private static final double showQuantile = 0.8;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FILE));
        String l = null;
        int max = 0;
        while ((l = reader.readLine()) != null) {
            max = Math.max(l.length(), max);
        }
        System.out.println("找到最长行：" + max + "字符");
        reader.close();

        reader = new BufferedReader(new FileReader(FILE));
        while ((l = reader.readLine()) != null) {
            if (l.length() >= max * showQuantile) {
                System.out.println(l);
            }
        }
        reader.close();
    }
}
