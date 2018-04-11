
package com.github.hisahi.u020toolchain.file;

import com.github.hisahi.u020toolchain.ui.Config;
import org.junit.Test;

/**
 * Unit tests for the configuration. A simple requirement to pass
 * is to not throw any exceptions.
 * 
 * @author hisahi
 */
public class ConfigTest {
    
    public ConfigTest() {
    }

    @Test
    public void loadConfigNoException() {
        Config.load();
    }
}
