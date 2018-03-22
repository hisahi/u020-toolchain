
package com.github.hisahi.u020_toolchain.ui; 

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class EmuMenuEdit extends EmuMenu {

    MenuItem copy;
    MenuItem paste;
    MenuItem assembler;
    MenuItem disassembler;
            
    public EmuMenuEdit(EmulatorMain main) {
        super(main);
        this.copy = new MenuItem(I18n.format("menu.edit.copy"));
        this.paste = new MenuItem(I18n.format("menu.edit.paste"));
        this.assembler = new MenuItem(I18n.format("menu.options.assembler"));
        this.disassembler = new MenuItem(I18n.format("menu.options.disassembler"));
        this.addActions();
    }
    
    @Override
    public Menu asMenu() {
        Menu menu = new Menu(I18n.format("menu.edit"));
        menu.getItems().add(copy);
        menu.getItems().add(paste);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(assembler);
        menu.getItems().add(disassembler);
        return menu;
    }

    private void addActions() {
        
    }

}
