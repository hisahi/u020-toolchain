
package com.github.hisahi.u020_toolchain.ui; 

import javafx.scene.control.Menu;

public abstract class EmuMenu {
    protected EmulatorMain main;
    public EmuMenu(EmulatorMain main) {
        this.main = main;
    };
    
    public abstract Menu asMenu();
}
