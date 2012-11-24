/*  Copyright (c) 2010 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.kkdict.summarization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.types.GeoLocation;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.DictHelper;
import cn.kk.kkdict.utils.Helper;

/**
 * See http://dbpedia.hg.sourceforge.net/hgweb/dbpedia/dbpedia/file/945c24bdc54c/extraction/extractors/GeoExtractor.php
 * 
 */
public class WikiDictGeoLocationCorrector {
  private static final char    UNKNOWN_DIR      = '\0';
  public static final String   IN_DIR           = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKIPEDIA);
  public static final String   OUT_DIR          = Configuration.IMPORTER_FOLDER_FILTERED_DICTS.getPath(Source.DICT_WIKIPEDIA);
  public static final String   OUT_DIR_FINISHED = WikiDictGeoLocationCorrector.OUT_DIR + "/finished";

  public static final String   SUFFIX_CORRECTED = "_corrected";
  private static final boolean DEBUG            = false;
  private static final boolean TRACE            = false;

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    new File(WikiDictGeoLocationCorrector.OUT_DIR).mkdirs();
    final File inDirFile = new File(WikiDictGeoLocationCorrector.IN_DIR);
    if (inDirFile.isDirectory()) {

      System.out.print("修复wiki坐标文件'" + WikiDictGeoLocationCorrector.IN_DIR + "' ... ");
      final File[] files = inDirFile.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          return name.startsWith("output-dict_coords.");
        }
      });
      System.out.println(files.length);

      final long start = System.currentTimeMillis();
      final String[] filePaths = Helper.getFileNames(files);
      final ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
      final DictByteBufferRow row = new DictByteBufferRow();
      for (final String f : filePaths) {
        if (WikiDictGeoLocationCorrector.DEBUG) {
          System.out.println("处理坐标文件：" + f);
        }
        final String outFile = WikiDictGeoLocationCorrector.OUT_DIR + File.separator
            + Helper.appendFileName(new File(f).getName(), WikiDictGeoLocationCorrector.SUFFIX_CORRECTED);
        final Language lng = DictHelper.getWikiLanguage(f);
        if (lng != null) {
          final long startFile = System.currentTimeMillis();
          int statValid = 0;
          int statInvalid = 0;
          try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
              final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), Helper.BUFFER_SIZE);) {

            while (-1 != ArrayHelper.readLine(in, lineBB)) {
              row.parseFrom(lineBB);
              if ((row.size() == 1) && (row.getAttributesSize(0, 0) == 1)) {
                if (-1 != WikiDictGeoLocationCorrector.correctGeoLocation(row)) {
                  statValid++;
                  out.write(lineBB.array(), 0, lineBB.limit());
                  out.write(Helper.SEP_NEWLINE_CHAR);
                  if (WikiDictGeoLocationCorrector.DEBUG) {
                    System.out.println("写入坐标：" + ArrayHelper.toString(lineBB));
                  }
                } else {
                  statInvalid++;
                  if (WikiDictGeoLocationCorrector.DEBUG) {
                    System.err.println("跳过坐标：" + ArrayHelper.toString(lineBB));
                  }
                }
              }
            }
          }
          System.out.println("完成'" + outFile + "'，有效：" + statValid + "，无效：" + statInvalid + "（" + Helper.formatSpace(new File(outFile).length()) + "），用时："
              + Helper.formatDuration(System.currentTimeMillis() - startFile));
          final File file = new File(f);
          file.renameTo(new File(WikiDictGeoLocationCorrector.OUT_DIR_FINISHED, file.getName()));
        }
      }
      ArrayHelper.giveBack(lineBB);
      System.out.println("修复坐标文件总共用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
    }
  }

  public static int correctGeoLocation(final DictByteBufferRow row) {
    final ByteBuffer bb = row.getAttribute(0, 0, 0);
    final int insertPos = ArrayHelper.positionP(bb, GeoLocation.TYPE_ID_BYTES.length);

    ArrayHelper.stripP(bb, (byte) ' ');
    String coordText = Helper.stripHtmlText(Helper.unescapeHtml(ArrayHelper.toStringP(bb)), true).toLowerCase();
    int idx;
    if ((idx = coordText.indexOf('}')) != -1) {
      coordText = coordText.substring(0, idx);
    }
    coordText = coordText.replace('º', '°');
    final String[] pieces = coordText.split("\\|");
    final String firstPiece = pieces[0];
    final Coordinate c = new Coordinate();

    if (coordText.contains("north coord")) {
      // change defaults
      c.setLat('n');
      c.setLon('w');
    }

    if (firstPiece.startsWith("{{")) {
      if (firstPiece.startsWith("{{coordinate")) {
        // http://de.wikipedia.org/wiki/Wikipedia:WikiProjekt_Georeferenzierung/Neue_Koordinatenvorlage
        // {{Coordinate|NS=49.759681|EW=6.6440194|type=landmark|dim=25|region=DE-RP}}
        // {{Coordinate|NS=49/45/34.85/N|EW=6/38/38.47/E|type=landmark|dim=25|region=DE-RP}}
        for (int i = 1; i < pieces.length; i++) {
          try {
            final String[] pair = pieces[i].split("=");
            if (pair.length == 2) {
              final String[] parts = pair[1].split("/");
              if ("ns".equals(pair[0])) {
                if (parts.length == 1) {
                  c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                } else if (parts.length > 0) {
                  c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                  if (parts.length > 1) {
                    c.setLatmin(WikiDictGeoLocationCorrector.getDouble(parts[1]));
                  }
                  if (parts.length > 2) {
                    c.setLatsec(WikiDictGeoLocationCorrector.getDouble(parts[2]));
                  }
                  if (parts.length > 3) {
                    c.setLat(WikiDictGeoLocationCorrector.getChar(parts[3], c.getLat()));
                  }
                }
              } else if ("ew".equals(pair[0])) {
                if (parts.length == 1) {
                  c.setLondeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                } else if (parts.length > 0) {
                  c.setLondeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                  if (parts.length > 1) {
                    c.setLonmin(WikiDictGeoLocationCorrector.getDouble(parts[1]));
                  }
                  if (parts.length > 2) {
                    c.setLonsec(WikiDictGeoLocationCorrector.getDouble(parts[2]));
                  }
                  if (parts.length > 3) {
                    c.setLon(WikiDictGeoLocationCorrector.getChar(parts[3], c.getLat()));
                  }
                }
              }
            }
          } catch (final IllegalArgumentException e) {
            // ignore
          }
        }
        if (WikiDictGeoLocationCorrector.TRACE) {
          if (c.isValid()) {
            System.out.println("coordinate: " + c);
          } else {
            System.err.println("coordinate: " + c + "; " + coordText);
          }
        }
      } else if (firstPiece.startsWith("{{koor") || (firstPiece.startsWith("{{coor") && (pieces.length > 1) && (pieces[1].indexOf('_') != -1))) {
        // {{KoordinateTextArtikel|40_14_40_N_111_39_39_W_type:city(105161)_region:US-UT|40??14'40N,111??39'39GG??W}}
        // {{Koordinate Artikel|49_45_34.85_N_6_38_38.47_E_type:landmark_region:DE-RP_dim:25|49?? 45?? 35???? n.
        // Br., 6??
        // 38??38???? ?. L.}}
        // {{Koordinate Artikel|49.759681_N_6.6440194_E_type:landmark_region:DE-RP_dim:25|49?? 45?? 35?? n. Br.,
        // 6??
        // 38??
        // 38?? ?. L.}}
        // {{Koordinate Artikel|49_45_34.85_N_6_38_38.47_E_type:landmark_region:DE-RP_dim:25|49?? 45?? 35?? n.
        // Br.,
        // 6??
        // 38?? 38?? ?. L.}}
        try {
          if (pieces.length > 1) {
            final String[] parts = pieces[1].split("_");
            int p = 0;
            COORD: for (int i = 0; i < parts.length; i++) {
              final String v = parts[i];
              if (WikiDictGeoLocationCorrector.isChar(v)) {
                if (p < 4) {
                  p = 4;
                } else {
                  p = 8;
                }
              } else {
                p++;
              }
              switch (p) {
                case 1:
                  c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(v));
                  break;
                case 2:
                  c.setLatmin(WikiDictGeoLocationCorrector.getDouble(v));
                  break;
                case 3:
                  c.setLatsec(WikiDictGeoLocationCorrector.getDouble(v));
                  break;
                case 4:
                  c.setLat(WikiDictGeoLocationCorrector.getChar(v, c.getLat()));
                  break;
                case 5:
                  c.setLondeg(WikiDictGeoLocationCorrector.getDouble(v));
                  break;
                case 6:
                  c.setLonmin(WikiDictGeoLocationCorrector.getDouble(v));
                  break;
                case 7:
                  c.setLonsec(WikiDictGeoLocationCorrector.getDouble(v));
                  break;
                case 8:
                  c.setLon(WikiDictGeoLocationCorrector.getChar(v, c.getLon()));
                  break COORD;
                default:
                  break;
              }
            }
          }
        } catch (final IllegalArgumentException e) {
          // ignore
        }
        if (WikiDictGeoLocationCorrector.TRACE) {
          if (c.isValid()) {
            System.out.println("koor: " + c);
          } else {
            System.err.println("koor: " + c + "; " + coordText);
          }
        }
      } else if (firstPiece.startsWith("{{coor") || firstPiece.startsWith("{{좌표") || firstPiece.startsWith("{{Location")) {
        // {{coord|latitude|longitude[|parameters][|display=display]}}
        // {{coord|dd|N/S|dd|E/W[|parameters][|display=display]}}
        // {{coord|dd|mm|N/S|dd|mm|E/W[|parameters][|display=display]}}
        // {{coord|dd|mm|ss|N/S|dd|mm|ss|E/W[|parameters][|display=display]}}
        // {{cooricon|47|38|37.6|N|122|7|44.17|W|3000}}
        // {{Coord|28|46|3.57|S|133|46|10.31|E|25000000|display=title}}
        // {{Coord|43.651234|-79.383333}}
        // {{Coord|43.651234|N|79.383333|W}}
        // {{Coord|43|29|4|N|79|23|0|W}}
        // {{coord|52|28|59|N|1|53|37|W |display=inline,title|region:GB_type:city}}
        // {{coor title d|deg|NS|deg|EW[|parameters]}}
        // {{coor title dm|deg|min|NS|deg|min|EW[|parameters]}}
        // {{coor title dms|deg|min|sec|NS|deg|min|sec|EW[|parameters]}}
        // {{Location|42|43|49.5|N|41|26|56.4|E}}
        try {
          int nr = 0;
          char b;
          String v;
          boolean stop = false;
          PARSE: for (int i = 1; i < pieces.length; i++) {
            v = pieces[i];
            if ((i == 1) && (v.indexOf('=') != -1)) {
              continue;
            } else if ((nr == 1) && (pieces.length < 7) && (v.indexOf('.') != -1)) {
              stop = true;
              nr = 5;
            } else if ((v.length() == 1) && (((b = v.charAt(0)) == 'n') || (b == 's'))) {
              nr = 4;
            } else if ((v.length() == 1) && (((b = v.charAt(0)) == 'e') || (b == 'w') || (b == 'o'))) {
              nr = 8;
            } else {
              nr++;
            }
            switch (nr) {
              case 1:
                c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(v));
                break;
              case 2:
                c.setLatmin(WikiDictGeoLocationCorrector.getDouble(v));
                break;
              case 3:
                c.setLatsec(WikiDictGeoLocationCorrector.getDouble(v));
                break;
              case 4:
                c.setLat(WikiDictGeoLocationCorrector.getChar(v, c.getLat()));
                break;
              case 5:
                c.setLondeg(WikiDictGeoLocationCorrector.getDouble(v));
                if (stop) {
                  break PARSE;
                }
                break;
              case 6:
                c.setLonmin(WikiDictGeoLocationCorrector.getDouble(v));
                break;
              case 7:
                c.setLonmin(WikiDictGeoLocationCorrector.getDouble(v));
                break;
              case 8:
                c.setLon(WikiDictGeoLocationCorrector.getChar(v, c.getLon()));
                break PARSE;
              default:
                break;
            }
          }
        } catch (final IllegalArgumentException e) {
          // ignore
        }
        if (WikiDictGeoLocationCorrector.TRACE) {
          if (c.isValid()) {
            System.out.println("coor: " + c);
          } else {
            System.err.println("coor: " + c + "; " + coordText);
          }
        }
      } else if (firstPiece.startsWith("{{geolink") || firstPiece.startsWith("{{mapit")) {
        // {{Geolinks-US-streetscale|37.429847|-122.169447}}
        try {
          if (pieces.length > 2) {
            c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(pieces[1]));
            c.setLondeg(WikiDictGeoLocationCorrector.getDouble(pieces[2]));
          }
        } catch (final IllegalArgumentException e) {
          // ignore
        }
        if (WikiDictGeoLocationCorrector.TRACE) {
          if (c.isValid()) {
            System.out.println("geo: " + c);
          } else {
            System.err.println("geo: " + c + "; " + coordText);
          }
        }
      }
    } else {
      // latitude=2.6|longitude=21.5
      // latd=52|latm=21|lats=|latNS=N|longd=21|longm=14|longs=|longEW=E
      // lat_deg=48|lat_min=08|lat_sec=28|lon_deg=16|lon_min=28|lon_sec=43
      try {
        String[] pair;
        for (int i = 0; i < pieces.length; i++) {
          pair = pieces[i].split("=");
          if (pair.length == 2) {
            final String k = pair[0];
            String v = pair[1];
            if ("latd".equals(k) || "lat_deg".equals(k) || "lat-deg".equals(k) || "lat_d".equals(k) || "lat_degrees".equals(k)
                || "koordinate_breitengrad".equals(k) || "n".equals(k) || "north coord".equals(k) || "latitude".equals(k) || "latdeg".equals(k)) {
              if (v.indexOf('°') != -1) {
                v = v.replaceAll("[°'′″'\"]+", "/");
              }
              if (v.indexOf('/') != -1) {
                final String[] parts = v.split("/");
                if (parts.length > 0) {
                  c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                  if (parts.length > 1) {
                    c.setLatmin(WikiDictGeoLocationCorrector.getDouble(parts[1]));
                  }
                  if (parts.length > 2) {
                    c.setLatsec(WikiDictGeoLocationCorrector.getDouble(parts[2]));
                  }
                  if (parts.length > 3) {
                    c.setLat(WikiDictGeoLocationCorrector.getChar(parts[3], c.getLat()));
                  }
                }
              } else {
                c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(v));
              }
            } else if ("latm".equals(k) || "lat_min".equals(k) || "lat-min".equals(k) || "lat_m".equals(k) || "lat_minutes".equals(k)
                || "koordinate_breitenminute".equals(k) || "latmin".equals(k)) {
              c.setLatmin(WikiDictGeoLocationCorrector.getDouble(v));
            } else if ("lats".equals(k) || "lat_sec".equals(k) || "lat-sec".equals(k) || "lat_s".equals(k) || "lat_seconds".equals(k)
                || "koordinate_breitensekunde".equals(k) || "latsec".equals(k)) {
              if ((v.indexOf('/') != -1) || (v.indexOf('°') != -1)) {
                String[] parts;
                if (v.indexOf('/') != -1) {
                  parts = v.split("/");
                } else {
                  parts = v.split("°");
                }
                c.setLatsec(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                if (parts.length > 1) {
                  c.setLat(WikiDictGeoLocationCorrector.getChar(parts[1], c.getLat()));
                }
              } else {
                c.setLatsec(WikiDictGeoLocationCorrector.getDouble(v));
              }
            } else if ("latns".equals(k) || "lat_ns".equals(k) || "lat_hem".equals(k) || "latns".equals(k) || "lat_direction".equals(k)
                || "koordinate_breite".equals(k)) {
              c.setLat(WikiDictGeoLocationCorrector.getChar(v, c.getLat()));
            } else if ("longd".equals(k) || "lon_deg".equals(k) || "lon-deg".equals(k) || "lon_d".equals(k) || "long_d".equals(k) || "long_degrees".equals(k)
                || "koordinate_längengrad".equals(k) || "e".equals(k) || "west coord".equals(k) || "longitude".equals(k) || "londeg".equals(k)) {
              if (v.indexOf('°') != -1) {
                v = v.replaceAll("[°'′″\"]", "/");
              }
              if (v.indexOf('/') != -1) {
                final String[] parts = v.split("/");
                if (parts.length > 0) {
                  c.setLondeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                  if (parts.length > 1) {
                    c.setLonmin(WikiDictGeoLocationCorrector.getDouble(parts[1]));
                  }
                  if (parts.length > 2) {
                    c.setLonsec(WikiDictGeoLocationCorrector.getDouble(parts[2]));
                  }
                  if (parts.length > 3) {
                    c.setLon(WikiDictGeoLocationCorrector.getChar(parts[3], c.getLon()));
                  }
                }
              } else {
                c.setLondeg(WikiDictGeoLocationCorrector.getDouble(v));
              }
            } else if ("longm".equals(k) || "lon_min".equals(k) || "lon-min".equals(k) || "lon_m".equals(k) || "long_m".equals(k) || "long_minutes".equals(k)
                || "koordinate_längenminute".equals(k) || "lonmin".equals(k)) {
              c.setLonmin(WikiDictGeoLocationCorrector.getDouble(v));
            } else if ("longs".equals(k) || "lon_sec".equals(k) || "lon-sec".equals(k) || "lon_s".equals(k) || "long_s".equals(k) || "long_seconds".equals(k)
                || "koordinate_längensekunde".equals(k) || "lonsec".equals(k)) {
              if ((v.indexOf('/') != -1) || (v.indexOf('°') != -1)) {
                String[] parts;
                if (v.indexOf('/') != -1) {
                  parts = v.split("/");
                } else {
                  parts = v.split("°");
                }

                c.setLonsec(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                if (parts.length > 1) {
                  c.setLon(WikiDictGeoLocationCorrector.getChar(parts[1], c.getLon()));
                }
              } else {
                c.setLonsec(WikiDictGeoLocationCorrector.getDouble(v));
              }
            } else if ("longns".equals(k) || "long_ns".equals(k) || "lon_hem".equals(k) || "long_hem".equals(k) || "lonns".equals(k)
                || "long_direction".equals(k) || "koordinate_länge".equals(k)) {
              c.setLon(WikiDictGeoLocationCorrector.getChar(v, c.getLon()));
            } else if ("lon".equals(k) || "long".equals(k)) {
              if (v.length() == 1) {
                if (WikiDictGeoLocationCorrector.isChar(v)) {
                  c.setLon(WikiDictGeoLocationCorrector.getChar(v, c.getLon()));
                } else {
                  c.setLondeg(WikiDictGeoLocationCorrector.getDouble(v));
                }
              } else {
                if (v.indexOf('°') != -1) {
                  v = v.replaceAll("[°'′″\"]", "/");
                }
                if (v.indexOf('/') != -1) {

                  final String[] parts = v.split("/");
                  if (parts.length > 0) {
                    c.setLondeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                    if (parts.length > 1) {
                      c.setLonmin(WikiDictGeoLocationCorrector.getDouble(parts[1]));
                    }
                    if (parts.length > 2) {
                      c.setLonsec(WikiDictGeoLocationCorrector.getDouble(parts[2]));
                    }
                    if (parts.length > 3) {
                      c.setLon(WikiDictGeoLocationCorrector.getChar(parts[3], c.getLon()));
                    }
                  }
                } else {
                  c.setLondeg(WikiDictGeoLocationCorrector.getDouble(v));
                }
              }
            } else if ("lat".equals(k)) {
              if (v.length() == 1) {
                if (WikiDictGeoLocationCorrector.isChar(v)) {
                  c.setLat(WikiDictGeoLocationCorrector.getChar(v, c.getLat()));
                } else {
                  c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(v));
                }
              } else {
                if (v.indexOf('°') != -1) {
                  v = v.replaceAll("[°'′″\"]", "/");
                }
                if (v.indexOf('/') != -1) {
                  final String[] parts = v.split("/");
                  if (parts.length > 0) {
                    c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(parts[0]));
                    if (parts.length > 1) {
                      c.setLatmin(WikiDictGeoLocationCorrector.getDouble(parts[1]));
                    }
                    if (parts.length > 2) {
                      c.setLatsec(WikiDictGeoLocationCorrector.getDouble(parts[2]));
                    }
                    if (parts.length > 3) {
                      c.setLat(WikiDictGeoLocationCorrector.getChar(parts[3], c.getLat()));
                    }
                  }
                } else {
                  c.setLatdeg(WikiDictGeoLocationCorrector.getDouble(v));
                }
              }
            }
          }
        }
      } catch (final IllegalArgumentException e) {
        // ignore
      }
      if (WikiDictGeoLocationCorrector.TRACE) {
        if (c.isValid()) {
          System.out.println("box: " + c);
        } else {
          System.err.println("box: " + c + "; " + coordText);
        }
      }
    }
    // Samples:
    //
    //
    // array('lat_deg', 'lat_min', 'lat_sec', 'lat_NS', 'lon_deg', 'lon_min', 'lon_sec', 'long_EW'),
    // array('lat-deg', 'lat-min', 'lat-sec', 'lat', 'lon-deg', 'lon-min', 'lon-sec', 'lon'),
    // array('lat_d', 'lat_m', 'lat_s', 'lat_NS', 'long_d', 'long_m', 'long_s', 'long_EW'),
    // array('latd', 'latm', 'lats', 'latNS', 'longd', 'longm', 'longs', 'longEW'),
    // array('lat_d', 'lat_m', 'lat_s', 'lat_hem', 'lon_d', 'lon_m', 'lon_s', 'lon_hem'),
    // array('lat_degrees', 'lat_minutes', 'lat_seconds', 'lat_direction', 'long_degrees', 'long_minutes',
    // 'long_seconds', 'long_direction'),
    // array('Koordinate_Breitengrad', 'Koordinate_Breitenminute', 'Koordinate_Breitensekunde', 'Koordinate_Breite',
    // 'Koordinate_L%C3%A4ngengrad', 'Koordinate_L%C3%A4ngenminute', 'Koordinate_L%C3%A4ngensekunde',
    // 'Koordinate_L%C3%A4nge'),
    // array('N', null, null, null, 'E', null, null, null),
    // array('LatDeg', 'LatMin', 'LatSec', null, 'LonDeg', 'LonMin', 'LonSec', null),
    // array('north coord', null, null, null, 'west coord', null, null, null, 'N' /* NS */, 'W' /* EW */),
    // array('latitude', null, null, null, 'longitude', null, null, null),

    // System.out.println(coordText);
    if (c.isValid()) {
      bb.clear().position(insertPos);
      final double[] r = c.getResult();
      bb.put(String.valueOf(Helper.toFixed(r[0], 8)).getBytes(Helper.CHARSET_UTF8));
      bb.put((byte) ',');
      bb.put(String.valueOf(Helper.toFixed(r[1], 8)).getBytes(Helper.CHARSET_UTF8));
      bb.limit(bb.position());
      row.parseFrom(bb);
      // System.out.println(ArrayHelper.toString(row.getByteBuffer()));
      return bb.limit();
    } else {
      row.parseFrom(row.getDefinition(0));
      return -1;
    }
  }

  private static boolean isChar(final String v) {
    if (v.length() > 0) {
      final char c = v.charAt(0);
      if ((c == 'n') || (c == 'w') || (c == 's') || (c == 'e') || (c == 'o')) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private static char getChar(final String v, final char defaultChar) {
    if (v.isEmpty()) {
      return defaultChar;
    }
    if ((v.indexOf('=') != -1) || (v.indexOf(':') != -1)) {
      throw new IllegalArgumentException(v);
    }
    final char c = v.charAt(0);
    if ((c == 'n') || (c == 'w') || (c == 's') || (c == 'e') || (c == 'o')) {
      return c;
    }
    System.err.println("不是方向字符：" + v);
    return WikiDictGeoLocationCorrector.UNKNOWN_DIR;
  }

  private static double getDouble(final String v) {
    if (v.isEmpty()) {
      return 0;
    }
    if ((v.indexOf('=') != -1) || (v.indexOf(':') != -1)) {
      throw new IllegalArgumentException(v);
    }
    try {
      return Double.parseDouble(v);
    } catch (final NumberFormatException e) {
      System.err.println(v + "：" + e.toString());
    }
    return -1;
  }

  public static class Coordinate {
    private static final int UNKNOWN = Integer.MIN_VALUE;
    private double           latdeg  = Coordinate.UNKNOWN;
    private double           latmin  = Coordinate.UNKNOWN;
    private double           latsec  = Coordinate.UNKNOWN;
    private char             lat     = 'n';
    private double           londeg  = Coordinate.UNKNOWN;
    private double           lonmin  = Coordinate.UNKNOWN;
    private double           lonsec  = Coordinate.UNKNOWN;
    private char             lon     = 'e';

    public boolean isValid() {
      boolean valid = ((this.latdeg != Coordinate.UNKNOWN) || (this.latmin != Coordinate.UNKNOWN) || (this.latsec != Coordinate.UNKNOWN))
          && ((this.londeg != Coordinate.UNKNOWN) || (this.lonmin != Coordinate.UNKNOWN) || (this.lonsec != Coordinate.UNKNOWN));
      if (valid) {
        if (((this.latdeg != Coordinate.UNKNOWN) && ((this.latdeg > 90) || (this.latdeg < -90)))
            || ((this.londeg != Coordinate.UNKNOWN) && ((this.londeg > 180) || (this.londeg < -180)))
            || ((this.latmin != Coordinate.UNKNOWN) && ((this.latmin > 60) || (this.latmin < 0)))
            || ((this.lonmin != Coordinate.UNKNOWN) && ((this.lonmin > 60) || (this.lonmin < 0)))
            || ((this.latsec != Coordinate.UNKNOWN) && ((this.latsec > 60) || (this.latsec < 0)))
            || ((this.lonsec != Coordinate.UNKNOWN) && ((this.lonsec > 60) || (this.lonsec < 0)))) {
          valid = false;
        }
      }
      return valid;
    }

    public double[] getResult() {
      double latftr = 1.0;
      double lonftr = 1.0;
      if ((this.lat != WikiDictGeoLocationCorrector.UNKNOWN_DIR) && (this.lat == 's')) {
        latftr = -1;
      }
      if ((this.lon != WikiDictGeoLocationCorrector.UNKNOWN_DIR) && (this.lon == 'w')) {
        lonftr = -1;
      }
      return new double[] { Coordinate.calc(this.latdeg, this.latmin, this.latsec, latftr), Coordinate.calc(this.londeg, this.lonmin, this.lonsec, lonftr) };
    }

    private static final double calc(double deg, double min, double sec, final double ftr) {
      double d = deg;
      double m = min;
      double s = sec;
      if (d == Coordinate.UNKNOWN) {
        d = 0;
      }
      if (m == Coordinate.UNKNOWN) {
        m = 0;
      }
      if (s == Coordinate.UNKNOWN) {
        s = 0;
      }
      m += s / 60.0;
      if (d < 0) {
        d -= m / 60.0;
      } else {
        d += m / 60.0;
      }
      return d * ftr;
    }

    @Override
    public String toString() {
      return "latdeg=" + this.latdeg + ", latmin=" + this.latmin + ", latsec=" + this.latsec + ", latNS=" + this.lat + ", londeg=" + this.londeg + ", lonmin="
          + this.lonmin + ", lonsec=" + this.lonsec + ", lonEW=" + this.lon;
    }

    public void setLatdeg(final double latdeg) {
      if (this.latdeg == Coordinate.UNKNOWN) {
        this.latdeg = latdeg;
      }
    }

    public void setLatmin(final double latmin) {
      if (this.latmin == Coordinate.UNKNOWN) {
        this.latmin = latmin;
      }
    }

    public void setLatsec(final double latsec) {
      if (this.latsec == Coordinate.UNKNOWN) {
        this.latsec = latsec;
      }
    }

    public void setLat(final char lat) {
      if (this.lat == Coordinate.UNKNOWN) {
        this.lat = lat;
      }
    }

    public void setLondeg(double londeg) {
      if (this.londeg == Coordinate.UNKNOWN) {
        if (londeg > 180) {
          this.londeg = londeg - 360;
        } else {
          this.londeg = londeg;
        }
      }
    }

    public void setLonmin(final double lonmin) {
      if (this.lonmin == Coordinate.UNKNOWN) {
        this.lonmin = lonmin;
      }
    }

    public void setLonsec(final double lonsec) {
      if (this.lonsec == Coordinate.UNKNOWN) {
        this.lonsec = lonsec;
      }
    }

    public void setLon(final char lon) {
      if (this.lon == Coordinate.UNKNOWN) {
        this.lon = lon;
      }
    }

    public char getLat() {
      return this.lat;
    }

    public char getLon() {
      return this.lon;
    }

  }
}
