
package com.github.hisahi.u020toolchain.ui; 

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
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
        onlineHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    URI page = new URI("https://github.com/hisahi/u020-toolchain/tree/master/doc");
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(page);
                    } else {
                        new Alert(AlertType.ERROR, "Cannot open in default browser", ButtonType.OK).show();
                    }
                } catch (URISyntaxException ex) {
                    Logger.getLogger(EmuMenuHelp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    new Alert(AlertType.ERROR, "Cannot open in default browser", ButtonType.OK).show();
                }
            }
        });
        about.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                new Alert(AlertType.INFORMATION, "U020Emu " + EmulatorMain.VERSION + " by Sampo Hippeläinen / hisahi\n\nhttps://github.com/hisahi/u020-toolchain", ButtonType.OK).show();
            }
        });
    }

}
