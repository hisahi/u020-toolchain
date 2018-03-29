
package com.github.hisahi.u020toolchain.ui; 

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class EmuMenuOptions extends EmuMenu {

    MenuItem options;
            
    public EmuMenuOptions(EmulatorMain main) {
        super(main);
        this.options = new MenuItem(I18n.format("menu.options.options"));
        this.addActions();
    }
    
    @Override
    public Menu asMenu() {
        Menu menu = new Menu(I18n.format("menu.options"));
        menu.getItems().add(options);
        return menu;
    }

    private void addActions() {
        options.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.showOptions();
            }
        });
    }

}
