
package com.github.hisahi.u020toolchain.ui; 

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;

/**
 * The Run menu for the emulator UI.
 * 
 * @author hisahi
 */
public class EmuMenuRun extends EmuMenu {

    private static final String QUICK_SAVE_FILE = "state.us";
    MenuItem pause;
    MenuItem hardReset;
    MenuItem saveState;
    MenuItem loadState;
    Menu stateQuick;
    MenuItem saveStateQuick;
    MenuItem loadStateQuick;
            
    /**
     * Initializes a new EmuMenuRun instance.
     * 
     * @param main The main window.
     */
    public EmuMenuRun(EmulatorMain main) {
        super(main);
        this.pause = new MenuItem(I18n.format("menu.run.pause"));
        this.hardReset = new MenuItem(I18n.format("menu.run.reset"));
        this.saveState = new MenuItem(I18n.format("menu.run.savestate"));
        this.loadState = new MenuItem(I18n.format("menu.run.loadstate"));
        this.stateQuick = new Menu(I18n.format("menu.run.choosestatequick"));
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
        menu.getItems().add(hardReset);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(stateQuick);
        menu.getItems().add(saveStateQuick);
        menu.getItems().add(loadStateQuick);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(saveState);
        menu.getItems().add(loadState);
        return menu;
    }
    
    /**
     * Updates the current selected quick save state. This changes the
     * state of check boxes under the menu.
     * 
     * @param state The new selected quick state.
     */
    public void updateQuickSavestate(int state) {
        for (int i = 0; i < 10; ++i) {
            ((CheckMenuItem) this.stateQuick.getItems().get(i)).setSelected(i == state);
        }
    }

    private void addActions() {
        pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (main.cpu.isPaused()) {
                    main.cpu.resume();
                    pause.setText(I18n.format("menu.run.pause"));
                } else {
                    main.cpu.pause();
                    pause.setText(I18n.format("menu.run.resume"));
                }
            }
        });
        hardReset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                main.cpu.reset(true);
            }
        });
        loadState.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                boolean oldPaused = main.cpu.isPaused();
                main.cpu.pause();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.loadstate"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.usv"), "*.usv"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.all"), "*"));
                File file = fileChooser.showOpenDialog(main.mainStage);
                if (file != null) {
                    loadStateFrom(file);
                }
                if (!oldPaused) {
                    main.cpu.resume();
                }
            }
        });
        saveState.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                boolean oldPaused = main.cpu.isPaused();
                main.cpu.pause();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.savestate"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.usv"), "*.usv"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.all"), "*"));
                File file = fileChooser.showSaveDialog(main.mainStage);
                if (file != null) {
                    saveStateTo(file);
                }
                if (!oldPaused) {
                    main.cpu.resume();
                }
            }
        });
        loadStateQuick.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                boolean oldPaused = main.cpu.isPaused();
                main.cpu.pause();
                File f = new File(QUICK_SAVE_FILE + main.getQuickSaveState());
                if (f.isFile()) {
                    loadStateFrom(f);
                }
                if (!oldPaused) {
                    main.cpu.resume();
                }
            }
        });
        saveStateQuick.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                boolean oldPaused = main.cpu.isPaused();
                main.cpu.pause();
                File f = new File(QUICK_SAVE_FILE + main.getQuickSaveState());
                saveStateTo(f);
                if (!oldPaused) {
                    main.cpu.resume();
                }
            }
        });
        for (int i = 0; i < 10; ++i) {
            final int j = i;
            this.stateQuick.getItems().get(i).setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    main.setQuickSaveState(j);
                    for (int i = 0; i < 10; ++i) {
                        ((CheckMenuItem) stateQuick.getItems().get(i)).setSelected(i == j);
                    }
                }
            });
        }
    }
    
    private void loadStateFrom(File file) {
        DataInputStream restoredata = null;
        try (
            ByteArrayOutputStream backupbaos = new ByteArrayOutputStream();
            DataOutputStream backupdata = new DataOutputStream(backupbaos);
            ByteArrayInputStream restorebaos = new ByteArrayInputStream(backupbaos.toByteArray());
            FileInputStream fin = new FileInputStream(file);
            DataInputStream filedata = new DataInputStream(fin);
        ) {
            main.cpu.saveState(backupdata);
            restoredata = new DataInputStream(restorebaos);
            long l = filedata.readLong();
            if (l != 0xA25530323053731AL) {
                throw new IOException("not a save state file");
            }
            main.cpu.restoreState(filedata);
        } catch (IOException ex) {
            Logger.getLogger(EmuMenuRun.class.getName()).log(Level.SEVERE, null, ex);
            if (restoredata != null) {
                try {
                    main.cpu.restoreState(restoredata);
                } catch (Exception ex2) {
                    Logger.getLogger(EmuMenuRun.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            new Alert(AlertType.ERROR, I18n.format("error.savestate"), ButtonType.OK).showAndWait();
            return;
        }
        if (restoredata != null) {
            try {
                restoredata.close();
            } catch (IOException ex) {
            }
        }
    }
    
    private void saveStateTo(File file) {
        FileChannel channel = null;
        try {
            channel = new FileOutputStream(file, false).getChannel();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(baos);
            data.writeLong(0xA25530323053731AL);
            main.cpu.saveState(data);
            channel.write(ByteBuffer.wrap(baos.toByteArray()));
            data.close();
            baos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EmuMenuRun.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EmuMenuRun.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    Logger.getLogger(EmuMenuRun.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
