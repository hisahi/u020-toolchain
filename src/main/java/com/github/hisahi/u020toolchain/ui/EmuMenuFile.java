
package com.github.hisahi.u020toolchain.ui; 

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class EmuMenuFile extends EmuMenu {

    MenuItem loadAndRun;
    MenuItem floppy0Insert;
    MenuItem floppy0Eject;
    CheckMenuItem floppy0Wp;
    MenuItem floppy1Insert;
    MenuItem floppy1Eject;
    CheckMenuItem floppy1Wp;
    MenuItem debugger;
    MenuItem exit;
            
    public EmuMenuFile(EmulatorMain main) {
        super(main);
        this.loadAndRun = new MenuItem(I18n.format("menu.file.loadandrun"));
        this.floppy0Insert = new MenuItem(I18n.format("menu.file.floppy0insert"));
        this.floppy0Eject = new MenuItem(I18n.format("menu.file.floppy0eject"));
        this.floppy0Wp = new CheckMenuItem(I18n.format("menu.file.floppy0wp"));
        this.floppy1Insert = new MenuItem(I18n.format("menu.file.floppy1insert"));
        this.floppy1Eject = new MenuItem(I18n.format("menu.file.floppy1eject"));
        this.floppy1Wp = new CheckMenuItem(I18n.format("menu.file.floppy1wp"));
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
        menu.getItems().add(floppy0Wp);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(floppy1Insert);
        menu.getItems().add(floppy1Eject);
        menu.getItems().add(floppy1Wp);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(debugger);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(exit);
        return menu;
    }

    private void addActions() {
        floppy0Wp.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                main.setWriteProtected(0, newValue);
            }
        });
        floppy1Wp.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                main.setWriteProtected(1, newValue);
            }
        });
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
