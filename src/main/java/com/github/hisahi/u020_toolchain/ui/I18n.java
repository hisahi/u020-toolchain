
package com.github.hisahi.u020_toolchain.ui; 

import com.github.hisahi.u020_toolchain.file.LangJsonFileLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class I18n {
    private static Map<String, String> keys = new HashMap<>();
    private static LangJsonFileLoader loader = new LangJsonFileLoader();
    public static void loadLanguageFromString(String string) {
        keys = loader.loadFromString(string);
    }
    public static void loadLanguage(String langCode) {
        InputStream langFile = I18n.class.getResourceAsStream("/lang/" + langCode + ".json");
        if (langFile != null) {
            keys = loader.loadFromStream(langFile);
        }
    }
    private static String getString(String key) {
        return keys.getOrDefault(key, key);
    }
    public static String format(String key, Object... args) {
        return String.format(getString(key), args);
    }
}
