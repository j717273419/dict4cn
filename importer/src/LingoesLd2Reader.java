import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Lingoes LD2/LDF File Reader
 * 
 * <pre>
 * Lingoes Format overview:
 * 
 * General Information:
 * - Dictionary data are stored in deflate streams.
 * - Index group information is stored in an index array in the LD2 file itself.
 * - Numbers are using little endian byte order.
 * - Version 2.5 uses UTF-16LE in its dictionary
 * - Version >2.5 uses UTF-8 ?
 * 
 * LD2 file schema:
 * - File Header
 * - File Description
 * - Additional Information (optional)
 * - Index Group (corresponds to definitions in dictionary) 
 * - Deflated Dictionary Streams
 * -- Index Data
 * --- Offsets of definitions
 * --- Offsets of translations
 * --- Flags
 * --- References to other translations
 * -- Definitions
 * -- Translations (xml)
 * 
 * </pre>
 * 
 * @author keke
 * 
 */
public class LingoesLd2Reader {
    public static void main(String[] args) throws IOException {
        // download from
        // https://skydrive.live.com/?cid=a10100d37adc7ad3&sc=documents&id=A10100D37ADC7AD3%211172#cid=A10100D37ADC7AD3&sc=documents
        String ld2File = "D:\\test.ld2";

        // read lingoes ld2 into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(ld2File, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        System.out.println("文件：" + ld2File);
        System.out.println("类型：" + new String(dataRawBytes.array(), 0, 4, "ASCII"));
        System.out.println("版本：" + dataRawBytes.getShort(0x18) + "." + dataRawBytes.getShort(0x1A));
        System.out.println("ID: 0x" + Long.toHexString(dataRawBytes.getLong(0x1C)));

        int offsetData = dataRawBytes.getInt(0x5C) + 0x60;
        if (dataRawBytes.limit() > offsetData) {
            System.out.println("简介地址：0x" + Integer.toHexString(offsetData));
            int type = dataRawBytes.getInt(offsetData);
            System.out.println("简介类型：0x" + Integer.toHexString(type));
            int offsetWithInfo = dataRawBytes.getInt(offsetData + 4) + offsetData + 12;
            if (type == 3) {
                // without additional information
                readDictionary(ld2File, dataRawBytes, offsetData, "UTF-16LE");
            } else if (dataRawBytes.limit() > offsetWithInfo - 0x1C) {
                readDictionary(ld2File, dataRawBytes, offsetWithInfo, "UTF-8");
            } else {
                System.err.println("文件不包含字典数据。（网上字典？）");
            }
        } else {
            System.err.println("文件不包含字典数据。（网上字典？）");
        }
    }

    private static void readDictionary(String ld2File, ByteBuffer dataRawBytes, int offsetWithIndex, String encoding)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {
        System.out.println("词典类型：0x" + Integer.toHexString(dataRawBytes.getInt(offsetWithIndex)));
        int limit = dataRawBytes.getInt(offsetWithIndex + 4) + offsetWithIndex + 8;
        int offsetIndex = offsetWithIndex + 0x1C;
        int offsetCompressedDataHeader = dataRawBytes.getInt(offsetWithIndex + 8) + offsetIndex;
        int inflatedWordsIndexLength = dataRawBytes.getInt(offsetWithIndex + 12);
        int inflatedWordsLength = dataRawBytes.getInt(offsetWithIndex + 16);
        int inflatedXmlLength = dataRawBytes.getInt(offsetWithIndex + 20);
        int definitions = (offsetCompressedDataHeader - offsetIndex) / 4;
        List<Integer> deflateStreams = new ArrayList<Integer>();
        dataRawBytes.position(offsetCompressedDataHeader + 8);
        int offset = dataRawBytes.getInt();
        while (offset + dataRawBytes.position() < limit) {
            offset = dataRawBytes.getInt();
            deflateStreams.add(Integer.valueOf(offset));
        }
        int offsetCompressedData = dataRawBytes.position();
        System.out.println("索引词组数目：" + definitions);
        System.out.println("索引地址/大小：0x" + Integer.toHexString(offsetIndex) + " / "
                + (offsetCompressedDataHeader - offsetIndex) + " B");
        System.out.println("压缩数据地址/大小：0x" + Integer.toHexString(offsetCompressedData) + " / "
                + (limit - offsetCompressedData) + " B");
        System.out.println("词组索引地址/大小（解压缩后）：0x0 / " + inflatedWordsIndexLength + " B");
        System.out.println("词组地址/大小（解压缩后）：0x" + Integer.toHexString(inflatedWordsIndexLength) + " / "
                + inflatedWordsLength + " B");
        System.out.println("XML地址/大小（解压缩后）：0x" + Integer.toHexString(inflatedWordsIndexLength + inflatedWordsLength)
                + " / " + inflatedXmlLength + " B");
        System.out.println("文件大小（解压缩后）：" + (inflatedWordsIndexLength + inflatedWordsLength + inflatedXmlLength) / 1024
                + " KB");
        String inflatedFile = ld2File + ".inflated";
        inflate(dataRawBytes, deflateStreams, inflatedFile);

        if (new File(inflatedFile).isFile()) {
            String indexFile = ld2File + ".idx";
            String extractedFile = ld2File + ".words";
            String extractedXmlFile = ld2File + ".xml";
            String extractedOutputFile = ld2File + ".output";

            dataRawBytes.position(offsetIndex);
            int[] idxArray = new int[definitions];
            for (int i = 0; i < definitions; i++) {
                idxArray[i] = dataRawBytes.getInt();
            }
            extract(inflatedFile, indexFile, extractedFile, extractedXmlFile, extractedOutputFile, idxArray,
                    inflatedWordsIndexLength, inflatedWordsIndexLength + inflatedWordsLength, encoding);
        }
    }

    private static void extract(String inflatedFile, String indexFile, String extractedWordsFile,
            String extractedXmlFile, String extractedOutputFile, int[] idxArray, int offsetWords, int offsetXml,
            String encoding) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        System.out.println("写入'" + extractedOutputFile + "'。。。");

        FileWriter indexWriter = new FileWriter(indexFile);
        FileWriter wordsWriter = new FileWriter(extractedWordsFile);
        FileWriter xmlWriter = new FileWriter(extractedXmlFile);
        FileWriter outputWriter = new FileWriter(extractedOutputFile);
        // read inflated data
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(inflatedFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        int dataLen = 4 + 4 + 2;
        int wordsTotal = offsetWords / dataLen - 1;
        String[] words = new String[wordsTotal];

        dataRawBytes.position(8);
        int[] wordIdxData = new int[6];
        int counter = 0;
        String xml;
        String word;
        for (int i = 0; i < wordsTotal; i++) {
            getIdxData(dataRawBytes, dataLen * i, wordIdxData);
            int lastWordPos = wordIdxData[0];
            int lastXmlPos = wordIdxData[1];
            int flags = wordIdxData[2];
            int refs = wordIdxData[3];
            int currentWordOffset = wordIdxData[4];
            int currenXmlOffset = wordIdxData[5];

            xml = new String(dataRawBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos, encoding);
            while (refs-- > 0) {
                int ref = dataRawBytes.getInt(offsetWords + lastWordPos);
                getIdxData(dataRawBytes, dataLen * ref, wordIdxData);
                lastXmlPos = wordIdxData[1];
                currenXmlOffset = wordIdxData[5];
                if (xml.isEmpty()) {
                    xml = new String(dataRawBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos,
                            encoding);
                } else {
                    xml = new String(dataRawBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos,
                            encoding) + ", " + xml;
                }
                lastWordPos += 4;
            }

            word = new String(dataRawBytes.array(), offsetWords + lastWordPos, currentWordOffset - lastWordPos,
                    encoding);
            words[i] = word;
            wordsWriter.write(word);
            wordsWriter.write("\n");

            xmlWriter.write(xml);
            xmlWriter.write("\n");

            if (xml.isEmpty()) {
                int ref = dataRawBytes.getInt(offsetWords + lastWordPos);
                System.out.println(counter + ". 0x" + Integer.toHexString(dataLen * i) + ": " + word + ", 0x"
                        + Integer.toHexString(offsetWords + lastWordPos) + ", 0x" + Integer.toHexString(flags) + ", "
                        + xml + ", 0x" + Integer.toHexString(ref));
                System.out.println("0x" + Integer.toHexString(offsetXml + lastXmlPos));
                break;
            }
            outputWriter.write(word);
            outputWriter.write("=");
            outputWriter.write(strip(xml));
            outputWriter.write("\n");
            counter++;
            lastXmlPos = currenXmlOffset;
            lastWordPos = currentWordOffset;
        }
        for (int i = 0; i < idxArray.length; i++) {
            int idx = idxArray[i];
            indexWriter.write(words[idx]);
            indexWriter.write(", ");
            indexWriter.write(String.valueOf(idx));
            indexWriter.write("\n");
        }
        indexWriter.close();
        wordsWriter.close();
        xmlWriter.close();
        outputWriter.close();
        System.out.println("成功读出" + counter + "组数据。");
    }

    private static String strip(String xml) {
        StringBuilder result = new StringBuilder();
        int open = xml.indexOf('<', 0);
        int end = 0;
        do {
            if (open - end > 1) {
                result.append(xml.substring(end + 1, open));
            }
            open = xml.indexOf('<', open + 1);
            end = xml.indexOf('>', end + 1);
        } while (open != -1 && end != -1);
        return result.toString();
    }

    private static void getIdxData(ByteBuffer dataRawBytes, int position, int[] wordIdxData) {
        dataRawBytes.position(position);
        wordIdxData[0] = dataRawBytes.getInt();
        wordIdxData[1] = dataRawBytes.getInt();
        wordIdxData[2] = dataRawBytes.get() & 0xff;
        wordIdxData[3] = dataRawBytes.get() & 0xff;
        wordIdxData[4] = dataRawBytes.getInt();
        wordIdxData[5] = dataRawBytes.getInt();
    }

    private static void inflate(ByteBuffer dataRawBytes, List<Integer> deflateStreams, String inflatedFile) {
        System.out.println("解压缩'" + deflateStreams.size() + "'个数据流至'" + inflatedFile + "'。。。");
        int startOffset = dataRawBytes.position();
        int offset = -1;
        int lastOffset = startOffset;
        boolean append = false;
        try {
            for (Integer offsetRelative : deflateStreams) {
                offset = startOffset + offsetRelative.intValue();
                decompress(inflatedFile, dataRawBytes, lastOffset, offset - lastOffset, append);
                append = true;
                lastOffset = offset;
            }
        } catch (Throwable e) {
            System.err.println("解压缩失败: 0x" + Integer.toHexString(offset) + ": " + e.toString());
        }
    }

    private static long decompress(String inflatedFile, ByteBuffer data, int offset, int length, boolean append)
            throws IOException {
        Inflater inflator = new Inflater();
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length),
                inflator, 1024 * 8);
        FileOutputStream out = new FileOutputStream(inflatedFile, append);
        writeInputStream(in, out);
        in.close();
        out.close();
        return inflator.getBytesRead();
    }

    private static void writeInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 8];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
}
