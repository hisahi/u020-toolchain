
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.hardware.M35FD;
import com.github.hisahi.u020toolchain.hardware.UNAC810;
import com.github.hisahi.u020toolchain.hardware.UNMS001;
import java.util.Iterator;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class EmulatorOptions {
    private EmulatorMain main;
    private Stage mainStage;
    private Scene mainScene;
    private Spinner inserteddrives;
    private ComboBox displayscale;
    private ComboBox cpuspeed;
    private Slider volumeslider;
    private CheckBox hidecursor;
    private CheckBox pauseinactive;
    private CheckBox unms001;
    private CheckBox unac810;
    
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
        ((VBox) mainScene.getRoot()).getChildren().addAll(getMouseSetting());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getSoundSetting());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getResetWarning());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getHideCursor());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getPauseIfInactive());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getDisplayScale());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getEmulationSpeed());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getVolumeSetting());
        ((VBox) mainScene.getRoot()).getChildren().addAll(getButtons());
        
        mainStage.setScene(mainScene);
        mainStage.sizeToScene();
    }

    private void saveConfig() {
        Config.put("m35fd_drives", clamp((int) inserteddrives.getValue(), 0, 2));
        Config.put("displayscale", clamp((int) displayscale.getSelectionModel().getSelectedIndex() + 1, 1, 4));
        Config.put("cpuhz", indexToCpuSpeed(cpuspeed.getSelectionModel().getSelectedIndex()));
        Config.put("hidecursor", hidecursor.isSelected());
        Config.put("pauseinactive", pauseinactive.isSelected());
        Config.put("volume", volumeslider.getValue() * 0.01);
        Config.put("unms001", unms001.isSelected());
        Config.put("unac810", unac810.isSelected());
        reloadConfig();
    }

    void reloadConfig() {
        boolean hwchanged = false;
        int m35fdraw = clamp(tryGetInt("m35fd_drives", 0), 0, 2);
        boolean m35fd0 = m35fdraw >= 1;
        boolean m35fd1 = m35fdraw >= 2;
        boolean m35fd0now = false;
        boolean m35fd1now = false;
        boolean unms001 = tryGetBoolean("unms001", false);
        boolean unac810 = tryGetBoolean("unac810", false);
        int scale = clamp(tryGetInt("displayscale", 2), 1, 4);
        int speed = clamp(tryGetInt("cpuhz", 200), 50, 800);
        boolean hide = tryGetBoolean("hidecursor", false);
        boolean pause = tryGetBoolean("pauseinactive", false);
        double volume = clamp(tryGetDouble("volume", 1.0), 0.0, 1.0);
        inserteddrives.getValueFactory().setValue(m35fdraw);
        displayscale.getSelectionModel().select(scale - 1);
        cpuspeed.getSelectionModel().select(cpuSpeedToIndex(speed));
        hidecursor.setSelected(hide);
        pauseinactive.setSelected(pause);
        volumeslider.setValue((int) (volume * 100.0));
        this.unms001.setSelected(unms001);
        this.unac810.setSelected(unac810);
        boolean unms001now = false;
        boolean unac810now = false;
        Iterator<Hardware> hwiter = main.cpu.getDevices().iterator();
        while (hwiter.hasNext()) {
            Hardware hw = hwiter.next();
            if (hw instanceof M35FD) {
                int did = ((M35FD) hw).getDriveId();
                if (did == 0) {
                    if (m35fd0) {
                        m35fd0now = true;
                    } else {
                        ((M35FD) hw).reset();
                        ((M35FD) hw).eject();
                        hwchanged = true;
                        hwiter.remove();
                    }
                } else if (did == 1) {
                    if (m35fd1) {
                        m35fd1now = true;
                    } else {
                        ((M35FD) hw).reset();
                        ((M35FD) hw).eject();
                        hwchanged = true;
                        hwiter.remove();
                    }
                }
            } else if (hw instanceof UNMS001) {
                if (unms001) {
                    unms001now = true;
                } else {
                    hw.reset();
                    hwiter.remove();
                    hwchanged = true;
                    main.unms001 = null;
                }
            } else if (hw instanceof UNAC810) {
                if (unac810) {
                    unac810now = true;
                } else {
                    hw.reset();
                    hwiter.remove();
                    hwchanged = true;
                    main.unac810 = null;
                }
            }
        }
        if (m35fd0 && !m35fd0now) {
            main.cpu.addDevice(new M35FD(main.cpu, 0));
            hwchanged = true;
        }
        if (m35fd1 && !m35fd1now) {
            main.cpu.addDevice(new M35FD(main.cpu, 1));
            hwchanged = true;
        }
        if (unms001 && !unms001now) {
            main.cpu.addDevice(main.unms001 = new UNMS001(main.cpu));
            hwchanged = true;
        }
        if (unac810 && !unac810now) {
            main.cpu.addDevice(main.unac810 = new UNAC810(main.cpu));
            hwchanged = true;
        }
        if (main.unac810 != null) {
            main.unac810.setVolume(volume);
            main.unac810.setSpeed(speed);
        }
        int oldscale = (int) main.screen.getScaleX();
        if (oldscale != scale) {
            main.screen.setScaleX(scale);
            main.screen.setScaleY(scale);
            main.mainStage.sizeToScene();
        }
        main.cpuclock.setSpeed(speed * 10000);
        main.shouldHideCursor = hide;
        main.pauseIfInactive = pause;
        main.updateFloppies();
        if (hwchanged) {
            main.cpu.reset(true, false);
        }
    }

    private HBox getHideCursor() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.hidecursor"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        hidecursor = new CheckBox();
        hbox.getChildren().addAll(lbl, space, hidecursor);
        return hbox;
    }

    private HBox getPauseIfInactive() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.pauseinactive"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        pauseinactive = new CheckBox();
        hbox.getChildren().addAll(lbl, space, pauseinactive);
        return hbox;
    }

    private HBox getDriveSetting() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.inserteddrives"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        inserteddrives = new Spinner(0, 2, 0);
        inserteddrives.setPrefWidth(64);
        hbox.getChildren().addAll(lbl, space, inserteddrives);
        return hbox;
    }

    private HBox getMouseSetting() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.unms001"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        unms001 = new CheckBox();
        hbox.getChildren().addAll(lbl, space, unms001);
        return hbox;
    }

    private HBox getSoundSetting() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.unac810"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        unac810 = new CheckBox();
        hbox.getChildren().addAll(lbl, space, unac810);
        return hbox;
    }

    private HBox getResetWarning() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.resetwarning"));
        lbl.setFont(Font.font(lbl.getFont().getFamily(), FontWeight.EXTRA_BOLD, lbl.getFont().getSize()));
        lbl.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(lbl);
        return hbox;
    }

    private HBox getEmulationSpeed() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.cpuspeed"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        cpuspeed = new ComboBox(FXCollections.observableArrayList(
                            I18n.format("options.cpuspeed.25"),
                            I18n.format("options.cpuspeed.5"),
                            I18n.format("options.cpuspeed1"),
                            I18n.format("options.cpuspeed2"),
                            I18n.format("options.cpuspeed4")));
        hbox.getChildren().addAll(lbl, space, cpuspeed);
        return hbox;
    }
    
    private HBox getVolumeSetting() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.volume"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        volumeslider = new Slider();
        volumeslider.setMin(0);
        volumeslider.setMax(100);
        volumeslider.setShowTickLabels(true);
        volumeslider.setShowTickMarks(true);
        volumeslider.setMajorTickUnit(25);
        volumeslider.setMinorTickCount(5);
        volumeslider.setBlockIncrement(2);
        hbox.getChildren().addAll(lbl, space, volumeslider);
        return hbox;
    }

    private HBox getDisplayScale() {
        HBox hbox = new HBox();
        Label lbl = new Label(I18n.format("options.displayscale"));
        lbl.setAlignment(Pos.CENTER_LEFT);
        final Pane space = new Pane();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinSize(10, 1);
        displayscale = new ComboBox(FXCollections.observableArrayList(
                            I18n.format("options.displayscale1"),
                            I18n.format("options.displayscale2"),
                            I18n.format("options.displayscale3"),
                            I18n.format("options.displayscale4")));
        hbox.getChildren().addAll(lbl, space, displayscale);
        return hbox;
    }

    private HBox getButtons() {
        HBox buttonhbox = new HBox(5);
        Button okButton = new Button(I18n.format("options.ok"));
        Button cancelButton = new Button(I18n.format("options.cancel"));
        Button applyButton = new Button(I18n.format("options.apply"));
        Button resetButton = new Button(I18n.format("options.reset"));
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
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert confirm = new Alert(AlertType.CONFIRMATION);
                confirm.setContentText(I18n.format("options.resetsure"));
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() == ButtonType.OK) {
                    Config.clear();
                    reloadConfig();
                    main.cpu.pause();
                }
            }
        });
        buttonhbox.getChildren().addAll(resetButton, okButton, cancelButton, applyButton);
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

    private int cpuSpeedToIndex(int speed) {
        switch (speed) {
            case 50:
                return 0;
            case 100:
                return 1;
            case 200:
                return 2;
            case 400:
                return 3;
            case 800:
                return 4;
            default:
                return 2;
        }
    }

    private int indexToCpuSpeed(int index) {
        switch (index) {
            case 0:
                return 50;
            case 1:
                return 100;
            case 2:
                return 200;
            case 3:
                return 400;
            case 4:
                return 800;
            default:
                return 200;
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

    private double clamp(double i, double a, double b) {
        if (i < a) {
            return a;
        } else if (i > b) {
            return b;
        } else {
            return i;
        }
    }

    private boolean tryGetBoolean(String c, boolean b) {
        Object o = Config.get(c, b);
        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        } else {
            return b;
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

    private double tryGetDouble(String c, double d) {
        Object o = Config.get(c, d);
        if (o instanceof Double) {
            return ((Double) o).doubleValue();
        } else {
            return d;
        }
    }
    
    
}
