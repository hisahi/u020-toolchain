
package com.github.hisahi.u020_toolchain.ui;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EmulatorMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    private Stage mainStage;
    private Scene mainScene;
    Canvas screen;
    GraphicsContext ctx;
    EmuMenuFile menuFile;
    EmuMenuEdit menuEdit;
    EmuMenuRun menuRun;
    EmuMenuOptions menuOptions;
    EmuMenuHelp menuHelp;
 
    @Override
    public void start(Stage stage) {
        mainStage = stage;
        stage.setTitle("U020 Toolchain Emulator");
        mainScene = new Scene(new VBox());
        
        I18n.loadLanguage("en_US"); // no language selection support yet
        
        MenuBar menuBar = initializeMenu();
        ((VBox) mainScene.getRoot()).getChildren().addAll(menuBar);
        
        Group screenGroup = new Group();
        this.screen = new Canvas(256, 192);
        this.ctx = this.screen.getGraphicsContext2D();
        setScale(2);
        this.clearScreen();
        screenGroup.getChildren().add(screen);
        ((VBox) mainScene.getRoot()).getChildren().addAll(screenGroup);
        
        stage.setScene(mainScene);
        stage.sizeToScene();
        stage.show();
    }

    private MenuBar initializeMenu() {
        MenuBar menuBar = new MenuBar();
        
        this.menuFile = new EmuMenuFile(this); 
        this.menuEdit = new EmuMenuEdit(this);
        this.menuRun = new EmuMenuRun(this);
        this.menuOptions = new EmuMenuOptions(this);
        this.menuHelp = new EmuMenuHelp(this);
        
        menuBar.getMenus().addAll(menuFile.asMenu(), menuEdit.asMenu(), menuRun.asMenu(), menuOptions.asMenu(), menuHelp.asMenu());
        return menuBar;
    }

    void setScale(int i) {
        this.screen.setScaleX(i);
        this.screen.setScaleY(i);
        mainStage.sizeToScene();
    }

    private void clearScreen() {
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, 256, 192);
    }

    void exitGracefully() {
        Config.save();
        System.exit(0);
    }
}
