
package com.github.hisahi.u020_toolchain.ui; 

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class EmuMenuFile extends EmuMenu {

    MenuItem loadAndRun;
    MenuItem floppy0Insert;
    MenuItem floppy0Eject;
    MenuItem floppy1Insert;
    MenuItem floppy1Eject;
    MenuItem debugger;
    MenuItem exit;
            
    public EmuMenuFile(EmulatorMain main) {
        super(main);
        this.loadAndRun = new MenuItem(I18n.format("menu.file.loadandrun"));
        this.floppy0Insert = new MenuItem(I18n.format("menu.file.floppy0insert"));
        this.floppy0Eject = new MenuItem(I18n.format("menu.file.floppy0eject"));
        this.floppy1Insert = new MenuItem(I18n.format("menu.file.floppy1insert"));
        this.floppy1Eject = new MenuItem(I18n.format("menu.file.floppy1eject"));
        this.debugger = new MenuItem(I18n.format("menu.file.debugger"));
        this.exit = new MenuItem(I18n.format("menu.file.exit"));
        this.addActions();
    }
    
    @Override
    public Menu asMenu() {
        Menu menu = new Menu(I18n.format("menu.file"));
        menu.getItems().add(loadAndRun);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(floppy0Insert);
        menu.getItems().add(floppy0Eject);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(floppy1Insert);
        menu.getItems().add(floppy1Eject);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(debugger);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(exit);
        return menu;
    }

    private void addActions() {
        debugger.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                main.showDebugger("");
            }
        });
        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                main.exitGracefully();
            }
        });
    }

}
