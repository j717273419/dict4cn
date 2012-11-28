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
package cn.kk.kkdict.types;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;

import cn.kk.kkdict.beans.ByteArrayPairs;
import cn.kk.kkdict.utils.Helper;

public final class LanguageConstants {
  public static final ByteArrayPairs getLanguageNamesBytes(final Language lng) {

    if (lng == null) {
      return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_ORIGINAL.txt"));
    }
    switch (lng) {
      case EN:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_EN.txt"));
      case RU:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_RU.txt"));
      case PL:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_PL.txt"));
      case JA:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_JA.txt"));
      case KO:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_KO.txt"));
      case ZH:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_ZH.txt"));
      case DE:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_DE.txt"));
      case FR:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_FR.txt"));
      case IT:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_IT.txt"));
      case ES:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_ES.txt"));
      case PT:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_PT.txt"));
      case NL:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_NL.txt"));
      case SV:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_SV.txt"));
      case UK:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_UK.txt"));
      case VI:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_VI.txt"));
      case CA:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_CA.txt"));
      case NO:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_NO.txt"));
      case FI:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_FI.txt"));
      case CS:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_CS.txt"));
      case HU:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_HU.txt"));
      case ID:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_ID.txt"));
      case TR:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_TR.txt"));
      case RO:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_RO.txt"));
      case FA:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_FA.txt"));
      case AR:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_AR.txt"));
      case DA:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_DA.txt"));
      case EO:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_EO.txt"));
      case SR:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_SR.txt"));
      case LT:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_LT.txt"));
      case SK:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_SK.txt"));
      case SL:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_SL.txt"));
      case MS:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_MS.txt"));
      case HE:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_HE.txt"));
      case BG:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_BG.txt"));
      case KK:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_KK.txt"));
      case EU:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_EU.txt"));
      case VO:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_VO.txt"));
      case WAR:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_WAR.txt"));
      case HR:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_HR.txt"));
      case HI:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_HI.txt"));
      case LA:
        return LanguageConstants.createByteArrayPairs(LanguageConstants.getLngProperties("lng2name_LA.txt"));
      default:
        break;
    }
    return null;
  }

  public final static ByteArrayPairs createByteArrayPairs(final Properties lngs) {

    final ByteArrayPairs result = new ByteArrayPairs(lngs.size());
    final Set<String> names = lngs.stringPropertyNames();
    int i = 0;
    for (final String n : names) {
      result.put(i, lngs.getProperty(n).getBytes(Helper.CHARSET_UTF8), n.getBytes(Helper.CHARSET_UTF8));
      i++;
    }
    return result.sort();
  }

  public static final Properties getLngProperties(final String f) {
    Properties props = null;
    try {
      props = new Properties();
      props.load(new InputStreamReader(LanguageConstants.class.getResourceAsStream("/" + f), Helper.CHARSET_UTF8));
    } catch (final IOException e) {
      System.err.println("Failed to load language properties for '" + f + "': " + e);
    }
    return props;
  }

  private static final String[] getWikiLngs() {
    return new String[] { Language.AA.getKey(), Language.AB.getKey(), Language.ACE.getKey(), Language.AF.getKey(), Language.AK.getKey(), Language.ALS.getKey(),
        Language.AM.getKey(), Language.AN.getKey(), Language.ANG.getKey(), Language.AR.getKey(), Language.ARC.getKey(), Language.ARZ.getKey(),
        Language.AS.getKey(), Language.AST.getKey(), Language.AV.getKey(), Language.AY.getKey(), Language.AZ.getKey(), Language.BA.getKey(),
        Language.BAR.getKey(), Language.BAT_SMG.getKey(), Language.BCL.getKey(), Language.BE.getKey(), Language.BE_X_OLD.getKey(), Language.BG.getKey(),
        Language.BH.getKey(), Language.BI.getKey(), Language.BJN.getKey(), Language.BM.getKey(), Language.BN.getKey(), Language.BO.getKey(),
        Language.BPY.getKey(), Language.BR.getKey(), Language.BS.getKey(), Language.BUG.getKey(), Language.BXR.getKey(), Language.CA.getKey(),
        Language.CBK_ZAM.getKey(), Language.CDO.getKey(), Language.CE.getKey(), Language.CEB.getKey(), Language.CH.getKey(), Language.CHO.getKey(),
        Language.CHR.getKey(), Language.CHY.getKey(), Language.CKB.getKey(), Language.CO.getKey(), Language.CR.getKey(), Language.CRH.getKey(),
        Language.CS.getKey(), Language.CSB.getKey(), Language.CU.getKey(), Language.CV.getKey(), Language.CY.getKey(), Language.DA.getKey(),
        Language.DE.getKey(), Language.DIQ.getKey(), Language.DSB.getKey(), Language.DV.getKey(), Language.DZ.getKey(), Language.EE.getKey(),
        Language.EL.getKey(), Language.EML.getKey(), Language.EN.getKey(), Language.EO.getKey(), Language.ES.getKey(), Language.ET.getKey(),
        Language.EU.getKey(), Language.EXT.getKey(), Language.FA.getKey(), Language.FF.getKey(), Language.FI.getKey(), Language.FIU_VRO.getKey(),
        Language.FJ.getKey(), Language.FO.getKey(), Language.FR.getKey(), Language.FRP.getKey(), Language.FRR.getKey(), Language.FUR.getKey(),
        Language.FY.getKey(), Language.GA.getKey(), Language.GAG.getKey(), Language.GAN.getKey(), Language.GD.getKey(), Language.GL.getKey(),
        Language.GLK.getKey(), Language.GN.getKey(), Language.GOT.getKey(), Language.GU.getKey(), Language.GV.getKey(), Language.HA.getKey(),
        Language.HAK.getKey(), Language.HAW.getKey(), Language.HE.getKey(), Language.HI.getKey(), Language.HIF.getKey(), Language.HO.getKey(),
        Language.HR.getKey(), Language.HSB.getKey(), Language.HT.getKey(), Language.HU.getKey(), Language.HY.getKey(), Language.HZ.getKey(),
        Language.IA.getKey(), Language.ID.getKey(), Language.IE.getKey(), Language.IG.getKey(), Language.II.getKey(), Language.IK.getKey(),
        Language.ILO.getKey(), Language.IO.getKey(), Language.IS.getKey(), Language.IT.getKey(), Language.IU.getKey(), Language.JA.getKey(),
        Language.JBO.getKey(), Language.JV.getKey(), Language.KA.getKey(), Language.KAA.getKey(), Language.KAB.getKey(), Language.KBD.getKey(),
        Language.KG.getKey(), Language.KI.getKey(), Language.KJ.getKey(), Language.KK.getKey(), Language.KL.getKey(), Language.KM.getKey(),
        Language.KN.getKey(), Language.KO.getKey(), Language.KOI.getKey(), Language.KR.getKey(), Language.KRC.getKey(), Language.KS.getKey(),
        Language.KSH.getKey(), Language.KU.getKey(), Language.KV.getKey(), Language.KW.getKey(), Language.KY.getKey(), Language.LA.getKey(),
        Language.LAD.getKey(), Language.LB.getKey(), Language.LBE.getKey(), Language.LG.getKey(), Language.LI.getKey(), Language.LIJ.getKey(),
        Language.LMO.getKey(), Language.LN.getKey(), Language.LO.getKey(), Language.LT.getKey(), Language.LTG.getKey(), Language.LV.getKey(),
        Language.MAP_BMS.getKey(), Language.MDF.getKey(), Language.MG.getKey(), Language.MH.getKey(), Language.MHR.getKey(), Language.MI.getKey(),
        Language.MK.getKey(), Language.ML.getKey(), Language.MN.getKey(), Language.MO.getKey(), Language.MR.getKey(), Language.MRJ.getKey(),
        Language.MS.getKey(), Language.MT.getKey(), Language.MUS.getKey(), Language.MWL.getKey(), Language.MY.getKey(), Language.MYV.getKey(),
        Language.MZN.getKey(), Language.NA.getKey(), Language.NAH.getKey(), Language.NAP.getKey(), Language.NDS.getKey(), Language.NDS_NL.getKey(),
        Language.NE.getKey(), Language.NEW.getKey(), Language.NG.getKey(), Language.NL.getKey(), Language.NN.getKey(), Language.NO.getKey(),
        Language.NOV.getKey(), Language.NRM.getKey(), Language.NSO.getKey(), Language.NV.getKey(), Language.NY.getKey(), Language.OC.getKey(),
        Language.OM.getKey(), Language.OR.getKey(), Language.OS.getKey(), Language.PA.getKey(), Language.PAG.getKey(), Language.PAM.getKey(),
        Language.PAP.getKey(), Language.PCD.getKey(), Language.PDC.getKey(), Language.PFL.getKey(), Language.PI.getKey(), Language.PIH.getKey(),
        Language.PL.getKey(), Language.PMS.getKey(), Language.PNB.getKey(), Language.PNT.getKey(), Language.PS.getKey(), Language.PT.getKey(),
        Language.QU.getKey(), Language.RM.getKey(), Language.RMY.getKey(), Language.RN.getKey(), Language.RO.getKey(), Language.ROA_RUP.getKey(),
        Language.ROA_TARA.getKey(), Language.RU.getKey(), Language.RUE.getKey(), Language.RW.getKey(), Language.SA.getKey(), Language.SAH.getKey(),
        Language.SC.getKey(), Language.SCN.getKey(), Language.SCO.getKey(), Language.SD.getKey(), Language.SE.getKey(), Language.SG.getKey(),
        Language.SH.getKey(), Language.SI.getKey(), Language.SIMPLE.getKey(), Language.SK.getKey(), Language.SL.getKey(), Language.SM.getKey(),
        Language.SN.getKey(), Language.SO.getKey(), Language.SQ.getKey(), Language.SR.getKey(), Language.SRN.getKey(), Language.SS.getKey(),
        Language.ST.getKey(), Language.STQ.getKey(), Language.SU.getKey(), Language.SV.getKey(), Language.SW.getKey(), Language.SZL.getKey(),
        Language.TA.getKey(), Language.TE.getKey(), Language.TET.getKey(), Language.TG.getKey(), Language.TH.getKey(), Language.TI.getKey(),
        Language.TK.getKey(), Language.TL.getKey(), Language.TN.getKey(), Language.TO.getKey(), Language.TPI.getKey(), Language.TR.getKey(),
        Language.TS.getKey(), Language.TT.getKey(), Language.TUM.getKey(), Language.TW.getKey(), Language.TY.getKey(), Language.UDM.getKey(),
        Language.UG.getKey(), Language.UK.getKey(), Language.UR.getKey(), Language.UZ.getKey(), Language.VE.getKey(), Language.VEC.getKey(),
        Language.VEP.getKey(), Language.VI.getKey(), Language.VLS.getKey(), Language.VO.getKey(), Language.WA.getKey(), Language.WAR.getKey(),
        Language.WO.getKey(), Language.WUU.getKey(), Language.XAL.getKey(), Language.XH.getKey(), Language.XMF.getKey(), Language.YI.getKey(),
        Language.YO.getKey(), Language.ZA.getKey(), Language.ZEA.getKey(), Language.ZH.getKey(), Language.ZH_CLASSICAL.getKey(), Language.ZH_MIN_NAN.getKey(),
        Language.ZH_YUE.getKey(), Language.ZU.getKey(), };
  }

  public static final String[] KEYS_WIKI;
  static {
    KEYS_WIKI = LanguageConstants.getWikiLngs();
    java.util.Arrays.sort(LanguageConstants.KEYS_WIKI);
  }

  private static final String[] getWiktLngs() {
    return new String[] { Language.AA.getKey(), Language.AB.getKey(), Language.AF.getKey(), Language.AK.getKey(), Language.ALS.getKey(), Language.AM.getKey(),
        Language.AN.getKey(), Language.ANG.getKey(), Language.AR.getKey(), Language.AS.getKey(), Language.AST.getKey(), Language.AV.getKey(),
        Language.AY.getKey(), Language.AZ.getKey(), Language.BE.getKey(), Language.BG.getKey(), Language.BH.getKey(), Language.BI.getKey(),
        Language.BM.getKey(), Language.BN.getKey(), Language.BO.getKey(), Language.BR.getKey(), Language.BS.getKey(), Language.CA.getKey(),
        Language.CH.getKey(), Language.CHR.getKey(), Language.CO.getKey(), Language.CR.getKey(), Language.CS.getKey(), Language.CSB.getKey(),
        Language.CY.getKey(), Language.DA.getKey(), Language.DE.getKey(), Language.DV.getKey(), Language.DZ.getKey(), Language.EL.getKey(),
        Language.EN.getKey(), Language.EO.getKey(), Language.ES.getKey(), Language.ET.getKey(), Language.EU.getKey(), Language.FA.getKey(),
        Language.FI.getKey(), Language.FJ.getKey(), Language.FO.getKey(), Language.FR.getKey(), Language.FY.getKey(), Language.GA.getKey(),
        Language.GD.getKey(), Language.GL.getKey(), Language.GN.getKey(), Language.GU.getKey(), Language.GV.getKey(), Language.HA.getKey(),
        Language.HE.getKey(), Language.HI.getKey(), Language.HR.getKey(), Language.HSB.getKey(), Language.HU.getKey(), Language.HY.getKey(),
        Language.IA.getKey(), Language.ID.getKey(), Language.IE.getKey(), Language.IK.getKey(), Language.IO.getKey(), Language.IS.getKey(),
        Language.IT.getKey(), Language.IU.getKey(), Language.JA.getKey(), Language.JBO.getKey(), Language.JV.getKey(), Language.KA.getKey(),
        Language.KK.getKey(), Language.KL.getKey(), Language.KM.getKey(), Language.KN.getKey(), Language.KO.getKey(), Language.KS.getKey(),
        Language.KU.getKey(), Language.KW.getKey(), Language.KY.getKey(), Language.LA.getKey(), Language.LB.getKey(), Language.LI.getKey(),
        Language.LN.getKey(), Language.LO.getKey(), Language.LT.getKey(), Language.LV.getKey(), Language.MG.getKey(), Language.MH.getKey(),
        Language.MI.getKey(), Language.MK.getKey(), Language.ML.getKey(), Language.MN.getKey(), Language.MO.getKey(), Language.MR.getKey(),
        Language.MS.getKey(), Language.MT.getKey(), Language.MY.getKey(), Language.NA.getKey(), Language.NAH.getKey(), Language.NDS.getKey(),
        Language.NE.getKey(), Language.NL.getKey(), Language.NN.getKey(), Language.NO.getKey(), Language.OC.getKey(), Language.OM.getKey(),
        Language.OR.getKey(), Language.PA.getKey(), Language.PI.getKey(), Language.PL.getKey(), Language.PNB.getKey(), Language.PS.getKey(),
        Language.PT.getKey(), Language.QU.getKey(), Language.RM.getKey(), Language.RN.getKey(), Language.RO.getKey(), Language.ROA_RUP.getKey(),
        Language.RU.getKey(), Language.RW.getKey(), Language.SA.getKey(), Language.SC.getKey(), Language.SCN.getKey(), Language.SD.getKey(),
        Language.SG.getKey(), Language.SH.getKey(), Language.SI.getKey(), Language.SIMPLE.getKey(), Language.SK.getKey(), Language.SL.getKey(),
        Language.SM.getKey(), Language.SN.getKey(), Language.SO.getKey(), Language.SQ.getKey(), Language.SR.getKey(), Language.SS.getKey(),
        Language.ST.getKey(), Language.SU.getKey(), Language.SV.getKey(), Language.SW.getKey(), Language.TA.getKey(), Language.TE.getKey(),
        Language.TG.getKey(), Language.TH.getKey(), Language.TI.getKey(), Language.TK.getKey(), Language.TL.getKey(), Language.TN.getKey(),
        Language.TO.getKey(), Language.TPI.getKey(), Language.TR.getKey(), Language.TS.getKey(), Language.TT.getKey(), Language.TW.getKey(),
        Language.UG.getKey(), Language.UK.getKey(), Language.UR.getKey(), Language.UZ.getKey(), Language.VI.getKey(), Language.VO.getKey(),
        Language.WA.getKey(), Language.WO.getKey(), Language.XH.getKey(), Language.YI.getKey(), Language.YO.getKey(), Language.ZA.getKey(),
        Language.ZH.getKey(), Language.ZH_MIN_NAN.getKey(), Language.ZU.getKey(), };
  }

  public static final String[] KEYS_WIKT;
  static {
    KEYS_WIKT = LanguageConstants.getWiktLngs();
    java.util.Arrays.sort(LanguageConstants.KEYS_WIKT);
  }
}
