package cn.kk.kkdict.summarization;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cn.kk.kkdict.beans.DictByteBufferRow;
import cn.kk.kkdict.utils.ArrayHelper;
import cn.kk.kkdict.utils.Helper;

public class WikiDictGeoLocationCorrectorTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCorrectGeoLocation() {
    String test1 = "de═Mexiko‹位lat=19.419444 | long=-99.145556 | region=MX}}|lat=20.666111 | long=-103.351944 | region=MX}}|lat=25.682222 | long=-100.311111 | region=MX}}|lat=19.046944 | long=-98.209444 | region=MX}}|lat=19.190278 | long=-96.153333 | region=MX}}|lat=32.530833 | long=-117.02 | region=MX}}|lat=21.1225 | long=-101.688611 | region=MX}}|lat=31.733333 | long=-106.475 | region=MX}}|lat=25.544444 | long=-103.441667 | region=MX}}|lat=22.151111 | long=-100.842778 | region=MX}}|lat=20.981111 | long=-89.616389 | region=MX}}|lat=21.128056 | long=-86.813611 | region=MX}}|lat=17.064722 | long=-96.716389 | region=MX}}|lat=16.851667 | long=-99.909722 | region=MX}}|lat=24.142778 | long=-110.311667 | region=MX}}|lat=32.651667 | long=-115.478889 | region=MX}}|lat=28.632222 | long=-106.070833 | region=MX}}";
    ByteBuffer bb = ByteBuffer.wrap(test1.getBytes(Helper.CHARSET_UTF8));
    DictByteBufferRow row = new DictByteBufferRow();
    row.parseFrom(bb);
    WikiDictGeoLocationCorrector.correctGeoLocation(row);
    System.out.println(ArrayHelper.toString(row.getByteBuffer()));
    // Assert.assertEquals("de═Mexiko‹位19.375,-99.125", ArrayHelper.toString(row.getByteBuffer()));
    Assert.assertEquals("de═Mexiko‹位19.419444,-99.145556", ArrayHelper.toString(row.getByteBuffer()));

    String test2 = "zh═道谷‹位{{coord|38.7|S|272.1|W|globe:Mars_scale:100000|display=title|name=道谷（Dao Vallis）}}";
    bb = ByteBuffer.wrap(test2.getBytes(Helper.CHARSET_UTF8));
    row.parseFrom(bb);
    WikiDictGeoLocationCorrector.correctGeoLocation(row);
    System.out.println(ArrayHelper.toString(row.getByteBuffer()));
    Assert.assertEquals("zh═道谷‹位38.7,-87.9", ArrayHelper.toString(row.getByteBuffer()));

    String test3 = "de═Pinto (Spanien)‹位latitude=40/15|longitude=3/42/00/W";
    bb = ByteBuffer.wrap(test3.getBytes(Helper.CHARSET_UTF8));
    row.parseFrom(bb);
    WikiDictGeoLocationCorrector.correctGeoLocation(row);
    System.out.println(ArrayHelper.toString(row.getByteBuffer()));
    Assert.assertEquals("de═Pinto (Spanien)‹位40.25,3.7", ArrayHelper.toString(row.getByteBuffer()));

    String test4 = "zh═兰辛 (密歇根州)‹位latitude = 42° 44′ 0.6″ N|longitude = 84° 32′ 48.12″ W|latd=42 |latm=44 |lats=0.6 |latNS=N|longd=84 |longm=32 |longs=48.12 |longEW=W";
    bb = ByteBuffer.wrap(test4.getBytes(Helper.CHARSET_UTF8));
    row.parseFrom(bb);
    WikiDictGeoLocationCorrector.correctGeoLocation(row);
    System.out.println(ArrayHelper.toString(row.getByteBuffer()));
    Assert.assertEquals("zh═兰辛 (密歇根州)‹位42.75,84.5", ArrayHelper.toString(bb));

    String test5 = "zh═蒙诺维亚‹位latitude=34°08'39&quot;N|longitude=118°00'07&quot;W";
    bb = ByteBuffer.wrap(test5.getBytes(Helper.CHARSET_UTF8));
    row.parseFrom(bb);
    WikiDictGeoLocationCorrector.correctGeoLocation(row);
    System.out.println(ArrayHelper.toString(row.getByteBuffer()));
    Assert.assertEquals("zh═蒙诺维亚‹位34.125,118.0", ArrayHelper.toString(bb));

  }
}
