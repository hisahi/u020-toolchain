
package com.github.hisahi.u020toolchain.ui; 

import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    private TextArea listing;
    
    public EmulatorDisassembler(EmulatorMain main) {
        this.main = main;
        initStage();
    }

    private void initStage() {
        mainStage = new Stage();
        mainScene = new Scene(new VBox(5));
        mainStage.initOwner(main.mainStage);
        mainStage.setTitle(I18n.format("title.disassembler"));
        
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(createHexTab(), createSymbolTab(), createCodeTab());
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
        listing = new TextArea();
        listing.setFont(main.getMonospacedFont());
        listing.setMaxHeight(Double.MAX_VALUE);
        vbox.getChildren().addAll(label, listing);
        VBox.setVgrow(listing, Priority.ALWAYS);
        tab.setContent(vbox);
        return tab;
    }
    
    public void show() {
        if (mainStage.isShowing()) {
            mainStage.toFront();
        } else {
            mainStage.show();
        }
    }
}
