
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.logic.Assembler;
import com.github.hisahi.u020toolchain.logic.AssemblerResult;
import com.github.hisahi.u020toolchain.logic.AssemblyListing;
import com.github.hisahi.u020toolchain.logic.Disassembler;
import com.github.hisahi.u020toolchain.logic.SymbolTableParser;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EmulatorAssembler {
    private EmulatorMain main;
    private Stage mainStage;
    private Scene mainScene;
    private TabPane tabPane;
    private Tab listingTab;
    private TextArea sourcecode;
    private TextArea symboltable;
    private ListView<String> listing;
    private AssemblerResult asmres;
    
    public EmulatorAssembler(EmulatorMain main) {
        this.main = main;
        initStage();
    }

    private void initStage() {
        mainStage = new Stage();
        mainScene = new Scene(new VBox(5));
        mainStage.initOwner(main.mainStage);
        mainStage.setTitle(I18n.format("title.assembler"));
        
        tabPane = new TabPane();
        tabPane.getTabs().addAll(createCodeTab(), createSymbolTab(), listingTab = createHexTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        ((VBox) mainScene.getRoot()).getChildren().add(tabPane);
        mainStage.setScene(mainScene);
        mainStage.sizeToScene();
    }
    
    public Tab createCodeTab() {
        Tab tab = new Tab();
        tab.setText(I18n.format("assembler.code"));
        VBox vbox = new VBox(5);
        Label label = new Label(I18n.format("assembler.code.desc"));
        label.setTextAlignment(TextAlignment.JUSTIFY);
        label.setWrapText(true);
        sourcecode = new TextArea();
        sourcecode.setFont(main.getMonospacedFont());
        sourcecode.setMaxHeight(Double.MAX_VALUE);
        sourcecode.setWrapText(false);
        VBox.setVgrow(sourcecode, Priority.ALWAYS);
        Button button = new Button(I18n.format("assembler.assemble"));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                asmres = null;
                symboltable.clear();
                listing.getItems().clear();
                try {
                    asmres = Assembler.assemble(sourcecode.getText());
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                    return;
                }
                symboltable.setText(asmres.getSymbolTable());
                List<AssemblyListing> lst;
                Map<Integer, List<String>> labels = new HashMap<>();
                int[] dataAreas = new int[65536];
                SymbolTableParser.parse(symboltable.getText(), labels, dataAreas);
                lst = Disassembler.disassemble(asmres.getBinary(), 0, 0 + asmres.getBinary().length, labels, dataAreas);
                for (String line: Disassembler.listingToString(lst).split("\n")) {
                    listing.getItems().add(line);
                }
                tabPane.getSelectionModel().select(listingTab);
            }
        });
        vbox.getChildren().addAll(label, sourcecode, button);
        tab.setContent(vbox);
        return tab;
    }
    
    public Tab createSymbolTab() {
        Tab tab = new Tab();
        tab.setText(I18n.format("assembler.symbol"));
        VBox vbox = new VBox(5);
        Label label = new Label(I18n.format("assembler.symbol.desc"));
        label.setTextAlignment(TextAlignment.JUSTIFY);
        label.setWrapText(true);
        symboltable = new TextArea();
        symboltable.setFont(main.getMonospacedFont());
        symboltable.setMaxHeight(Double.MAX_VALUE);
        symboltable.setWrapText(false);
        Button exportsym = new Button(I18n.format("assembler.symbol.export"));
        exportsym.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (asmres == null) {
                    return;
                }
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.exportsym"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.sym"), "*.sym"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.all"), "*"));
                File file = fileChooser.showSaveDialog(main.mainStage);
                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        Files.write(file.toPath(), symboltable.getText().getBytes());
                    } catch (IOException ex) {
                        Logger.getLogger(EmuMenuFile.class.getName()).log(Level.SEVERE, null, ex);
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.fileio"), ButtonType.OK).showAndWait();
                    }
                }
            }
        });
        exportsym.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(symboltable, Priority.ALWAYS);
        vbox.getChildren().addAll(label, symboltable, exportsym);
        tab.setContent(vbox);
        return tab;
    }
    
    public Tab createHexTab() {
        Tab tab = new Tab();
        tab.setText(I18n.format("assembler.hex"));
        VBox vbox = new VBox(5);
        Label label = new Label(I18n.format("assembler.hex.desc"));
        label.setTextAlignment(TextAlignment.JUSTIFY);
        label.setWrapText(true);
        listing = new ListView<>(FXCollections.observableArrayList());
        listing.setMaxHeight(Double.MAX_VALUE);
        listing.setStyle("-fx-font-family: \"monospace\";");
        VBox.setVgrow(listing, Priority.ALWAYS);
        Button exportlst = new Button(I18n.format("assembler.hex.exportlist"));
        Button exportbin = new Button(I18n.format("assembler.hex.exportbin"));
        Button exporthex = new Button(I18n.format("assembler.hex.exporthex"));
        Button instrun = new Button(I18n.format("assembler.hex.run"));
        exportbin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (asmres == null) {
                    return;
                }
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.exportbin"));
                File file = fileChooser.showSaveDialog(main.mainStage);
                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int[] binary = asmres.getBinary();
                        for (int i = 0; i < binary.length; ++i) {
                            fos.write((binary[i] >> 8) & 0xFF);
                            fos.write(binary[i] & 0xFF);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(EmuMenuFile.class.getName()).log(Level.SEVERE, null, ex);
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.fileio"), ButtonType.OK).showAndWait();
                    }
                }
            }
        });
        exporthex.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (asmres == null) {
                    return;
                }
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.exporthex"));
                File file = fileChooser.showSaveDialog(main.mainStage);
                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        DataOutputStream dos = new DataOutputStream(fos);
                        int[] binary = asmres.getBinary();
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                        for (int i = 0; i < binary.length; ++i) {
                            if (i > 0) {
                                bw.write(" ");
                            }
                            bw.write(String.format("%04x", binary[i]));
                        }
                        bw.write(" ");
                        bw.newLine();
                    } catch (IOException ex) {
                        Logger.getLogger(EmuMenuFile.class.getName()).log(Level.SEVERE, null, ex);
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.fileio"), ButtonType.OK).showAndWait();
                    }
                }
            }
        });
        exportlst.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (asmres == null) {
                    return;
                }
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.exportlist"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.lst"), "*.lst"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.all"), "*"));
                File file = fileChooser.showSaveDialog(main.mainStage);
                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                        for (String s: listing.getItems()) {
                            bw.write(s);
                            bw.newLine();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(EmuMenuFile.class.getName()).log(Level.SEVERE, null, ex);
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.fileio"), ButtonType.OK).showAndWait();
                    }
                }
            }
        });
        instrun.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (asmres == null) {
                    return;
                }
                boolean oldPaused = main.cpu.isPaused();
                main.cpuclock.stop();
                main.cpu.reset(true);
                int[] memarr = main.cpu.getMemory().array();
                int[] binary = asmres.getBinary();
                for (int i = 0; i < 65536; ++i) {
                    if (i < binary.length) {
                        memarr[i] = binary[i];
                    } else {
                        memarr[i] = 0;
                    }
                }
                if (oldPaused) {
                    main.cpu.pause();
                }
                main.cpuclock.start();
            }
        });
        exportlst.setMaxWidth(Double.MAX_VALUE);
        exportbin.setMaxWidth(Double.MAX_VALUE);
        exporthex.setMaxWidth(Double.MAX_VALUE);
        instrun.setMaxWidth(Double.MAX_VALUE);
        vbox.getChildren().addAll(label, listing, exportlst, exportbin, exporthex, instrun);
        tab.setContent(vbox);
        return tab;
    }
    
    public void show() {
        if (mainStage.isShowing()) {
            mainStage.toFront();
        } else {
            sourcecode.clear();
            symboltable.clear();
            listing.getItems().clear();
            mainStage.show();
        }
    }

}
