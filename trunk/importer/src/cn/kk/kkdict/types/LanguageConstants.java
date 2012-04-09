package cn.kk.kkdict.types;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;

import cn.kk.kkdict.beans.ByteArrayPairs;
import cn.kk.kkdict.utils.Helper;

public final class LanguageConstants {
    private static final LanguageConstants INSTANCE = new LanguageConstants();

    public static final ByteArrayPairs getLanguageNamesBytes(final Language lng) {

        if (lng == null) {
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_ORIGINAL.txt"));
        }
        switch (lng) {
        case EN:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_EN.txt"));
        case RU:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_RU.txt"));
        case PL:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_PL.txt"));
        case JA:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_JA.txt"));
        case KO:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_KO.txt"));
        case ZH:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_ZH.txt"));
        case DE:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_DE.txt"));
        case FR:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_FR.txt"));
        case IT:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_IT.txt"));
        case ES:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_ES.txt"));
        case PT:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_PT.txt"));
        case NL:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_NL.txt"));
        case SV:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_SV.txt"));
        case UK:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_UK.txt"));
        case VI:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_VI.txt"));
        case CA:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_CA.txt"));
        case NO:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_NO.txt"));
        case FI:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_FI.txt"));
        case CS:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_CS.txt"));
        case HU:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_HU.txt"));
        case ID:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_ID.txt"));
        case TR:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_TR.txt"));
        case RO:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_RO.txt"));
        case FA:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_FA.txt"));
        case AR:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_AR.txt"));
        case DA:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_DA.txt"));
        case EO:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_EO.txt"));
        case SR:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_SR.txt"));
        case LT:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_LT.txt"));
        case SK:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_SK.txt"));
        case SL:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_SL.txt"));
        case MS:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_MS.txt"));
        case HE:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_HE.txt"));
        case BG:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_BG.txt"));
        case KK:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_KK.txt"));
        case EU:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_EU.txt"));
        case VO:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_VO.txt"));
        case WAR:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_WAR.txt"));
        case HR:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_HR.txt"));
        case HI:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_HI.txt"));
        case LA:
            return createByteArrayPairs(INSTANCE.getLngProperties("lng2name_LA.txt"));
        }
        return null;
    }

    public final static ByteArrayPairs createByteArrayPairs(Properties lngs) {

        ByteArrayPairs result = new ByteArrayPairs(lngs.size());
        Set<String> names = lngs.stringPropertyNames();
        int i = 0;
        for (String n : names) {
            result.put(i, lngs.getProperty(n).getBytes(Helper.CHARSET_UTF8), n.getBytes(Helper.CHARSET_UTF8));
            i++;
        }
        return result.sort();
    }

    public static final Properties getLngProperties(String f) {
        Properties props = null;
        try {
            props = new Properties();
            props.load(new InputStreamReader(LanguageConstants.class.getResourceAsStream("/" + f), Helper.CHARSET_UTF8));
        } catch (IOException e) {
            System.err.println("Failed to load language properties for '" + f + "': " + e);
        }
        return props;
    }

    private static final String[] getWikiLngs() {
        return new String[] { Language.AA.key, Language.AB.key, Language.ACE.key, Language.AF.key, Language.AK.key,
                Language.ALS.key, Language.AM.key, Language.AN.key, Language.ANG.key, Language.AR.key,
                Language.ARC.key, Language.ARZ.key, Language.AS.key, Language.AST.key, Language.AV.key,
                Language.AY.key, Language.AZ.key, Language.BA.key, Language.BAR.key, Language.BAT_SMG.key,
                Language.BCL.key, Language.BE.key, Language.BE_X_OLD.key, Language.BG.key, Language.BH.key,
                Language.BI.key, Language.BJN.key, Language.BM.key, Language.BN.key, Language.BO.key, Language.BPY.key,
                Language.BR.key, Language.BS.key, Language.BUG.key, Language.BXR.key, Language.CA.key,
                Language.CBK_ZAM.key, Language.CDO.key, Language.CE.key, Language.CEB.key, Language.CH.key,
                Language.CHO.key, Language.CHR.key, Language.CHY.key, Language.CKB.key, Language.CO.key,
                Language.CR.key, Language.CRH.key, Language.CS.key, Language.CSB.key, Language.CU.key, Language.CV.key,
                Language.CY.key, Language.DA.key, Language.DE.key, Language.DIQ.key, Language.DSB.key, Language.DV.key,
                Language.DZ.key, Language.EE.key, Language.EL.key, Language.EML.key, Language.EN.key, Language.EO.key,
                Language.ES.key, Language.ET.key, Language.EU.key, Language.EXT.key, Language.FA.key, Language.FF.key,
                Language.FI.key, Language.FIU_VRO.key, Language.FJ.key, Language.FO.key, Language.FR.key,
                Language.FRP.key, Language.FRR.key, Language.FUR.key, Language.FY.key, Language.GA.key,
                Language.GAG.key, Language.GAN.key, Language.GD.key, Language.GL.key, Language.GLK.key,
                Language.GN.key, Language.GOT.key, Language.GU.key, Language.GV.key, Language.HA.key, Language.HAK.key,
                Language.HAW.key, Language.HE.key, Language.HI.key, Language.HIF.key, Language.HO.key, Language.HR.key,
                Language.HSB.key, Language.HT.key, Language.HU.key, Language.HY.key, Language.HZ.key, Language.IA.key,
                Language.ID.key, Language.IE.key, Language.IG.key, Language.II.key, Language.IK.key, Language.ILO.key,
                Language.IO.key, Language.IS.key, Language.IT.key, Language.IU.key, Language.JA.key, Language.JBO.key,
                Language.JV.key, Language.KA.key, Language.KAA.key, Language.KAB.key, Language.KBD.key,
                Language.KG.key, Language.KI.key, Language.KJ.key, Language.KK.key, Language.KL.key, Language.KM.key,
                Language.KN.key, Language.KO.key, Language.KOI.key, Language.KR.key, Language.KRC.key, Language.KS.key,
                Language.KSH.key, Language.KU.key, Language.KV.key, Language.KW.key, Language.KY.key, Language.LA.key,
                Language.LAD.key, Language.LB.key, Language.LBE.key, Language.LG.key, Language.LI.key,
                Language.LIJ.key, Language.LMO.key, Language.LN.key, Language.LO.key, Language.LT.key,
                Language.LTG.key, Language.LV.key, Language.MAP_BMS.key, Language.MDF.key, Language.MG.key,
                Language.MH.key, Language.MHR.key, Language.MI.key, Language.MK.key, Language.ML.key, Language.MN.key,
                Language.MO.key, Language.MR.key, Language.MRJ.key, Language.MS.key, Language.MT.key, Language.MUS.key,
                Language.MWL.key, Language.MY.key, Language.MYV.key, Language.MZN.key, Language.NA.key,
                Language.NAH.key, Language.NAP.key, Language.NDS.key, Language.NDS_NL.key, Language.NE.key,
                Language.NEW.key, Language.NG.key, Language.NL.key, Language.NN.key, Language.NO.key, Language.NOV.key,
                Language.NRM.key, Language.NSO.key, Language.NV.key, Language.NY.key, Language.OC.key, Language.OM.key,
                Language.OR.key, Language.OS.key, Language.PA.key, Language.PAG.key, Language.PAM.key,
                Language.PAP.key, Language.PCD.key, Language.PDC.key, Language.PFL.key, Language.PI.key,
                Language.PIH.key, Language.PL.key, Language.PMS.key, Language.PNB.key, Language.PNT.key,
                Language.PS.key, Language.PT.key, Language.QU.key, Language.RM.key, Language.RMY.key, Language.RN.key,
                Language.RO.key, Language.ROA_RUP.key, Language.ROA_TARA.key, Language.RU.key, Language.RUE.key,
                Language.RW.key, Language.SA.key, Language.SAH.key, Language.SC.key, Language.SCN.key,
                Language.SCO.key, Language.SD.key, Language.SE.key, Language.SG.key, Language.SH.key, Language.SI.key,
                Language.SIMPLE.key, Language.SK.key, Language.SL.key, Language.SM.key, Language.SN.key,
                Language.SO.key, Language.SQ.key, Language.SR.key, Language.SRN.key, Language.SS.key, Language.ST.key,
                Language.STQ.key, Language.SU.key, Language.SV.key, Language.SW.key, Language.SZL.key, Language.TA.key,
                Language.TE.key, Language.TET.key, Language.TG.key, Language.TH.key, Language.TI.key, Language.TK.key,
                Language.TL.key, Language.TN.key, Language.TO.key, Language.TPI.key, Language.TR.key, Language.TS.key,
                Language.TT.key, Language.TUM.key, Language.TW.key, Language.TY.key, Language.UDM.key, Language.UG.key,
                Language.UK.key, Language.UR.key, Language.UZ.key, Language.VE.key, Language.VEC.key, Language.VEP.key,
                Language.VI.key, Language.VLS.key, Language.VO.key, Language.WA.key, Language.WAR.key, Language.WO.key,
                Language.WUU.key, Language.XAL.key, Language.XH.key, Language.XMF.key, Language.YI.key,
                Language.YO.key, Language.ZA.key, Language.ZEA.key, Language.ZH.key, Language.ZH_CLASSICAL.key,
                Language.ZH_MIN_NAN.key, Language.ZH_YUE.key, Language.ZU.key, };
    }

    public static final String[] KEYS_WIKI;
    static {
        KEYS_WIKI = getWikiLngs();
        java.util.Arrays.sort(KEYS_WIKI);
    }

    private static final String[] getWiktLngs() {
        return new String[] { Language.AA.key, Language.AB.key, Language.AF.key, Language.AK.key, Language.ALS.key,
                Language.AM.key, Language.AN.key, Language.ANG.key, Language.AR.key, Language.AS.key, Language.AST.key,
                Language.AV.key, Language.AY.key, Language.AZ.key, Language.BE.key, Language.BG.key, Language.BH.key,
                Language.BI.key, Language.BM.key, Language.BN.key, Language.BO.key, Language.BR.key, Language.BS.key,
                Language.CA.key, Language.CH.key, Language.CHR.key, Language.CO.key, Language.CR.key, Language.CS.key,
                Language.CSB.key, Language.CY.key, Language.DA.key, Language.DE.key, Language.DV.key, Language.DZ.key,
                Language.EL.key, Language.EN.key, Language.EO.key, Language.ES.key, Language.ET.key, Language.EU.key,
                Language.FA.key, Language.FI.key, Language.FJ.key, Language.FO.key, Language.FR.key, Language.FY.key,
                Language.GA.key, Language.GD.key, Language.GL.key, Language.GN.key, Language.GU.key, Language.GV.key,
                Language.HA.key, Language.HE.key, Language.HI.key, Language.HR.key, Language.HSB.key, Language.HU.key,
                Language.HY.key, Language.IA.key, Language.ID.key, Language.IE.key, Language.IK.key, Language.IO.key,
                Language.IS.key, Language.IT.key, Language.IU.key, Language.JA.key, Language.JBO.key, Language.JV.key,
                Language.KA.key, Language.KK.key, Language.KL.key, Language.KM.key, Language.KN.key, Language.KO.key,
                Language.KS.key, Language.KU.key, Language.KW.key, Language.KY.key, Language.LA.key, Language.LB.key,
                Language.LI.key, Language.LN.key, Language.LO.key, Language.LT.key, Language.LV.key, Language.MG.key,
                Language.MH.key, Language.MI.key, Language.MK.key, Language.ML.key, Language.MN.key, Language.MO.key,
                Language.MR.key, Language.MS.key, Language.MT.key, Language.MY.key, Language.NA.key, Language.NAH.key,
                Language.NDS.key, Language.NE.key, Language.NL.key, Language.NN.key, Language.NO.key, Language.OC.key,
                Language.OM.key, Language.OR.key, Language.PA.key, Language.PI.key, Language.PL.key, Language.PNB.key,
                Language.PS.key, Language.PT.key, Language.QU.key, Language.RM.key, Language.RN.key, Language.RO.key,
                Language.ROA_RUP.key, Language.RU.key, Language.RW.key, Language.SA.key, Language.SC.key,
                Language.SCN.key, Language.SD.key, Language.SG.key, Language.SH.key, Language.SI.key,
                Language.SIMPLE.key, Language.SK.key, Language.SL.key, Language.SM.key, Language.SN.key,
                Language.SO.key, Language.SQ.key, Language.SR.key, Language.SS.key, Language.ST.key, Language.SU.key,
                Language.SV.key, Language.SW.key, Language.TA.key, Language.TE.key, Language.TG.key, Language.TH.key,
                Language.TI.key, Language.TK.key, Language.TL.key, Language.TN.key, Language.TO.key, Language.TPI.key,
                Language.TR.key, Language.TS.key, Language.TT.key, Language.TW.key, Language.UG.key, Language.UK.key,
                Language.UR.key, Language.UZ.key, Language.VI.key, Language.VO.key, Language.WA.key, Language.WO.key,
                Language.XH.key, Language.YI.key, Language.YO.key, Language.ZA.key, Language.ZH.key,
                Language.ZH_MIN_NAN.key, Language.ZU.key, };
    }

    public static final String[] KEYS_WIKT;
    static {
        KEYS_WIKT = getWiktLngs();
        java.util.Arrays.sort(KEYS_WIKT);
    }
}
