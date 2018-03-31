
package com.github.hisahi.u020toolchain.ui; 

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        this.assembler = new MenuItem(I18n.format("menu.edit.assembler"));
        this.disassembler = new MenuItem(I18n.format("menu.edit.disassembler"));
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
        copy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String data = main.uncd321.copyScreenBuffer();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), null);
            }
        });
        paste.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    main.tryPaste(data);
                } catch (UnsupportedFlavorException ex) {
                    Logger.getLogger(EmuMenuEdit.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(EmuMenuEdit.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        });
        assembler.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                main.asmwnd.show();
            }
        });
        disassembler.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                main.disasmwnd.show();
            }
        });
    }

}
