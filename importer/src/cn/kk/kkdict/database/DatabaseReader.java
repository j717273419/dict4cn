package cn.kk.kkdict.database;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.CompressHelper;
import cn.kk.kkdict.utils.Helper;

public class DatabaseReader
{
  public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_SUMMARIZED_DICTS.getPath(Source.NULL) + "/";

  public static final String OUT_DATA = DatabaseReader.OUT_DIR + "dict2cn_data.txt";

  public static final String OUT_DATA_XZ = DatabaseReader.OUT_DIR + "dict2cn_data.xz";

  public static final String OUT_INDEX = DatabaseReader.OUT_DIR + "dict2cn_idx.txt";

  public static final String OUT_INDEX_XZ = DatabaseReader.OUT_DIR + "dict2cn_idx.xz";

  public static final String OUT_CACHED = DatabaseReader.OUT_DIR + "dict2cn_cached.txt";

  public static final String OUT_CACHED_XZ = DatabaseReader.OUT_DIR + "dict2cn_cached.xz";

  public static final String OUT_HEADER = DatabaseReader.OUT_DIR + "dict2cn_header.txt";

  public static final String OUT_HEADER_XZ = DatabaseReader.OUT_DIR + "dict2cn_header.xz";

  public static final String OUT_DXZ = DatabaseReader.OUT_DIR + "dict2cn.dxz";

  final int[] defsCount;

  private int totalCount;

  private static final int LANGUAGES_SIZE = 256;


  public DatabaseReader()
  {
    this.defsCount = new int[Language.values().length + 1];
  }


  /**
   * @param args
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, IOException
  {
    DatabaseReader dxzCreator = new DatabaseReader();
    dxzCreator.write();
    dxzCreator.check();
  }


  private void check() throws IOException
  {
    System.out.println("checking ...");
    int headerLen;
    try (DataInputStream in = new DataInputStream(new FileInputStream(OUT_DXZ)))
    {
      headerLen = in.readUnsignedShort();
    }

    int total;
    int headerOffset = 2;
    int cachedLen;
    int idxLen;
    int dataLen;

    try (DataInputStream inHeader =
        new DataInputStream(CompressHelper.getDecompressedInputStream(OUT_DXZ, headerOffset, headerLen)))
    {
      total = inHeader.readInt();
      if (total != totalCount)
      {
        System.err.println("total: " + total + " (" + totalCount + ")");
      } else
      {
        System.out.println("total: " + total);
      }
      cachedLen = inHeader.readUnsignedShort();
      idxLen = inHeader.readInt();
      dataLen = inHeader.readInt();
      System.out.println("cached: " + cachedLen);
      System.out.println("index: " + idxLen);
      System.out.println("data: " + dataLen);
    }

    int cachedOffset = headerOffset + headerLen;
    int idxOffset = cachedOffset + cachedLen;
    int dataOffset = idxOffset + idxLen;
    try (InputStream inCached = CompressHelper.getDecompressedInputStream(OUT_DXZ, cachedOffset, cachedLen);
        InputStream in = new FileInputStream(OUT_CACHED);)
    {
      if (Helper.equals(in, inCached))
      {
        System.out.println("cached ok");
      } else
      {
        System.err.println("cached invalid!");
      }
    }

    try (InputStream inIndex = CompressHelper.getDecompressedInputStream(OUT_DXZ, idxOffset, idxLen);
        InputStream in = new FileInputStream(OUT_INDEX);)
    {
      if (Helper.equals(in, inIndex))
      {
        System.out.println("index ok");
      } else
      {
        System.err.println("index invalid!");
      }
    }

    try (InputStream inData = CompressHelper.getDecompressedInputStream(OUT_DXZ, dataOffset, dataLen);
        InputStream in = new FileInputStream(OUT_DATA);)
    {
      if (Helper.equals(in, inData))
      {
        System.out.println("data ok");
      } else
      {
        System.err.println("data invalid!");
      }
    }
  }


  private void write() throws ClassNotFoundException, FileNotFoundException, IOException
  {
    String url = "jdbc:mysql://localhost:3306/dict2go?autoReconnect=true";
    String driver = "com.mysql.jdbc.Driver";
    Class.forName(driver);
    System.out.println("data: " + DatabaseReader.OUT_DATA);
    System.out.println("index: " + DatabaseReader.OUT_INDEX);
    try (BufferedOutputStream outData =
        new BufferedOutputStream(new FileOutputStream(DatabaseReader.OUT_DATA), Helper.BUFFER_SIZE);
        BufferedOutputStream outIndex =
            new BufferedOutputStream(new FileOutputStream(DatabaseReader.OUT_INDEX), Helper.BUFFER_SIZE);)
    {
      try (Connection conn = DriverManager.getConnection(url, "root", "pada");)
      {
        conn.setAutoCommit(false);
        this.totalCount = DatabaseReader.getCount(conn);
        this.init();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(DatabaseReader.query);)
        {
          while (rs.next())
          {
            int i = 1;
            long trlId = rs.getInt(i++);
            String srcKeyHex = rs.getString(i++);
            short srcLng = rs.getShort(i++);
            short tgtLng = rs.getShort(i++);
            String srcVal = rs.getString(i++);
            String tgtVal = rs.getString(i++);
            byte srcGender = rs.getByte(i++);
            byte srcCategory = rs.getByte(i++);
            byte srcType = rs.getByte(i++);
            byte srcUsage = rs.getByte(i++);
            this.writeTranslation(outData, outIndex, new Translation(trlId, srcKeyHex, srcLng, tgtLng, srcVal, tgtVal,
                srcGender, srcCategory, srcType, srcUsage));
          }
        } catch (SQLException e)
        {
          e.printStackTrace();
        }
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    System.out.println("cached: " + DatabaseReader.OUT_CACHED);

    final ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
    try (RandomAccessFile file = new RandomAccessFile(DatabaseReader.OUT_INDEX, "r");
        final FileChannel fChannel = file.getChannel();)
    {
      fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
    }
    final ByteBuffer idxRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
    final byte[] keyBytes = new byte[SuperIndexGenerator.LEN_SRC_KEY];
    final byte[] keyBytesTest = new byte[SuperIndexGenerator.LEN_SRC_KEY];
    int cachedSize = 0;
    try (BufferedOutputStream outCached =
        new BufferedOutputStream(new FileOutputStream(DatabaseReader.OUT_CACHED), Helper.BUFFER_SIZE);)
    {
      int pointer = 0;
      final int cacheStep = this.totalCount / DatabaseReader.TOTAL_SIDX_CACHE_LENGTH;
      for (int i = 0; i < this.totalCount; i += cacheStep)
      {
        pointer = i;
        final int pos = i * DatabaseReader.CACHED_BYTES;
        idxRawBytes.position(pos);
        idxRawBytes.get(keyBytes);
        for (int j = i - 1; j >= 0; j--)
        {
          final int posTest = j * DatabaseReader.CACHED_BYTES;
          idxRawBytes.position(posTest);
          idxRawBytes.get(keyBytesTest);

          if (!Arrays.equals(keyBytes, keyBytesTest))
          {
            break;
          } else
          {
            pointer = j;
          }
        }
        if (cachedSize < DatabaseReader.TOTAL_SIDX_CACHE_LENGTH)
        {
          DatabaseReader.writeCachedIndex(pointer, outCached, keyBytes);
          cachedSize++;
        } else
        {
          break;
        }
      }
      System.out.println("cached: " + cachedSize);
    }

    long idxCompressedLen = DatabaseReader.compressIndexBlock();
    long dataCompressedLen = DatabaseReader.compressDataBlock();
    long cachedCompressedLen = DatabaseReader.compressCachedBlock();
    this.writeHeader(cachedCompressedLen, idxCompressedLen, dataCompressedLen);
    long headerCompressedLen = DatabaseReader.compressHeaderBlock();

    DatabaseReader.pack(headerCompressedLen);
  }


  private static int getCount(Connection conn) throws SQLException
  {
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(DatabaseReader.queryCount);)
    {
      rs.next();
      return rs.getInt(1);
    }
  }


  private void writeTranslation(BufferedOutputStream outData, BufferedOutputStream outIndex, Translation trl)
      throws IOException
  {
    // System.out.println(trl);

    this.defsCount[trl.getSrcLng()]++;

    final int dataOffset = this.writeData(outData, trl);
    this.writeIndex(outIndex, (short) dataOffset, trl);

  }


  private static void writeCachedIndex(int pointer, BufferedOutputStream outCachedIndex, byte[] keyBytes)
      throws IOException
  {
    outCachedIndex.write(keyBytes);
    outCachedIndex.write(ArrayHelper.toBytes(pointer));
  }

  private static final String query =
      "SELECT trl_id, HEX(src_key), src_lng, tgt_lng, src_val, tgt_val, src_gen, src_cat, src_typ, src_use FROM translation ORDER BY src_key, src_lng, src_val";

  private static final String queryCount = "SELECT count(*) FROM translation";

  private final static int MAX_SIDX_SIZE = SuperIndexGenerator.LEN_SRC_KEY;

  // key + srclng
  private static final int CACHED_BYTES = DatabaseReader.MAX_SIDX_SIZE + 4;

  private final static int TOTAL_SIDX_CACHE_BYTES = 1024 * 1024;

  // key + idx
  private final static int TOTAL_SIDX_CACHE_LENGTH = DatabaseReader.TOTAL_SIDX_CACHE_BYTES
      / DatabaseReader.CACHED_BYTES;


  private static long compressDataBlock() throws FileNotFoundException, IOException
  {
    System.out.println("data.xz: " + DatabaseReader.OUT_DATA_XZ);
    return CompressHelper.compress(DatabaseReader.OUT_DATA, DatabaseReader.OUT_DATA_XZ);
  }


  private static long compressCachedBlock() throws FileNotFoundException, IOException
  {
    System.out.println("cached.xz: " + DatabaseReader.OUT_CACHED_XZ);
    return CompressHelper.compress(DatabaseReader.OUT_CACHED, DatabaseReader.OUT_CACHED_XZ);
  }


  private static long compressIndexBlock() throws FileNotFoundException, IOException
  {
    System.out.println("index.xz: " + DatabaseReader.OUT_INDEX_XZ);
    return CompressHelper.compress(DatabaseReader.OUT_INDEX, DatabaseReader.OUT_INDEX_XZ);
  }


  private static long compressHeaderBlock() throws FileNotFoundException, IOException
  {
    System.out.println("header.xz: " + DatabaseReader.OUT_HEADER_XZ);
    return CompressHelper.compress(DatabaseReader.OUT_HEADER, DatabaseReader.OUT_HEADER_XZ);
  }

  private int flowDefOffset;

  private int flowIdxOffset;


  private void init()
  {
    this.flowDefOffset = 0;
    this.flowIdxOffset = 0;
  }


  private static void pack(long headerCompressedLen) throws IOException
  {
    System.out.println("pack: " + DatabaseReader.OUT_DXZ);
    try (BufferedOutputStream outDxz =
        new BufferedOutputStream(new FileOutputStream(DatabaseReader.OUT_DXZ), Helper.BUFFER_SIZE);
        BufferedInputStream inHeader = new BufferedInputStream(new FileInputStream(DatabaseReader.OUT_HEADER_XZ));
        BufferedInputStream inCached = new BufferedInputStream(new FileInputStream(DatabaseReader.OUT_CACHED_XZ));
        BufferedInputStream inIndex = new BufferedInputStream(new FileInputStream(DatabaseReader.OUT_INDEX_XZ));
        BufferedInputStream inData = new BufferedInputStream(new FileInputStream(DatabaseReader.OUT_DATA_XZ));)
    {
      outDxz.write(ArrayHelper.toBytes((short) headerCompressedLen));
      Helper.write(inHeader, outDxz);
      Helper.write(inCached, outDxz);
      Helper.write(inIndex, outDxz);
      Helper.write(inData, outDxz);
    }
  }


  /**
   * Writes displayable data (html)
   * 
   * @param outData
   * 
   * @param trl
   * @return
   * @throws IOException
   */
  private int writeData(BufferedOutputStream outData, Translation trl) throws IOException
  {
    final byte[] tgtData = ArrayHelper.toBytes(trl.getTgtVal());
    final byte[] srcData = ArrayHelper.toBytes(trl.getSrcVal());
    outData.write(srcData);
    outData.write(tgtData);
    this.flowDefOffset += tgtData.length + srcData.length;
    return srcData.length;
  }


  private void writeHeader(long cachedLen, long idxLen, long dataLen) throws FileNotFoundException, IOException
  {
    // 0x0
    // I total
    // I idxLen
    // I defLen
    // I[] defsCount per lng
    try (BufferedOutputStream outHeader =
        new BufferedOutputStream(new FileOutputStream(DatabaseReader.OUT_HEADER), Helper.BUFFER_SIZE);)
    {
      outHeader.write(ArrayHelper.toBytes(this.totalCount));
      outHeader.write(ArrayHelper.toBytes((short) cachedLen));
      outHeader.write(ArrayHelper.toBytes((int) idxLen));
      outHeader.write(ArrayHelper.toBytes((int) dataLen));
      for (int i = 0; i < DatabaseReader.LANGUAGES_SIZE; i++)
      {
        outHeader.write(this.defsCount[i]);
      }
    }
  }


  /**
   * Writes sorted super index (key, srclng, tgtlng, key, srcVal len, data
   * offset)
   * 
   * @param outIndex
   * 
   * @param srcValLen
   * @param key
   * @param data
   * @return
   * @throws IOException
   */
  private int writeIndex(BufferedOutputStream outIndex, short srcValLen, Translation trl) throws IOException
  {
    final byte[] srcKey = new byte[SuperIndexGenerator.LEN_SRC_KEY];
    System.arraycopy(trl.getSrcKey(), 0, srcKey, 0, trl.getSrcKey().length);
    outIndex.write((byte) trl.getSrcLng());
    outIndex.write((byte) trl.getTgtLng());
    outIndex.write(srcKey);
    outIndex.write(ArrayHelper.toBytes(srcValLen));
    outIndex.write(ArrayHelper.toBytes(this.flowDefOffset));

    this.flowIdxOffset += 1 + 1 + 2 + 4 + SuperIndexGenerator.LEN_SRC_KEY; // srcval,
                                                                           // def
                                                                           // offset,
                                                                           // size
    return this.flowIdxOffset;
  }
}
