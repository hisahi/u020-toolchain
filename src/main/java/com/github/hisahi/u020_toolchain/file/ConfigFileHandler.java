
package com.github.hisahi.u020_toolchain.file; 

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class ConfigFileHandler {
    private static final String CONFIG_PATH = "settings.json";
    private Map<String, Object> map;
    public Object get(String key, Object def) {
        return map.getOrDefault(key, def);
    }
    public void put(String key, Object what) {
        map.put(key, what);
    }
    public void loadConfig() {
        try {
            this.map = new HashMap<>();
            String config = String.join("\n", Files.readAllLines(Paths.get(CONFIG_PATH)));
            JSONObject obj = new JSONObject(config);
            for (String k: obj.keySet())
                map.put(k, obj.get(k));
        } catch (IOException ex) {
            Logger.getLogger(ConfigFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void saveConfig() {
        try (PrintWriter writer = new PrintWriter(CONFIG_PATH)) {
            writer.write(new JSONObject(map).toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
