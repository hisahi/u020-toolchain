/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hisahi.u020toolchain.file;

import com.github.hisahi.u020toolchain.ui.Config;
import org.junit.Test;

/**
 *
 * @author hopea
 */
public class ConfigTest {
    
    public ConfigTest() {
    }

    @Test
    public void loadConfigNoException() {
        Config.load();
    }
}
