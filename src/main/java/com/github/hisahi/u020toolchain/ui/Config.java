
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.file.ConfigFileHandler;

public class Config {
    private static ConfigFileHandler filehandler = new ConfigFileHandler();
    public static void load() {
        filehandler.loadConfig();
    }
    public static void save() {
        filehandler.saveConfig();
    }
    public static Object get(String key, Object def) {
        return filehandler.get(key, def);
    }
    public static void put(String key, Object val) {
        filehandler.put(key, val);
    }
    public static void clear() {
        filehandler.clear();
        filehandler.saveConfig();
    }
}
