
package com.github.hisahi.u020_toolchain.ui;

import org.junit.Test;
import static org.junit.Assert.*;

public class I18nTest {
    
    public I18nTest() {
    }
    
    @Test
    public void i18nCanLoadFromString() {
        I18n.loadLanguageFromString("{ \"test.language\": \"Hello, World!\" }");
        assertEquals("Hello, World!", I18n.format("test.language"));
    }
    
    @Test
    public void i18nCanLoadFromFile() {
        I18n.loadLanguage("en_US");
        assertEquals("File", I18n.format("menu.file"));
    }
    
    @Test
    public void i18nCanLoadFromStringAndFormat() {
        I18n.loadLanguageFromString("{ \"test.languagefmt\": \"With %1$d spices and %2$s\" }");
        assertEquals("With 500 spices and condiments", I18n.format("test.languagefmt", 500, "condiments"));
    }
}
