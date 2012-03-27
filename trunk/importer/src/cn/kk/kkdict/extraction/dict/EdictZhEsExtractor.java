package cn.kk.kkdict.extraction.dict;

import java.io.IOException;
import java.util.Arrays;

import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.utils.Helper;

/**
 * Download: http://sourceforge.net/projects/hispacedic/files/
 * 
 * @author x_kez
 * 
 */
public class EdictZhEsExtractor {
    public static final String EDICT_FILE = Helper.DIR_IN_DICTS+"\\edict\\hispacedic.u8";

    public static final String OUT_DIR = Helper.DIR_OUT_DICTS+"\\edict";

    public static final String[] IRRELEVANT_WORDS_STRINGS = {};

    public static void main(String args[]) throws IOException {
        JECategory[] csValues = JECategory.values();
        String[] validCategoryKeys = new String[csValues.length];
        for (int i = 0; i < csValues.length; i++) {
            JECategory c = csValues[i];
            validCategoryKeys[i] = c.name().toUpperCase();
        }
        Arrays.sort(validCategoryKeys);
        EdictZhDeExtractor.extractDict(TranslationSource.EDICT_ZH_ES, EDICT_FILE, Helper.CHARSET_UTF8, OUT_DIR, Language.ZH,
                Language.ES, validCategoryKeys, IRRELEVANT_WORDS_STRINGS);
    }

    public static enum JECategory {
        AA_MM,
        Anat,
        Apellido,
        Biol,
        Dep,
        Depor,
        Der,
        EE_UU,
        Econ,
        Filos,
        Fís,
        Geogr,
        Geol,
        Hist,
        Inf,
        Ling,
        Mat,
        Med,
        Mil,
        Mús,
        Pol,
        Quím,
        Rel,
        Relig,
        Tec,
        Tw,
        Téc,
        abrev,
        adj,
        adv,
        ant,
        apellido,
        autobús,
        caracteres_utilizados_cíclicamente_en_el_calendario_y_los_números_ordinales,
        constelación,
        de_libro,
        del_año_lunar,
        desus,
        dial,
        estante,
        etc,
        expr,
        fig,
        interj,
        lit,
        n,
        persona,
        pop,
        prep,
        s,
        v,
        vulg,
        八,
        asociación,
        deporte,
        tren,
        China,
        Comer,
        Edu,
        Hk,
        Pekín,
        Psic,
        abr,
        de_un_arma,
        en_Pekín,
        informal,
        peyor,
    }
}
