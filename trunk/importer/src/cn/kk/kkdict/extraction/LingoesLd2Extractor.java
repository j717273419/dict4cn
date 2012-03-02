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
import java.util.regex.Pattern;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import cn.kk.kkdict.utils.Helper;

public class LingoesLd2Extractor {
    private static final String[] AVAIL_ENCODINGS = { "UTF-8", "UTF-16LE", "UTF-16BE" };
    private static final byte[] TRANSFER_BYTES = new byte[8192000];
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
                counter = readDictionary(ld2File, dataRawBytes, offsetData);
            } else if (dataRawBytes.limit() > offsetWithInfo + 0x1C) {
                counter = readDictionary(ld2File, dataRawBytes, offsetWithInfo);
            } else {
                System.err.println("文件不包含字典数据。网上字典？");
            }
        } else {
            System.err.println("文件不包含字典数据。网上字典？");
        }

        return counter;
    }

    private static int readDictionary(File ld2File, ByteBuffer dataRawBytes, int offsetData) throws IOException,
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
                outputFile);
        return counter;
    }

    private static int extract(ByteBuffer inflatedBytes, int offsetDefs, int offsetXml, String outputFile)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile), 8192000);
        inflatedBytes.order(ByteOrder.LITTLE_ENDIAN);

        final int dataLen = 10;
        final int defTotal = offsetDefs / dataLen - 1;

        int[] idxData = new int[6];
        String[] defData = new String[2];

        final String[] encodings = detectEncodings(inflatedBytes, offsetDefs, offsetXml, defTotal, dataLen, idxData,
                defData);

        inflatedBytes.position(8);
        int counter = 0;
        int failCounter = 0;
        final String defEncoding = encodings[0];
        final String xmlEncoding = encodings[1];
        for (int i = 0; i < defTotal; i++) {
            readDefinitionData(inflatedBytes, offsetDefs, offsetXml, dataLen, defEncoding, xmlEncoding, idxData,
                    defData, i);

            if (defData[0].trim().isEmpty() || defData[1].trim().isEmpty()) {
                failCounter++;
            }
            if (failCounter > defTotal * 0.01) {
                System.err.println("??");
                System.out.println(defData[0] + " = " + defData[1]);
                System.exit(1);
            }
            outputWriter.write(defData[0]);
            outputWriter.write(Helper.SEP_DEF);
            outputWriter.write(defData[1]);
            outputWriter.write(Helper.SEP_NEWLINE);
            counter++;
        }
        outputWriter.close();
        return counter;
    }

    private static String[] detectEncodings(ByteBuffer inflatedBytes, int offsetWords, int offsetXml, int defTotal,
            int dataLen, int[] idxData, String[] defData) throws UnsupportedEncodingException {
        int tests = Math.min(defTotal, 10);
        int defEnc = 0;
        int xmlEnc = 0;
        Pattern p = Pattern.compile("^.*[\\x00-\\x1f].*$");
        for (int i = 0; i < tests; i++) {
            readDefinitionData(inflatedBytes, offsetWords, offsetXml, dataLen, AVAIL_ENCODINGS[defEnc],
                    AVAIL_ENCODINGS[xmlEnc], idxData, defData, i);
            if (p.matcher(defData[0]).matches()) {
                if (defEnc < AVAIL_ENCODINGS.length - 1) {
                    defEnc++;
                } else {
                    System.err.println("err def");
                }
                i = 0;
            }
            if (p.matcher(defData[1]).matches()) {
                if (xmlEnc < AVAIL_ENCODINGS.length - 1) {
                    xmlEnc++;
                } else {
                    System.err.println("err xml");
                }
                i = 0;
            }
        }
        System.out.print(AVAIL_ENCODINGS[defEnc] + " / " + AVAIL_ENCODINGS[xmlEnc] + ", ");
        return new String[] { AVAIL_ENCODINGS[defEnc], AVAIL_ENCODINGS[xmlEnc] };
    }

    private static void readDefinitionData(final ByteBuffer inflatedBytes, final int offsetWords, final int offsetXml,
            final int dataLen, final String wordEncoding, final String xmlEncoding, final int[] wordIdxData,
            final String[] wordData, final int idx) throws UnsupportedEncodingException {
        getIdxData(inflatedBytes, dataLen * idx, wordIdxData);
        int lastWordPos = wordIdxData[0];
        int lastXmlPos = wordIdxData[1];
        int refs = wordIdxData[3];
        int currentWordOffset = wordIdxData[4];
        int currenXmlOffset = wordIdxData[5];
        String xml = strip(new String(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos,
                xmlEncoding));
        while (refs-- > 0) {
            int ref = inflatedBytes.getInt(offsetWords + lastWordPos);
            getIdxData(inflatedBytes, dataLen * ref, wordIdxData);
            lastXmlPos = wordIdxData[1];
            currenXmlOffset = wordIdxData[5];
            if (xml.isEmpty()) {
                xml = strip(new String(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos,
                        xmlEncoding));
            } else {
                xml = strip(new String(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos,
                        xmlEncoding)) + Helper.SEP_LIST + xml;
            }
            lastWordPos += 4;
        }
        wordData[1] = xml;

        String word = new String(inflatedBytes.array(), offsetWords + lastWordPos, currentWordOffset - lastWordPos,
                wordEncoding);
        wordData[0] = word;
    }

    private static String strip(String xml) {
        int open = 0;
        int end = 0;
        if ((open = xml.indexOf("<![CDATA[")) != -1) {
            if ((end = xml.indexOf("]]>", open)) != -1) {
                return xml.substring(open + "<![CDATA[".length(), end).replace('\t', ' ').replace('\n', ' ')
                        .replace('\u001e', ' ').replace('\u001f', ' ');
            }
        } else if ((open = xml.indexOf("<Ô")) != -1) {
            if ((end = xml.indexOf("</Ô", open)) != -1) {
                open = xml.indexOf(">", open + 1);
                return xml.substring(open + 1, end).replace('\t', ' ').replace('\n', ' ').replace('\u001e', ' ')
                        .replace('\u001f', ' ');
            }
        } else {
            StringBuilder sb = new StringBuilder();
            end = 0;
            open = xml.indexOf('<');
            do {
                if (open - end > 1) {
                    sb.append(xml.substring(end + 1, open));
                }
                open = xml.indexOf('<', open + 1);
                end = xml.indexOf('>', end + 1);
            } while (open != -1 && end != -1);
            return sb.toString().replace('\t', ' ').replace('\n', ' ').replace('\u001e', ' ').replace('\u001f', ' ');
        }
        return Helper.EMPTY_STRING;
    }

    private static final void getIdxData(final ByteBuffer dataRawBytes, final int position, final int[] wordIdxData) {
        dataRawBytes.position(position);
        wordIdxData[0] = dataRawBytes.getInt();
        wordIdxData[1] = dataRawBytes.getInt();
        wordIdxData[2] = dataRawBytes.get() & 0xff;
        wordIdxData[3] = dataRawBytes.get() & 0xff;
        wordIdxData[4] = dataRawBytes.getInt();
        wordIdxData[5] = dataRawBytes.getInt();
    }

    private static final ByteBuffer inflate(final ByteBuffer dataRawBytes, final List<Integer> deflateStreams)
            throws IOException {
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

    private static final long decompress(final ByteArrayOutputStream out, final ByteBuffer data, final int offset,
            final int length) throws IOException {
        Inflater inflator = new Inflater();
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length),
                inflator, 8192000);
        writeInputStream(in, out);
        long bytesRead = inflator.getBytesRead();
        inflator.end();
        in.close();
        return bytesRead;
    }

    private static final void writeInputStream(final InputStream in, final OutputStream out) throws IOException {
        int len;
        while ((len = in.read(TRANSFER_BYTES)) > 0) {
            out.write(TRANSFER_BYTES, 0, len);
        }
    }

}
