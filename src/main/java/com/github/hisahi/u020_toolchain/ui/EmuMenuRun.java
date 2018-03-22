
package com.github.hisahi.u020_toolchain.ui; 

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class EmuMenuRun extends EmuMenu {

    MenuItem pause;
    MenuItem softReset;
    MenuItem hardReset;
    MenuItem saveState;
    MenuItem loadState;
    Menu stateQuick;
    MenuItem saveStateQuick;
    MenuItem loadStateQuick;
            
    public EmuMenuRun(EmulatorMain main) {
        super(main);
        this.pause = new MenuItem(I18n.format("menu.run.pause"));
        this.softReset = new MenuItem(I18n.format("menu.run.softreset"));
        this.hardReset = new MenuItem(I18n.format("menu.run.hardreset"));
        this.saveState = new MenuItem(I18n.format("menu.run.savestate"));
        this.loadState = new MenuItem(I18n.format("menu.run.loadstate"));
        this.stateQuick = new Menu("menu.run.choosestatequick");
        this.saveStateQuick = new MenuItem(I18n.format("menu.run.savestatequick"));
        this.loadStateQuick = new MenuItem(I18n.format("menu.run.loadstatequick"));
        for (int i = 0; i < 10; ++i) {
            CheckMenuItem check = new CheckMenuItem(String.valueOf(i));
            check.setSelected(false);
            this.stateQuick.getItems().add(check);
        }
        updateQuickSavestate(0);
        this.addActions();
    }
    
    @Override
    public Menu asMenu() {
        Menu menu = new Menu(I18n.format("menu.run"));
        menu.getItems().add(pause);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(softReset);
        menu.getItems().add(hardReset);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(saveState);
        menu.getItems().add(loadState);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(stateQuick);
        menu.getItems().add(saveStateQuick);
        menu.getItems().add(loadStateQuick);
        return menu;
    }
    
    public void updateQuickSavestate(int state) {
        for (int i = 0; i < 10; ++i) {
            ((CheckMenuItem) this.stateQuick.getItems().get(i)).setSelected(i == state);
        }
    }

    private void addActions() {
        
    }

}
