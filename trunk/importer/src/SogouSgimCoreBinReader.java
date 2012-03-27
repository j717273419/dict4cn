import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Sogou sgim_core.bin Reader
 * 
 * 
 * <pre>
 * 地址：
 * 0x0C：单词数量
 * ????：单词长度（byte），单词（编码：UTF-16LE）
 * 
 * For files like sgim_eng.bin etc., the implementation has to be littlely modified.
 * </pre>
 * 
 * @author keke
 */
public class SogouSgimCoreBinReader {
    public static void main(String[] args) throws IOException {
        String binFile = "D:\\sgim_core.bin";
        // String binFile = "D:\\sgim_eng.bin";

        // read scel into byte array
        FileChannel fChannel = new RandomAccessFile(binFile, "r").getChannel();
        ByteBuffer bb = ByteBuffer.allocate((int) fChannel.size());
        fChannel.read(bb);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.rewind();

        int words = bb.getInt(0xC);
        System.out.println("读入文件: " + binFile + "，单词：" + words);

        int i;
        int startPos = -1;
        while (bb.hasRemaining()) {
            i = bb.getInt();
            if (i == 0x554a0002) { // core, 6.1.0.6700
                // if (i == 0x00610002) { // eng, 6.1.0.6700
                startPos = bb.position() - 4;
                break;
            }
        }

        if (startPos > -1) {
            short s;
            int counter = 0;
            ByteBuffer buffer = ByteBuffer.allocate(Short.MAX_VALUE);
            System.out.println("单词起始位置：0x" + Integer.toHexString(startPos));
            bb.position(startPos);
            while (bb.hasRemaining() && words-- > 0) {
                s = bb.getShort();
                bb.get(buffer.array(), 0, s);
                counter++;
                // System.out.println(new String(buffer.array(), 0, s, "UTF-16LE"));
            }
            int endPos = bb.position();
            int diff = endPos - startPos;
            System.out.println("读出单词'" + binFile + "'：" + counter);
            System.out.println("单词结尾位置：0x" + Integer.toHexString(endPos));
            System.out.println("单词词典长度：0x" + Integer.toHexString(diff));
        }

        fChannel.close();
    }
}
