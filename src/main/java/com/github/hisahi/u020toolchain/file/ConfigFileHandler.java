
package com.github.hisahi.u020toolchain.file; 

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

/**
 * Handles and parses the configuration file (settings.json). The interface
 * provided by this class is used by the Config class, which should then be
 * used by any class that needs to check a configuration flag. 
 * 
 * Loading and saving the file is also handled here.
 * 
 * @author hisahi
 */
public class ConfigFileHandler {
    private static final String CONFIG_PATH = "settings.json";
    private Map<String, Object> map;
    
    /**
     * Reads a configuration flag with the given key, or returns
     * a fallback default value if it does not exist.
     * 
     * @param key  The key.
     * @param def  The default value if the key is not found.
     * @return     The configuration flag, or def if the key is not present.
     */
    public Object get(String key, Object def) {
        return map.getOrDefault(key, def);
    }
    
    /**
     * Writes the specified value a configuration flag under the given key.
     * 
     * @param key  The key.
     * @param what What to store under that key.
     */
    public void put(String key, Object what) {
        map.put(key, what);
    }
    
    /**
     * Resets the configuration by deleting all flags.
     */
    public void clear() {
        map.clear();
    }
    
    /**
     * Loads the configuration from the settings.json file.
     */
    public void loadConfig() {
        try {
            this.map = new HashMap<>();
            String config = String.join("\n", Files.readAllLines(Paths.get(CONFIG_PATH)));
            JSONObject obj = new JSONObject(config);
            for (String k: obj.keySet()) {
                map.put(k, obj.get(k));
            }
        } catch (IOException ex) {
            Logger.getLogger(ConfigFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Saves the configuration into the settings.json file.
     */
    public void saveConfig() {
        try (PrintWriter writer = new PrintWriter(CONFIG_PATH)) {
            writer.write(new JSONObject(map).toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
