
package com.github.hisahi.u020_toolchain.ui; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Debugger {
    private EmulatorMain main;
    private UCPU16 cpu;
    private Stage debuggerStage;
    private Scene debuggerScene;
    private TextArea log;
    private TextField cmd;
    private String lastCommand;
    public Debugger(EmulatorMain main) {
        this.main = main;
        this.cpu = main.cpu;
        initDebuggerStage();
    }

    private void initDebuggerStage() {
        debuggerStage = new Stage();
        debuggerScene = new Scene(new VBox());
        debuggerStage.initOwner(main.mainStage);
        debuggerStage.setTitle(I18n.format("title.debugger"));
        
        log = new TextArea();
        cmd = new TextField();
        log.setEditable(false);
        cmd.setEditable(true);
        log.setFont(Font.font("Monospaced"));
        cmd.setFont(Font.font("Monospaced"));
        ((VBox) debuggerScene.getRoot()).getChildren().addAll(log, cmd);
        VBox.setVgrow(log, Priority.ALWAYS);
        cmd.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    String line = cmd.getText();
                    cmd.setText("");
                    executeCommand(line);
                }
            }
        });
        
        debuggerStage.setScene(debuggerScene);
    }
    void showDebugger(String reason) {
        if (debuggerStage.isShowing()) {
            log_println(reason);
            log_println(I18n.format("debugger.helpintro"));
            debuggerStage.toFront();
            cmd.requestFocus();
        } else {
            log.clear();
            log_println(reason);
            log_println(I18n.format("debugger.helpintro"));
            debuggerStage.show();
            cmd.requestFocus();
        }
    }
    public void log_print(String text) {
        log.appendText(text);
        log.setScrollTop(Double.MAX_VALUE);
    }
    public void log_println(String text) {
        log_print(text + "\n");
    }
    private void executeCommand(String line) {
        if (line.isEmpty()) {
            if (!lastCommand.isEmpty()) {
                executeCommand(lastCommand);
            }
            return;
        }
        lastCommand = line;
        if (line.equalsIgnoreCase("")) {
            
        } else {
            log_println(I18n.format("debugger.unknown"));
        }
    }
    
}
