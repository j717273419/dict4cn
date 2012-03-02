import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Tries to find every libzip deflate stream in a test file
 * 
 * @author keke
 * 
 */
public class DeflateReader {
    private static final int[] DEFLATE_HEADERS = { 0x789c, 0x78da, 0x7801, 0x785e, 0x78da };
    private static final int[] DEFLATE_WITH_DICT_HEADERS = { 0x78bb, 0x78f9, 0x783f, 0x787d };
    static {
        Arrays.sort(DEFLATE_HEADERS);
        Arrays.sort(DEFLATE_WITH_DICT_HEADERS);
    }

    public static void main(String[] args) throws IOException, DataFormatException {
        String dflFile = "D:\\test_header.dat";
        String outputFilePrefix = dflFile + ".";
        String outputFileSuffix = ".raw";
        String outputFileFinal = dflFile + outputFileSuffix;

        // read deflate file into byte array
        ByteBuffer dataRawBytes = readBytes(dflFile);

        System.out.println("文件: " + dflFile);
        System.out.println("文件大小: " + dataRawBytes.limit() + " B (0x"
                + Integer.toHexString(dataRawBytes.limit()) + ")");

        List<String> outputFiles = new ArrayList<String>();
        while (dataRawBytes.position() < dataRawBytes.limit() - 2) {
            try {
                int position = dataRawBytes.position();
                String positionHex = "0x" + Integer.toHexString(position);
                int header = dataRawBytes.getShort() & 0xffff;                
                String headerHex = "0x" + Integer.toHexString(header);
                if (Arrays.binarySearch(DEFLATE_WITH_DICT_HEADERS, header) >= 0) {
                    System.out.println(positionHex + ": Deflate with dictionary header '" + headerHex
                            + "' found (perhaps).");
                }
                if (Arrays.binarySearch(DEFLATE_HEADERS, header) >= 0) {
                    String outputFile = outputFilePrefix + outputFiles.size() + "." + positionHex + outputFileSuffix;
                    System.out.println(positionHex + ": Deflate header '" + headerHex
                            + "' found. Trying to decompress stream to '" + outputFile + "'.");
                    ByteBuffer plainBytes = decompress(dataRawBytes, position, dataRawBytes.limit() - position);
                    System.out.println(positionHex + ": extracted " + plainBytes.limit() + " B (0x"
                            + Integer.toHexString(plainBytes.limit()) + ").");
                    writeFile(outputFile, plainBytes, false);
                    outputFiles.add(outputFile);
                }
            } catch (Throwable e) {
                System.out.println("Error while analyzing: " + e.toString());
            }
        }

        System.out.println("Writing merged output file: '" + outputFileFinal + "'");
        writeFile(outputFileFinal, ByteBuffer.wrap(new byte[0]), false);
        for (String outputFile : outputFiles) {
            writeFile(outputFileFinal, readBytes(outputFile), true);
        }

    }

    private static ByteBuffer readBytes(String dflFile) throws FileNotFoundException, IOException {
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(dflFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        return dataRawBytes;
    }

    private static void writeFile(String outputFile, ByteBuffer data, boolean append) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data.array());
        FileOutputStream out = new FileOutputStream(outputFile, append);
        writeInputStream(in, out);
        in.close();
        out.close();
    }

    private static ByteBuffer decompress(ByteBuffer data, int offset, int length) throws IOException {
        Inflater inflater = new Inflater();
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length),
                inflater, 1024 * 8);
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream(1024 * 8);
        writeInputStream(in, dataOut);
        in.close();
        data.position(offset + (int) inflater.getBytesRead());
        inflater.end();
        return ByteBuffer.wrap(dataOut.toByteArray());
    }

    private static void writeInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 8];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
}
