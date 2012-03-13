import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LargeFileReader {
    private static final String FILE = "O:\\wiki\\extracted\\output-dict_it.wiki_it";
    private static final int LIMIT = 10000;
    private static final String FROM_STRING = null;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FILE));
        String l = null;
        int i = 0;
        boolean start = FROM_STRING == null;
        while (i < LIMIT && (l = reader.readLine()) != null) {
            if (!start) {
                if (FROM_STRING != null && l.contains(FROM_STRING)) {
                    start = true;
                }
            }
            if (start) {
                System.out.println(l);
                i++;
            }
        }
    }
}
