
package com.github.hisahi.u020toolchain.file;

import com.github.hisahi.u020toolchain.ui.I18n;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class LangJsonFileLoader {
    public Map<String, String> loadFromString(String s) {
        Map<String, String> keys = new HashMap<>();
        JSONObject langKeys = new JSONObject(s);
        for (String key: langKeys.keySet()) {
            keys.put(key, langKeys.getString(key));
        }
        return keys;
    }
    public Map<String, String> loadFromStream(InputStream f) {
        if (f != null) {
            BufferedReader reader = null;
            try {
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(f, "UTF-8"));
                String buffer;
                while ((buffer = reader.readLine()) != null) {
                    sb.append(buffer);
                }
                return loadFromString(sb.toString());
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(I18n.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(I18n.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(I18n.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return new HashMap<>();
    }
}
