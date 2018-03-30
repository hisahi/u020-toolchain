
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

public class EmulatorAssembler {
    private EmulatorMain main;
    private Stage mainStage;
    private Scene mainScene;
    private TextArea sourcecode;
    private TextArea symboltable;
    private TextArea listing;
    
    public EmulatorAssembler(EmulatorMain main) {
        this.main = main;
        initStage();
    }

    private void initStage() {
        mainStage = new Stage();
        mainScene = new Scene(new VBox(5));
        mainStage.initOwner(main.mainStage);
        mainStage.setTitle(I18n.format("title.assembler"));
        
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(createCodeTab(), createSymbolTab(), createHexTab());
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
        VBox.setVgrow(sourcecode, Priority.ALWAYS);
        Button button = new Button(I18n.format("assembler.assemble"));
        button.setMaxWidth(Double.MAX_VALUE);
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
        VBox.setVgrow(symboltable, Priority.ALWAYS);
        vbox.getChildren().addAll(label, symboltable);
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
        listing = new TextArea();
        listing.setFont(main.getMonospacedFont());
        listing.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(listing, Priority.ALWAYS);
        Button exporthex = new Button(I18n.format("assembler.hex.exporthex"));
        Button exportbin = new Button(I18n.format("assembler.hex.exportbin"));
        exporthex.setMaxWidth(Double.MAX_VALUE);
        exportbin.setMaxWidth(Double.MAX_VALUE);
        vbox.getChildren().addAll(label, listing, exporthex, exportbin);
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
