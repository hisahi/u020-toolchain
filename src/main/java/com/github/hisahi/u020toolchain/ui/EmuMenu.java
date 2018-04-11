
package com.github.hisahi.u020toolchain.ui; 

import javafx.scene.control.Menu;

/**
 * An abstract class for all of the menus used by the emulator UI.
 * 
 * @author hisahi
 */
public abstract class EmuMenu {
    protected EmulatorMain main;
    
    /**
     * Initializes a new EmuMenu instance.
     * 
     * @param main The main window.
     */
    public EmuMenu(EmulatorMain main) {
        this.main = main;
    };
    
    /**
     * Returns the menu as a JavaFX Menu.
     * 
     * @return The menu as a JavaFX Menu.   
     */
    public abstract Menu asMenu();
}
