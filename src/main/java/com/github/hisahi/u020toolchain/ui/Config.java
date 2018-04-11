
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.file.ConfigFileHandler;

/**
 * The main interface to access the settings or configuration set by the user.
 * Allows reading and writing flags, loading and saving the configuration
 * and clearing or reseting it.
 * 
 * @author hisahi
 */
public class Config {
    private static ConfigFileHandler filehandler = new ConfigFileHandler();
    
    /**
     * Reloads the configuration.
     */
    public static void load() {
        filehandler.loadConfig();
    }
    
    /**
     * Saves the configuration.
     */
    public static void save() {
        filehandler.saveConfig();
    }
    
    /**
     * Reads a configuration flag.
     * 
     * @param key The configuration key.
     * @param def A default value if the key is not present.
     * @return    The configuration flag or the default value, if the key
     *            is not present.
     */
    public static Object get(String key, Object def) {
        return filehandler.get(key, def);
    }
    
    /**
     * Writes a configuration flag.
     * 
     * @param key The configuration key.
     * @param val The value to be written.
     */
    public static void put(String key, Object val) {
        filehandler.put(key, val);
    }
    
    /**
     * Resets the configuration by clearing it.
     */
    public static void clear() {
        filehandler.clear();
        filehandler.saveConfig();
    }
}
