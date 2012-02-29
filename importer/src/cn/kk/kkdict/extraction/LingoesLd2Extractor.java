package cn.kk.kkdict.extraction;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
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

import cn.kk.kkdict.utils.Helper;

public class LingoesLd2Extractor {
    public static final String IN_DIR = "X:\\kkdict\\dicts\\lingoes";
    public static final String OUT_DIR = "X:\\kkdict\\out\\lingoes";

    public static void main(String[] args) throws IOException {
        File directory = new File(IN_DIR);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".ld2");
                }
            });

            long total = 0;
            for (File f : files) {
                System.out.print("Extracting '" + f + " ... ");
                int counter = extractLd2ToFile(f);
                if (counter > 0) {
                    System.out.println(counter);
                    total += counter;
                }
            }

            System.out.println("\n=====================================");
            System.out.println("Total Completed: " + files.length + " Files");
            System.out.println("Total Words: " + total);
            System.out.println("=====================================");
        }
    }

    private static int extractLd2ToFile(File ld2File) throws IOException {
        Helper.precheck(ld2File.getAbsolutePath(), OUT_DIR);
        int counter = 0;

        // read lingoes ld2 into byte array
        ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(ld2File, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        int offsetData = dataRawBytes.getInt(0x5C) + 0x60;
        if (dataRawBytes.limit() > offsetData) {
            int type = dataRawBytes.getInt(offsetData);
            int offsetWithInfo = dataRawBytes.getInt(offsetData + 4) + offsetData + 12;
            if (type == 3) {
                counter = readDictionary(ld2File, dataRawBytes, offsetData, "UTF-16LE");
            } else if (dataRawBytes.limit() > offsetWithInfo + 0x1C) {
                counter = readDictionary(ld2File, dataRawBytes, offsetWithInfo, "UTF-8");
            } else {
                System.err.println("文件不包含字典数据。（网上字典？）");
            }
        } else {
            System.err.println("文件不包含字典数据。（网上字典？）");
        }

        return counter;
    }

    private static int readDictionary(File ld2File, ByteBuffer dataRawBytes, int offsetData, String encoding) throws IOException,
            FileNotFoundException, UnsupportedEncodingException {
        int counter;
        int limit = dataRawBytes.getInt(offsetData + 4) + offsetData + 8;
        int offsetIndex = offsetData + 0x1C;
        int offsetCompressedDataHeader = dataRawBytes.getInt(offsetData + 8) + offsetIndex;
        int inflatedWordsIndexLength = dataRawBytes.getInt(offsetData + 12);
        int inflatedWordsLength = dataRawBytes.getInt(offsetData + 16);
        List<Integer> deflateStreams = new ArrayList<Integer>();
        dataRawBytes.position(offsetCompressedDataHeader + 8);
        int offset = dataRawBytes.getInt();
        while (offset + dataRawBytes.position() < limit) {
            offset = dataRawBytes.getInt();
            deflateStreams.add(Integer.valueOf(offset));
        }
        ByteBuffer inflatedBytes = inflate(dataRawBytes, deflateStreams);

        String outputFile = OUT_DIR + File.separator + ld2File.getName() + ".out";
        counter = extract(inflatedBytes, inflatedWordsIndexLength, inflatedWordsIndexLength + inflatedWordsLength,
                outputFile, encoding);
        return counter;
    }

    private static int extract(ByteBuffer inflatedBytes, int offsetWords, int offsetXml, String outputFile, String encoding)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile), 8192000);
        inflatedBytes.order(ByteOrder.LITTLE_ENDIAN);

        int dataLen = 10;
        int wordsTotal = offsetWords / dataLen - 1;

        inflatedBytes.position(8);
        int[] wordIdxData = new int[6];
        int counter = 0;
        String xml;
        String word;
        for (int i = 0; i < wordsTotal; i++) {
            getIdxData(inflatedBytes, dataLen * i, wordIdxData);
            int lastWordPos = wordIdxData[0];
            int lastXmlPos = wordIdxData[1];
            int refs = wordIdxData[3];
            int currentWordOffset = wordIdxData[4];
            int currenXmlOffset = wordIdxData[5];

            xml = new String(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos, encoding);
            while (refs-- > 0) {
                int ref = inflatedBytes.getInt(offsetWords + lastWordPos);
                getIdxData(inflatedBytes, dataLen * ref, wordIdxData);
                lastXmlPos = wordIdxData[1];
                currenXmlOffset = wordIdxData[5];
                if (xml.isEmpty()) {
                    xml = new String(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos,
                            encoding);
                } else {
                    xml = new String(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos,
                            encoding) + Helper.SEP_LIST + xml;
                }
                lastWordPos += 4;
            }

            word = new String(inflatedBytes.array(), offsetWords + lastWordPos, currentWordOffset - lastWordPos,
                    encoding);

            outputWriter.write(word);
            outputWriter.write(Helper.SEP_DEF);
            outputWriter.write(strip(xml));
            outputWriter.write(Helper.SEP_NEWLINE);
            counter++;
            lastXmlPos = currenXmlOffset;
            lastWordPos = currentWordOffset;
        }
        outputWriter.close();
        return counter;
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

    private static ByteBuffer inflate(ByteBuffer dataRawBytes, List<Integer> deflateStreams) throws IOException {
        int startOffset = dataRawBytes.position();
        int offset = -1;
        int lastOffset = startOffset;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Integer offsetRelative : deflateStreams) {
            offset = startOffset + offsetRelative.intValue();
            decompress(out, dataRawBytes, lastOffset, offset - lastOffset);
            lastOffset = offset;
        }
        return ByteBuffer.wrap(out.toByteArray());
    }

    private static long decompress(ByteArrayOutputStream out, ByteBuffer data, int offset, int length)
            throws IOException {
        Inflater inflator = new Inflater();
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length),
                inflator, 1024 * 8);
        writeInputStream(in, out);
        in.close();
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
