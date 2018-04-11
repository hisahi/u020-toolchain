
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.file.LangJsonFileLoader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles localizations and translation strings. Use I18n.format to
 * format a translation string.
 * 
 * @author hisahi
 */
public class I18n {
    private static Map<String, String> keys = new HashMap<>();
    private static LangJsonFileLoader loader = new LangJsonFileLoader();
    
    /**
     * Loads the current language to be used for {@link I18n#format} from the given String.
     * 
     * @param string The JSON string to load the language data from.
     */
    public static void loadLanguageFromString(String string) {
        keys = loader.loadFromString(string);
    }
    
    /**
     * Loads the current language to be used for {@link I18n#format} from a given resource file.
     * 
     * @param langCode The code of the language to load.
     */
    public static void loadLanguage(String langCode) {
        InputStream langFile = I18n.class.getResourceAsStream("/lang/" + langCode + ".json");
        if (langFile != null) {
            keys = loader.loadFromStream(langFile);
        }
    }
    
    private static String getString(String key) {
        return keys.getOrDefault(key, key);
    }
    
    /**
     * Formats a localizable string with the given key and format arguments.
     * 
     * @param key  The key of the localizable string.
     * @param args The format arguments.
     * @return     The formatted, translated string.
     */
    public static String format(String key, Object... args) {
        return String.format(getString(key), args);
    }
}
