
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.hardware.M35FD;
import java.util.Iterator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmulatorOptions {
    private EmulatorMain main;
    private Stage mainStage;
    private Scene mainScene;
    private Spinner inserteddrives;
    
    public EmulatorOptions(EmulatorMain main) {
        this.main = main;
        initOptionsStage();
    }

    private void initOptionsStage() {
        mainStage = new Stage();
        mainScene = new Scene(new VBox(10));
        mainStage.initOwner(main.mainStage);
        mainStage.setTitle(I18n.format("title.options"));
        
        ((VBox) mainScene.getRoot()).getChildren().addAll(getDriveSetting());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getButtons());
        
        mainStage.setScene(mainScene);
        mainStage.sizeToScene();
    }

    private void saveConfig() {
        Config.put("m35fd_drives", clamp((int) inserteddrives.getValue(), 0, 2));
        reloadConfig();
    }

    void reloadConfig() {
        int m35fd = clamp(tryGetInt("m35fd_drives", 0), 0, 2);
        inserteddrives.getValueFactory().setValue(m35fd);
        Iterator<Hardware> drives = main.cpu.getDevices().iterator();
        while (drives.hasNext()) {
            Hardware hw = drives.next();
            if (hw instanceof M35FD && ((M35FD) hw).getDriveId() >= m35fd) {
                ((M35FD) hw).reset();
                ((M35FD) hw).eject();
                drives.remove();
            }
        }
        for (int i = 0; i < m35fd; ++i) {
            M35FD drive = new M35FD(main.cpu, i);
            main.cpu.getDevices().add(drive);
        }
        main.updateFloppies();
    }

    private HBox getDriveSetting() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.inserteddrives"));
        lbl.setPadding(new Insets(0, 20, 0, 0));
        lbl.setAlignment(Pos.CENTER_LEFT);
        inserteddrives = new Spinner(0, 2, 0);
        inserteddrives.setPrefWidth(64);
        hbox.getChildren().addAll(lbl, inserteddrives);
        return hbox;
    }

    private HBox getButtons() {
        HBox buttonhbox = new HBox(5);
        Button okButton = new Button(I18n.format("options.ok"));
        Button cancelButton = new Button(I18n.format("options.cancel"));
        Button applyButton = new Button(I18n.format("options.apply"));
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveConfig();
                mainStage.close();
            }
        });
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainStage.close();
            }
        });
        applyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveConfig();
            }
        });
        buttonhbox.getChildren().addAll(okButton, cancelButton, applyButton);
        buttonhbox.setAlignment(Pos.BOTTOM_RIGHT);
        return buttonhbox;
    }
    
    public void show() {
        if (mainStage.isShowing()) {
            mainStage.toFront();
        } else {
            mainStage.showAndWait();
        }
    }

    private int clamp(int i, int a, int b) {
        if (i < a) {
            return a;
        } else if (i > b) {
            return b;
        } else {
            return i;
        }
    }

    private int tryGetInt(String c, int i) {
        Object o = Config.get(c, i);
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return i;
        }
    }
    
    
}
