
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.hardware.M35FD;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;

public class EmuMenuFile extends EmuMenu {

    MenuItem loadAndRun;
    MenuItem floppy0Insert;
    MenuItem floppy0Eject;
    CheckMenuItem floppy0Wp;
    MenuItem floppy1Insert;
    MenuItem floppy1Eject;
    CheckMenuItem floppy1Wp;
    MenuItem floppyCreate;
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
        this.floppyCreate = new MenuItem(I18n.format("menu.file.floppycreate"));
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
        menu.getItems().add(floppyCreate);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(debugger);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(exit);
        return menu;
    }

    private void addActions() {
        floppy0Insert.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                insertFileToDrive(main.getDrive(0));
            }
        });
        floppy0Eject.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                main.getDrive(0).eject();
                main.updateFloppies();
            }
        });
        floppy0Wp.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                main.setWriteProtected(0, newValue);
            }
        });
        floppy1Insert.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                insertFileToDrive(main.getDrive(1));
            }
        });
        floppy1Eject.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                main.getDrive(1).eject();
                main.updateFloppies();
            }
        });
        floppy1Wp.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                main.setWriteProtected(1, newValue);
            }
        });
        floppyCreate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.newfloppy"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.ufi"), "*.ufi"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.all"), "*"));
                File file = fileChooser.showSaveDialog(main.mainStage);
                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(new byte[2 * M35FD.DISK_SIZE]);
                    } catch (IOException ex) {
                        Logger.getLogger(EmuMenuFile.class.getName()).log(Level.SEVERE, null, ex);
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.floppy.saveio"), ButtonType.OK).showAndWait();
                    }
                }
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
    
    public void insertFileToDrive(M35FD drive) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.format("dialog.openfloppy"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.ufi"), "*.ufi"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.all"), "*"));
        File file = fileChooser.showOpenDialog(main.mainStage);
        if (file != null) {
            long len = file.length();
            if (len > 2 * M35FD.DISK_SIZE) {
                new Alert(Alert.AlertType.ERROR, I18n.format("error.floppy.toolarge"), ButtonType.OK).showAndWait();
                return;
            }
            if (len < 2 * M35FD.DISK_SIZE) {
                new Alert(Alert.AlertType.ERROR, I18n.format("error.floppy.toosmall"), ButtonType.OK).showAndWait();
                return;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] img = new byte[2 * M35FD.DISK_SIZE];
                int rb;
                if ((rb = fis.read(img)) != 2 * M35FD.DISK_SIZE) {
                    throw new IOException("how can I not read this entire file? expected to read " + (2 * M35FD.DISK_SIZE) + " bytes but only got " + rb);
                }
                int[] data = new int[M35FD.DISK_SIZE];
                // conversion: byte (8b) to 16b, big endian
                for (int i = 0; i < data.length; ++i) {
                    data[i] = (img[i << 1] << 8) | img[(i << 1) + 1];
                }
                drive.insert(data);
            } catch (IOException ex) {
                Logger.getLogger(EmuMenuFile.class.getName()).log(Level.SEVERE, null, ex);
                new Alert(Alert.AlertType.ERROR, I18n.format("error.floppy.loadio"), ButtonType.OK).showAndWait();
                return;
            }
            main.updateFloppies();
        }
    }

}
