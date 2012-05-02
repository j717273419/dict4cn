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
    private static final char UNKNOWN_DIR = '\0';
    public static final String IN_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_WIKIPEDIA);
    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_FILTERED_DICTS.getPath(Source.DICT_WIKIPEDIA);
    public static final String SUFFIX_CORRECTED = "_corrected";
    private static final boolean DEBUG = false;
    private static final boolean TRACE = false;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new File(OUT_DIR).mkdirs();
        File inDirFile = new File(IN_DIR);
        if (inDirFile.isDirectory()) {

            System.out.print("修复wiki坐标文件'" + IN_DIR + "' ... ");
            File[] files = inDirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("output-dict_coords.");
                }
            });
            System.out.println(files.length);

            long start = System.currentTimeMillis();
            String[] filePaths = Helper.getFileNames(files);
            ByteBuffer lineBB = ArrayHelper.borrowByteBufferSmall();
            DictByteBufferRow row = new DictByteBufferRow();
            for (String f : filePaths) {
                if (DEBUG) {
                    System.out.println("处理坐标文件：" + f);
                }
                String outFile = OUT_DIR + File.separator
                        + Helper.appendFileName(new File(f).getName(), SUFFIX_CORRECTED);
                Language lng = DictHelper.getWikiLanguage(f);
                if (lng != null) {
                    long startFile = System.currentTimeMillis();

                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), Helper.BUFFER_SIZE);
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile),
                            Helper.BUFFER_SIZE);
                    int statValid = 0;
                    int statInvalid = 0;
                    while (-1 != ArrayHelper.readLine(in, lineBB)) {
                        row.parseFrom(lineBB);
                        if (row.size() == 1 && row.getAttributesSize(0, 0) == 1) {
                            if (-1 != correctGeoLocation(row)) {
                                statValid++;
                                out.write(lineBB.array(), 0, lineBB.limit());
                                out.write(Helper.SEP_NEWLINE_CHAR);
                                if (DEBUG) {
                                    System.out.println("写入坐标：" + ArrayHelper.toString(lineBB));
                                }
                            } else {
                                statInvalid++;
                                if (DEBUG) {
                                    System.err.println("跳过坐标：" + ArrayHelper.toString(lineBB));
                                }
                            }
                        }
                    }
                    out.close();
                    in.close();
                    System.out.println("完成'" + outFile + "'，有效：" + statValid + "，无效：" + statInvalid + "（"
                            + Helper.formatSpace(new File(outFile).length()) + "），用时："
                            + Helper.formatDuration(System.currentTimeMillis() - startFile));
                }
            }
            ArrayHelper.giveBack(lineBB);
            System.out.println("修复坐标文件总共用时：" + Helper.formatDuration(System.currentTimeMillis() - start));
        }
    }

    public static int correctGeoLocation(DictByteBufferRow row) {
        ByteBuffer bb = row.getAttribute(0, 0, 0);
        final int insertPos = ArrayHelper.positionP(bb, GeoLocation.TYPE_ID_BYTES.length);

        ArrayHelper.stripP(bb, (byte) ' ');
        String coordText = Helper.stripHtmlText(Helper.unescapeHtml(ArrayHelper.toStringP(bb)), true).toLowerCase();
        int idx;
        if ((idx = coordText.indexOf('}')) != -1) {
            coordText = coordText.substring(0, idx);
        }
        coordText = coordText.replace('º', '°');
        String[] pieces = coordText.split("\\|");
        final String firstPiece = pieces[0];
        Coordinate c = new Coordinate();

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
                        String[] pair = pieces[i].split("=");
                        if (pair.length == 2) {
                            String[] parts = pair[1].split("/");
                            if ("ns".equals(pair[0])) {
                                if (parts.length == 1) {
                                    c.setLatdeg(getDouble(parts[0]));
                                } else if (parts.length > 0) {
                                    c.setLatdeg(getDouble(parts[0]));
                                    if (parts.length > 1) {
                                        c.setLatmin(getDouble(parts[1]));
                                    }
                                    if (parts.length > 2) {
                                        c.setLatsec(getDouble(parts[2]));
                                    }
                                    if (parts.length > 3) {
                                        c.setLat(getChar(parts[3], c.lat));
                                    }
                                }
                            } else if ("ew".equals(pair[0])) {
                                if (parts.length == 1) {
                                    c.setLondeg(getDouble(parts[0]));
                                } else if (parts.length > 0) {
                                    c.setLondeg(getDouble(parts[0]));
                                    if (parts.length > 1) {
                                        c.setLonmin(getDouble(parts[1]));
                                    }
                                    if (parts.length > 2) {
                                        c.setLonsec(getDouble(parts[2]));
                                    }
                                    if (parts.length > 3) {
                                        c.setLon(getChar(parts[3], c.lat));
                                    }
                                }
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
                if (DEBUG && TRACE) {
                    if (c.isValid()) {
                        System.out.println("coordinate: " + c);
                    } else {
                        System.err.println("coordinate: " + c + "; " + coordText);
                    }
                }
            } else if (firstPiece.startsWith("{{koor")
                    || (firstPiece.startsWith("{{coor") && pieces.length > 1 && pieces[1].indexOf('_') != -1)) {
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
                        String[] parts = pieces[1].split("_");
                        int p = 0;
                        COORD: for (int i = 0; i < parts.length; i++) {
                            String v = parts[i];
                            if (isChar(v)) {
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
                                c.setLatdeg(getDouble(v));
                                break;
                            case 2:
                                c.setLatmin(getDouble(v));
                                break;
                            case 3:
                                c.setLatsec(getDouble(v));
                                break;
                            case 4:
                                c.setLat(getChar(v, c.lat));
                                break;
                            case 5:
                                c.setLondeg(getDouble(v));
                                break;
                            case 6:
                                c.setLonmin(getDouble(v));
                                break;
                            case 7:
                                c.setLonsec(getDouble(v));
                                break;
                            case 8:
                                c.setLon(getChar(v, c.lon));
                                break COORD;
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // ignore
                }
                if (DEBUG && TRACE) {
                    if (c.isValid()) {
                        System.out.println("koor: " + c);
                    } else {
                        System.err.println("koor: " + c + "; " + coordText);
                    }
                }
            } else if (firstPiece.startsWith("{{coor") || firstPiece.startsWith("{{좌표")
                    || firstPiece.startsWith("{{Location")) {
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
                        if (i == 1 && v.indexOf('=') != -1) {
                            continue;
                        } else if (nr == 1 && pieces.length < 7 && v.indexOf('.') != -1) {
                            stop = true;
                            nr = 5;
                        } else if (v.length() == 1 && ((b = v.charAt(0)) == 'n' || b == 's')) {
                            nr = 4;
                        } else if (v.length() == 1 && ((b = v.charAt(0)) == 'e' || b == 'w' || b == 'o')) {
                            nr = 8;
                        } else {
                            nr++;
                        }
                        switch (nr) {
                        case 1:
                            c.setLatdeg(getDouble(v));
                            break;
                        case 2:
                            c.setLatmin(getDouble(v));
                            break;
                        case 3:
                            c.setLatsec(getDouble(v));
                            break;
                        case 4:
                            c.setLat(getChar(v, c.lat));
                            break;
                        case 5:
                            c.setLondeg(getDouble(v));
                            if (stop) {
                                break PARSE;
                            }
                            break;
                        case 6:
                            c.setLonmin(getDouble(v));
                            break;
                        case 7:
                            c.setLonmin(getDouble(v));
                            break;
                        case 8:
                            c.setLon(getChar(v, c.lon));
                            break PARSE;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // ignore
                }
                if (DEBUG && TRACE) {
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
                        c.setLatdeg(getDouble(pieces[1]));
                        c.setLondeg(getDouble(pieces[2]));
                    }
                } catch (IllegalArgumentException e) {
                    // ignore
                }
                if (DEBUG && TRACE) {
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
                        String k = pair[0];
                        String v = pair[1];
                        if ("latd".equals(k) || "lat_deg".equals(k) || "lat-deg".equals(k) || "lat_d".equals(k)
                                || "lat_degrees".equals(k) || "koordinate_breitengrad".equals(k) || "n".equals(k)
                                || "north coord".equals(k) || "latitude".equals(k) || "latdeg".equals(k)) {
                            if (v.indexOf('°') != -1) {
                                v = v.replaceAll("[°'′″'\"]+", "/");
                            }
                            if (v.indexOf('/') != -1) {
                                String[] parts = v.split("/");
                                if (parts.length > 0) {
                                    c.setLatdeg(getDouble(parts[0]));
                                    if (parts.length > 1) {
                                        c.setLatmin(getDouble(parts[1]));
                                    }
                                    if (parts.length > 2) {
                                        c.setLatsec(getDouble(parts[2]));
                                    }
                                    if (parts.length > 3) {
                                        c.setLat(getChar(parts[3], c.lat));
                                    }
                                }
                            } else {
                                c.setLatdeg(getDouble(v));
                            }
                        } else if ("latm".equals(k) || "lat_min".equals(k) || "lat-min".equals(k) || "lat_m".equals(k)
                                || "lat_minutes".equals(k) || "koordinate_breitenminute".equals(k)
                                || "latmin".equals(k)) {
                            c.setLatmin(getDouble(v));
                        } else if ("lats".equals(k) || "lat_sec".equals(k) || "lat-sec".equals(k) || "lat_s".equals(k)
                                || "lat_seconds".equals(k) || "koordinate_breitensekunde".equals(k)
                                || "latsec".equals(k)) {
                            if (v.indexOf('/') != -1 || v.indexOf('°') != -1) {
                                String[] parts;
                                if (v.indexOf('/') != -1) {
                                    parts = v.split("/");
                                } else {
                                    parts = v.split("°");
                                }
                                c.setLatsec(getDouble(parts[0]));
                                if (parts.length > 1) {
                                    c.setLat(getChar(parts[1], c.lat));
                                }
                            } else {
                                c.setLatsec(getDouble(v));
                            }
                        } else if ("latns".equals(k) || "lat_ns".equals(k) || "lat_hem".equals(k) || "latns".equals(k)
                                || "lat_direction".equals(k) || "koordinate_breite".equals(k)) {
                            c.setLat(getChar(v, c.lat));
                        } else if ("longd".equals(k) || "lon_deg".equals(k) || "lon-deg".equals(k) || "lon_d".equals(k)
                                || "long_d".equals(k) || "long_degrees".equals(k) || "koordinate_längengrad".equals(k)
                                || "e".equals(k) || "west coord".equals(k) || "longitude".equals(k)
                                || "londeg".equals(k)) {
                            if (v.indexOf('°') != -1) {
                                v = v.replaceAll("[°'′″\"]", "/");
                            }
                            if (v.indexOf('/') != -1) {
                                String[] parts = v.split("/");
                                if (parts.length > 0) {
                                    c.setLondeg(getDouble(parts[0]));
                                    if (parts.length > 1) {
                                        c.setLonmin(getDouble(parts[1]));
                                    }
                                    if (parts.length > 2) {
                                        c.setLonsec(getDouble(parts[2]));
                                    }
                                    if (parts.length > 3) {
                                        c.setLon(getChar(parts[3], c.lon));
                                    }
                                }
                            } else {
                                c.setLondeg(getDouble(v));
                            }
                        } else if ("longm".equals(k) || "lon_min".equals(k) || "lon-min".equals(k) || "lon_m".equals(k)
                                || "long_m".equals(k) || "long_minutes".equals(k)
                                || "koordinate_längenminute".equals(k) || "lonmin".equals(k)) {
                            c.setLonmin(getDouble(v));
                        } else if ("longs".equals(k) || "lon_sec".equals(k) || "lon-sec".equals(k) || "lon_s".equals(k)
                                || "long_s".equals(k) || "long_seconds".equals(k)
                                || "koordinate_längensekunde".equals(k) || "lonsec".equals(k)) {
                            if (v.indexOf('/') != -1 || v.indexOf('°') != -1) {
                                String[] parts;
                                if (v.indexOf('/') != -1) {
                                    parts = v.split("/");
                                } else {
                                    parts = v.split("°");
                                }

                                c.setLonsec(getDouble(parts[0]));
                                if (parts.length > 1) {
                                    c.setLon(getChar(parts[1], c.lon));
                                }
                            } else {
                                c.setLonsec(getDouble(v));
                            }
                        } else if ("longns".equals(k) || "long_ns".equals(k) || "lon_hem".equals(k)
                                || "long_hem".equals(k) || "lonns".equals(k) || "long_direction".equals(k)
                                || "koordinate_länge".equals(k)) {
                            c.setLon(getChar(v, c.lon));
                        } else if ("lon".equals(k) || "long".equals(k)) {
                            if (v.length() == 1) {
                                if (isChar(v)) {
                                    c.setLon(getChar(v, c.lon));
                                } else {
                                    c.setLondeg(getDouble(v));
                                }
                            } else {
                                if (v.indexOf('°') != -1) {
                                    v = v.replaceAll("[°'′″\"]", "/");
                                }
                                if (v.indexOf('/') != -1) {

                                    String[] parts = v.split("/");
                                    if (parts.length > 0) {
                                        c.setLondeg(getDouble(parts[0]));
                                        if (parts.length > 1) {
                                            c.setLonmin(getDouble(parts[1]));
                                        }
                                        if (parts.length > 2) {
                                            c.setLonsec(getDouble(parts[2]));
                                        }
                                        if (parts.length > 3) {
                                            c.setLon(getChar(parts[3], c.lon));
                                        }
                                    }
                                } else {
                                    c.setLondeg(getDouble(v));
                                }
                            }
                        } else if ("lat".equals(k)) {
                            if (v.length() == 1) {
                                if (isChar(v)) {
                                    c.setLat(getChar(v, c.lat));
                                } else {
                                    c.setLatdeg(getDouble(v));
                                }
                            } else {
                                if (v.indexOf('°') != -1) {
                                    v = v.replaceAll("[°'′″\"]", "/");
                                }
                                if (v.indexOf('/') != -1) {
                                    String[] parts = v.split("/");
                                    if (parts.length > 0) {
                                        c.setLatdeg(getDouble(parts[0]));
                                        if (parts.length > 1) {
                                            c.setLatmin(getDouble(parts[1]));
                                        }
                                        if (parts.length > 2) {
                                            c.setLatsec(getDouble(parts[2]));
                                        }
                                        if (parts.length > 3) {
                                            c.setLat(getChar(parts[3], c.lat));
                                        }
                                    }
                                } else {
                                    c.setLatdeg(getDouble(v));
                                }
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // ignore
            }
            if (DEBUG && TRACE) {
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
            double[] r = c.getResult();
            bb.put(String.valueOf(Helper.toFixed(r[0], 8)).getBytes(Helper.CHARSET_UTF8));
            bb.put((byte) ',');
            bb.put(String.valueOf(Helper.toFixed(r[1], 8)).getBytes(Helper.CHARSET_UTF8));
            return bb.limit(bb.position()).limit();
        } else {
            return -1;
        }
    }

    private static boolean isChar(String v) {
        final char c = v.charAt(0);
        if (c == 'n' || c == 'w' || c == 's' || c == 'e' || c == 'o') {
            return true;
        } else {
            return false;
        }

    }

    private static char getChar(String v, char defaultChar) {
        if (v.isEmpty()) {
            return defaultChar;
        }
        if (v.indexOf('=') != -1 || v.indexOf(':') != -1) {
            throw new IllegalArgumentException(v);
        }
        final char c = v.charAt(0);
        if (c == 'n' || c == 'w' || c == 's' || c == 'e' || c == 'o') {
            return c;
        }
        System.err.println("不是方向字符：" + v);
        return UNKNOWN_DIR;
    }

    private static double getDouble(String v) {
        if (v.isEmpty()) {
            return 0;
        }
        if (v.indexOf('=') != -1 || v.indexOf(':') != -1) {
            throw new IllegalArgumentException(v);
        }
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            System.err.println(v + "：" + e.toString());
        }
        return -1;
    }

    public static class Coordinate {
        private static final int UNKNOWN = Integer.MIN_VALUE;
        private double latdeg = UNKNOWN;
        private double latmin = UNKNOWN;
        private double latsec = UNKNOWN;
        private char lat = 'n';
        private double londeg = UNKNOWN;
        private double lonmin = UNKNOWN;
        private double lonsec = UNKNOWN;
        private char lon = 'e';

        public boolean isValid() {
            boolean valid = (latdeg != UNKNOWN || latmin != UNKNOWN || latsec != UNKNOWN)
                    && (londeg != UNKNOWN || lonmin != UNKNOWN || lonsec != UNKNOWN);
            if (valid) {
                if ((latdeg != UNKNOWN && (latdeg > 90 || latdeg < -90))
                        || (londeg != UNKNOWN && (londeg > 180 || londeg < -180))
                        || (latmin != UNKNOWN && (latmin > 60 || latmin < 0))
                        || (lonmin != UNKNOWN && (lonmin > 60 || lonmin < 0))
                        || (latsec != UNKNOWN && (latsec > 60 || latsec < 0))
                        || (lonsec != UNKNOWN && (lonsec > 60 || lonsec < 0))) {
                    valid = false;
                }
            }
            return valid;
        }

        public double[] getResult() {
            double latftr = 1.0;
            double lonftr = 1.0;
            if (lat != UNKNOWN_DIR && lat == 's') {
                latftr = -1;
            }
            if (lon != UNKNOWN_DIR && lon == 'w') {
                lonftr = -1;
            }
            return new double[] { calc(latdeg, latmin, latsec, latftr), calc(londeg, lonmin, lonsec, lonftr) };
        }

        private static final double calc(double deg, double min, double sec, double ftr) {
            if (deg == UNKNOWN) {
                deg = 0;
            }
            if (min == UNKNOWN) {
                min = 0;
            }
            if (sec == UNKNOWN) {
                sec = 0;
            }
            min += sec / 60.0;
            if (deg < 0) {
                deg -= min / 60.0;
            } else {
                deg += min / 60.0;
            }
            return deg * ftr;
        }

        @Override
        public String toString() {
            return "latdeg=" + latdeg + ", latmin=" + latmin + ", latsec=" + latsec + ", latNS=" + lat + ", londeg="
                    + londeg + ", lonmin=" + lonmin + ", lonsec=" + lonsec + ", lonEW=" + lon;
        }

        public void setLatdeg(double latdeg) {
            if (this.latdeg == UNKNOWN) {
                this.latdeg = latdeg;
            }
        }

        public void setLatmin(double latmin) {
            if (this.latmin == UNKNOWN) {
                this.latmin = latmin;
            }
        }

        public void setLatsec(double latsec) {
            if (this.latsec == UNKNOWN) {
                this.latsec = latsec;
            }
        }

        public void setLat(char lat) {
            if (this.lat == UNKNOWN) {
                this.lat = lat;
            }
        }

        public void setLondeg(double londeg) {
            if (this.londeg == UNKNOWN) {
                if (londeg > 180) {
                    londeg = londeg - 360;
                }
                this.londeg = londeg;
            }
        }

        public void setLonmin(double lonmin) {
            if (this.lonmin == UNKNOWN) {
                this.lonmin = lonmin;
            }
        }

        public void setLonsec(double lonsec) {
            if (this.lonsec == UNKNOWN) {
                this.lonsec = lonsec;
            }
        }

        public void setLon(char lon) {
            if (this.lon == UNKNOWN) {
                this.lon = lon;
            }
        }

    }
}
