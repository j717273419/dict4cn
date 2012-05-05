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
package cn.kk.kkdict.extraction.dict;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.kk.kkdict.Configuration;
import cn.kk.kkdict.Configuration.Source;
import cn.kk.kkdict.types.Category;
import cn.kk.kkdict.types.Language;
import cn.kk.kkdict.types.TranslationSource;
import cn.kk.kkdict.types.Usage;
import cn.kk.kkdict.types.WordType;
import cn.kk.kkdict.utils.Helper;

/**
 * Download: http://sourceforge.net/projects/hispacedic/files/
 * 
 * @author x_kez
 * 
 */
public class EdictZhEsExtractor extends EdictZhDeExtractor {
    public static final String EDICT_FILE = Configuration.IMPORTER_FOLDER_SELECTED_DICTS.getFile(Source.DICT_EDICT,
            "hispacedic.u8");

    public static final String OUT_DIR = Configuration.IMPORTER_FOLDER_EXTRACTED_DICTS.getPath(Source.DICT_EDICT);

    public static final String[] IRRELEVANT_WORDS_STRINGS = {};

    public static void main(String args[]) throws IOException {
        Map<String, EdictCategory> categoriesMap = new HashMap<String, EdictCategory>();
        final CategoryImpl[] values = CategoryImpl.values();
        for (int i = 0; i < values.length; i++) {
            CategoryImpl c = values[i];
            categoriesMap.put(c.getConstantName(), c);
        }
        new EdictZhEsExtractor().extractDict(TranslationSource.EDICT_ZH_ES, EDICT_FILE, Helper.CHARSET_UTF8, OUT_DIR,
                Language.ZH, Language.ES, categoriesMap, IRRELEVANT_WORDS_STRINGS);
    }

    public static enum CategoryImpl implements EdictZhDeExtractor.EdictCategory {
        AA_MM(null, null),
        Anat(null, Category.TYPE_ID + Category.MEDICINE.key),
        Apellido(null, null),
        Biol(null, Category.TYPE_ID + Category.BIOLOGY.key),
        Dep(null, Category.TYPE_ID + Category.SPORTS.key),
        Depor(null, Category.TYPE_ID + Category.SPORTS.key),
        Der(null, Category.TYPE_ID + Category.LAW.key),
        EE_UU(null, null),
        Econ(null, Category.TYPE_ID + Category.ECONOMY.key),
        Filos(null, Category.TYPE_ID + Category.PHILOSOPHY.key),
        Fís(null, Category.TYPE_ID + Category.PHYSICS.key),
        Geogr(null, Category.TYPE_ID + Category.GEOGRAPHY.key),
        Geol(null, Category.TYPE_ID + Category.BIOLOGY.key),
        Hist(null, Category.TYPE_ID + Category.HISTORY.key),
        Inf(null, Category.TYPE_ID + Category.SOFTWARE.key),
        Ling(null, Category.TYPE_ID + Category.LITERATURE.key),
        Mat(null, Category.TYPE_ID + Category.MATHEMATICS.key),
        Med(null, Category.TYPE_ID + Category.MEDICINE.key),
        Mil(null, Category.TYPE_ID + Category.MILITARY.key),
        Mús(null, Category.TYPE_ID + Category.MUSIC.key),
        Pol(null, Category.TYPE_ID + Category.POLITICS.key),
        Quím(null, Category.TYPE_ID + Category.CHEMISTRY.key),
        Rel(null, Category.TYPE_ID + Category.RELIGION.key),
        Relig(null, Category.TYPE_ID + Category.RELIGION.key),
        Tec(null, Category.TYPE_ID + Category.TECHNOLOGY.key),
        Tw(null, Category.TYPE_ID + Category.TIME.key),
        Téc(null, Category.TYPE_ID + Category.INDUSTRY.key),
        abrev(null, WordType.TYPE_ID + WordType.ABBREVIATION.key),
        adj(null, WordType.TYPE_ID + WordType.ADJECTIVE.key),
        adv(null, WordType.TYPE_ID + WordType.ADVERB.key),
        ant(null, null),
        apellido(null, null),
        autobús(null, Category.TYPE_ID + Category.AUTOMOBILE.key),
        caracteres_utilizados_cíclicamente_en_el_calendario_y_los_números_ordinales(null, null),
        constelación(null, Category.TYPE_ID + Category.ASTRONOMY.key),
        de_libro(null, null),
        del_año_lunar(null, Category.TYPE_ID + Category.TIME.key),
        desus(null, null),
        dial(null, Usage.TYPE_ID + Usage.DIALECT.key),
        estante(null, null),
        etc(null, null),
        expr(null, null),
        fig(null, Usage.TYPE_ID + Usage.FIGURATIVE.key),
        interj(null, WordType.TYPE_ID + WordType.INTERJECTION.key),
        lit(null, null),
        n(null, WordType.TYPE_ID + WordType.NOUN.key),
        persona(null, null),
        pop(null, null),
        prep(null, WordType.TYPE_ID + WordType.PREPOSITION.key),
        s(null, null),
        v(null, WordType.TYPE_ID + WordType.VERB.key),
        vulg(null, Usage.TYPE_ID + Usage.VULGAR.key),
        八(null, null),
        asociación(null, Category.TYPE_ID + Category.POLITICS.key),
        deporte(null, null),
        tren(null, Category.TYPE_ID + Category.RAILWAYS.key),
        China(null, Category.TYPE_ID + Category.NAME.key),
        Comer(null, Category.TYPE_ID + Category.ECONOMY.key),
        Edu(null, Category.TYPE_ID + Category.EDUCATION.key),
        Hk(null, Category.TYPE_ID + Category.NAME.key),
        Pekín(null, Category.TYPE_ID + Category.NAME.key),
        Psic(null, Category.TYPE_ID + Category.PSYCHOLOGY.key),
        abr(null, WordType.TYPE_ID + WordType.ABBREVIATION.key),
        de_un_arma(null, null),
        en_Pekín(null, Category.TYPE_ID + Category.NAME.key),
        informal(null, Usage.TYPE_ID + Usage.COLLOQUIAL.key),
        peyor(null, Usage.TYPE_ID + Usage.PEJORATIVE.key), ;

        private final String attrs;
        private final String value;

        CategoryImpl(String value, String attrs) {
            this.value = value;
            this.attrs = attrs;
        }

        @Override
        public String getAttributes() {
            return attrs;
        }

        @Override
        public String getReplacement() {
            return value;
        }

        @Override
        public String getConstantName() {
            return Helper.toConstantName(this.name());
        }
    }
}
