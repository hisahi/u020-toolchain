
package com.github.hisahi.u020_toolchain.ui; 

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class EmuMenuHelp extends EmuMenu {

    MenuItem onlineHelp;
    MenuItem about;
            
    public EmuMenuHelp(EmulatorMain main) {
        super(main);
        this.onlineHelp = new MenuItem(I18n.format("menu.help.online"));
        this.about = new MenuItem(I18n.format("menu.help.about"));
        this.addActions();
    }
    
    @Override
    public Menu asMenu() {
        Menu menu = new Menu(I18n.format("menu.help"));
        menu.getItems().add(onlineHelp);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(about);
        return menu;
    }

    private void addActions() {
        
    }

}
