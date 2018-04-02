
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.logic.AssemblyListing;
import com.github.hisahi.u020toolchain.logic.Disassembler;
import com.github.hisahi.u020toolchain.logic.SymbolTableParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class EmulatorDisassembler {
    private EmulatorMain main;
    private Stage mainStage;
    private Scene mainScene;
    private TextField address;
    private TextArea hexinput;
    private TextArea symboltable;
    private TextArea listingArea;
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
        Button importbin = new Button(I18n.format("disassembler.hex.import"));
        Button disasm = new Button(I18n.format("disassembler.disassemble"));
        importbin.setMaxWidth(Double.MAX_VALUE);
        disasm.setMaxWidth(Double.MAX_VALUE);
        
        disasm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listing = null;
                Map<Integer, String> labels = new HashMap<>();
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
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                    return;
                }
                try {
                    listing = Disassembler.disassemble(memory, startAddr, startAddr + tok.length, labels, dataAreas);
                } catch (IllegalArgumentException ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                    return;
                }
                listingArea.setText(Disassembler.listingToString(listing));
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
        vbox.getChildren().addAll(label, symboltable);
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
        listingArea = new TextArea();
        listingArea.setFont(main.getMonospacedFont());
        listingArea.setMaxHeight(Double.MAX_VALUE);
        vbox.getChildren().addAll(label, listingArea);
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
            mainStage.show();
        }
    }
}
