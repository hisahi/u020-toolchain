
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.logic.AssemblyListing;
import com.github.hisahi.u020toolchain.logic.Disassembler;
import com.github.hisahi.u020toolchain.logic.SymbolTableParser;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

public class EmulatorDisassembler {
    private EmulatorMain main;
    private Stage mainStage;
    private Scene mainScene;
    private TextField address;
    private TextArea hexinput;
    private TextArea symboltable;
    private ListView<String> listingArea;
    private TabPane tabPane;
    private Tab resultTab;
    private List<AssemblyListing> listing;
    
    public EmulatorDisassembler(EmulatorMain main) {
        this.main = main;
        this.listing = null;
        initStage();
    }

    private void initStage() {
        mainStage = new Stage();
        mainScene = new Scene(new VBox(5));
        mainStage.initOwner(main.mainStage);
        mainStage.setTitle(I18n.format("title.disassembler"));
        
        tabPane = new TabPane();
        tabPane.getTabs().addAll(createHexTab(), createSymbolTab(), resultTab = createCodeTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        ((VBox) mainScene.getRoot()).getChildren().add(tabPane);
        mainStage.setScene(mainScene);
        mainStage.sizeToScene();
    }
    
    public Tab createHexTab() {
        Tab tab = new Tab();
        tab.setText(I18n.format("disassembler.hex"));
        VBox vbox = new VBox(5);
        Label label = new Label(I18n.format("disassembler.hex.desc"));
        label.setTextAlignment(TextAlignment.JUSTIFY);
        label.setWrapText(true);
        HBox addressbox = new HBox(10);
        address = new TextField();
        address.setFont(main.getMonospacedFont());
        address.setText("0000");
        Label labeladdr = new Label(I18n.format("disassembler.hex.address"));
        labeladdr.setWrapText(true);
        addressbox.getChildren().addAll(labeladdr, address);
        hexinput = new TextArea();
        hexinput.setFont(main.getMonospacedFont());
        hexinput.setMaxHeight(Double.MAX_VALUE);
        hexinput.setWrapText(false);
        Button importbin = new Button(I18n.format("disassembler.hex.import"));
        Button disasm = new Button(I18n.format("disassembler.disassemble"));
        importbin.setMaxWidth(Double.MAX_VALUE);
        disasm.setMaxWidth(Double.MAX_VALUE);
        importbin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.importbin"));
                File file = fileChooser.showOpenDialog(main.mainStage);
                if (file != null) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] f = new byte[Math.min(131072, (int) file.length())];
                        int rb = fis.read(f) & ~1;
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < rb; i += 2) {
                            sb.append(String.format("%02x", f[i]));
                            sb.append(String.format("%02x", f[i + 1]));
                            sb.append(' ');
                        }
                        hexinput.setText(sb.toString().trim());
                    } catch (IOException ex) {
                        Logger.getLogger(EmulatorDisassembler.class.getName()).log(Level.SEVERE, null, ex);
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.fileio"), ButtonType.OK).showAndWait();
                    }
                }
            }
        });
        disasm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listing = null;
                listingArea.getItems().clear();
                Map<Integer, List<String>> labels = new HashMap<>();
                int startAddr;
                try {
                    startAddr = Integer.parseInt(stripHex(address.getText()), 16) & 0xFFFF;
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, I18n.format("error.disasm.invalidaddr"), ButtonType.OK).showAndWait();
                    return;
                }
                int[] dataAreas = new int[65536];
                int[] memory = new int[65536];
                String[] tok = hexinput.getText().trim().split("\\s+");
                if (tok.length > 65536) {
                    new Alert(Alert.AlertType.ERROR, I18n.format("error.disasm.hexlong"), ButtonType.OK).showAndWait();
                    return;
                }
                for (int i = 0; i < tok.length; ++i) {
                    try {
                        memory[(startAddr + i) & 0xFFFF] = Integer.parseInt(stripHex(tok[i]), 16) & 0xFFFF;
                    } catch (NumberFormatException ex) {
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.disasm.invalidhex"), ButtonType.OK).showAndWait();
                        return;
                    }
                }
                try {
                    SymbolTableParser.parse(symboltable.getText(), labels, dataAreas);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                    return;
                }
                try {
                    listing = Disassembler.disassemble(memory, startAddr, startAddr + tok.length, labels, dataAreas);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                    return;
                }
                for (String line: Disassembler.listingToString(listing).split("\n")) {
                    listingArea.getItems().add(line);
                }
                tabPane.getSelectionModel().select(resultTab);
            }
        });
        vbox.getChildren().addAll(label, addressbox, hexinput, importbin, disasm);
        VBox.setVgrow(hexinput, Priority.ALWAYS);
        tab.setContent(vbox);
        return tab;
    }
    
    public Tab createSymbolTab() {
        Tab tab = new Tab();
        tab.setText(I18n.format("disassembler.symbol"));
        VBox vbox = new VBox(5);
        Label label = new Label(I18n.format("disassembler.symbol.desc"));
        label.setTextAlignment(TextAlignment.JUSTIFY);
        label.setWrapText(true);
        symboltable = new TextArea();
        symboltable.setFont(main.getMonospacedFont());
        symboltable.setMaxHeight(Double.MAX_VALUE);
        symboltable.setWrapText(false);
        Button importsym = new Button(I18n.format("disassembler.symbol.import"));
        importsym.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.format("dialog.importbin"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.sym"), "*.sym"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.format("dialog.extension.all"), "*"));
                File file = fileChooser.showOpenDialog(main.mainStage);
                if (file != null) {
                    try {
                        symboltable.setText(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
                    } catch (IOException ex) {
                        Logger.getLogger(EmulatorDisassembler.class.getName()).log(Level.SEVERE, null, ex);
                        new Alert(Alert.AlertType.ERROR, I18n.format("error.fileio"), ButtonType.OK).showAndWait();
                    }
                }
            }
        });
        importsym.setMaxWidth(Double.MAX_VALUE);
        vbox.getChildren().addAll(label, symboltable, importsym);
        VBox.setVgrow(symboltable, Priority.ALWAYS);
        tab.setContent(vbox);
        return tab;
    }
    
    public Tab createCodeTab() {
        Tab tab = new Tab();
        tab.setText(I18n.format("disassembler.code"));
        VBox vbox = new VBox(5);
        Label label = new Label(I18n.format("disassembler.code.desc"));
        label.setTextAlignment(TextAlignment.JUSTIFY);
        label.setWrapText(true);
        listingArea = new ListView<>();
        listingArea.setMaxHeight(Double.MAX_VALUE);
        listingArea.setStyle("-fx-font-family: \"monospace\";");
        Button exportlst = new Button(I18n.format("disassembler.code.export"));
        exportlst.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (listing == null) {
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
                        for (String s: listingArea.getItems()) {
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
        exportlst.setMaxWidth(Double.MAX_VALUE);
        vbox.getChildren().addAll(label, listingArea, exportlst);
        VBox.setVgrow(listingArea, Priority.ALWAYS);
        tab.setContent(vbox);
        return tab;
    }
    
    public String stripHex(String str) {
        if (str.startsWith("0x")) {
            return str.substring(2);
        }
        return str;
    }
    
    public void show() {
        if (mainStage.isShowing()) {
            mainStage.toFront();
        } else {
            hexinput.clear();
            symboltable.clear();
            listingArea.getItems().clear();
            mainStage.show();
        }
    }
}
