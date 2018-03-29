
package com.github.hisahi.u020toolchain.ui;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.hardware.Clock;
import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.hardware.Keyboard;
import com.github.hisahi.u020toolchain.hardware.M35FD;
import com.github.hisahi.u020toolchain.hardware.UNCD321;
import com.github.hisahi.u020toolchain.hardware.UNEM192;
import com.github.hisahi.u020toolchain.hardware.UNTM200;
import com.github.hisahi.u020toolchain.logic.HighResolutionTimer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class EmulatorMain extends Application {

    public static final String VERSION = "v0.2";
    public static final int CPU_HZ = 2000000;
    public static void main(String[] args) {
        launch(args);
    }
    
    Stage mainStage;
    private Scene mainScene;
    Debugger debugger;
    EmulatorOptions options;
    UCPU16 cpu;
    HighResolutionTimer cpuclock;
    UNCD321 uncd321;
    Keyboard keyboard;
    UNEM192 unem192;
    Clock clock;
    UNTM200 untm200;
    Canvas screen;
    GraphicsContext ctx;
    PixelWriter pw;
    EmuMenuFile menuFile;
    EmuMenuEdit menuEdit;
    EmuMenuRun menuRun;
    EmuMenuOptions menuOptions;
    EmuMenuHelp menuHelp;
    int quickState;
    long animationTimer;
    
    @Override
    public void start(Stage stage) {
        mainStage = stage;
        mainScene = new Scene(new VBox());
        
        quickState = 0;
        animationTimer = Long.MIN_VALUE;
        
        Config.load();
        I18n.loadLanguage("en_US"); // no language selection support yet
        stage.setTitle(I18n.format("title"));
        
        MenuBar menuBar = initializeMenu();
        ((VBox) mainScene.getRoot()).getChildren().addAll(menuBar);
        
        Group screenGroup = new Group();
        this.screen = new Canvas(256, 192);
        this.ctx = this.screen.getGraphicsContext2D();
        this.pw = this.ctx.getPixelWriter();
        setScale(2);
        this.clearScreen();
        screenGroup.getChildren().add(screen);
        ((VBox) mainScene.getRoot()).getChildren().addAll(screenGroup);
        
        addKeyEvents();
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                exitGracefully();
            }
        });
        
        initCPU();
        //initDevices(); called via CPU reset now
        initDisplay();
        
        debugger = new Debugger(this);
        options = new EmulatorOptions(this);
        options.reloadConfig();
        
        updateFloppies();
        
        stage.setScene(mainScene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }
    
    public void showDebugger(String reason) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                debugger.showDebugger(reason);
            }
        });
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

    public void setCanCopy(boolean b) {
        menuEdit.copy.setDisable(!b);
    }

    void setScale(int i) {
        this.screen.setScaleX(i);
        this.screen.setScaleY(i);
        mainStage.sizeToScene();
    }

    public void clearScreen() {
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, 256, 192);
    }

    void exitGracefully() {
        Config.save();
        System.exit(0);
    }
    
    public void loadDevicesFromState(UNCD321 monitor, Keyboard keyboard, UNEM192 memory, Clock clock, UNTM200 untm200) {
        this.uncd321 = monitor;
        this.keyboard = keyboard;
        this.unem192 = memory;
        this.clock = clock;
        this.untm200 = untm200;
        this.updateFloppies();
    }

    private void addKeyEvents() {
        this.screen.setFocusTraversable(true);
        this.screen.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> this.screen.requestFocus());
        this.screen.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                keyboard.keyDown(event.getCode(), event.isShiftDown(), event.isControlDown());
                event.consume();
            }
        });
        this.screen.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                keyboard.keyUp(event.getCode(), event.isShiftDown(), event.isControlDown());
                event.consume();
            }
        });
    }

    private void initCPU() {
        this.cpu = new UCPU16(new StandardMemory(), this);
        this.cpuclock = new HighResolutionTimer(CPU_HZ, cpu);
        this.cpu.setClock(cpuclock);
        this.cpu.reset(true);
        this.cpu.getClock().start();
    }

    public void initDevices() {
        this.cpu.addDevice(this.uncd321 = new UNCD321(this.cpu, this));
        this.cpu.addDevice(this.keyboard = new Keyboard(this.cpu));
        this.cpu.addDevice(this.unem192 = new UNEM192(this.cpu));
        this.cpu.addDevice(this.clock = new Clock(this.cpu));
        this.cpu.addDevice(this.untm200 = new UNTM200(this.cpu));
    }

    private void initDisplay() {
        final EmulatorMain self = this;
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (animationTimer == Long.MIN_VALUE) {
                    animationTimer = now;
                } else {
                    if ((now - animationTimer) >= 33000000) { // 33 ms
                        animationTimer = now;
                    } else {
                        return;
                    }
                }
                uncd321.displayFrame(now, screen, ctx, pw, self);
            }
        }.start();
    }

    void tryPaste(String data) {
        for (char c: data.toCharArray()) {
            if (c == 10) {
                keyboard.addToKeyQueueFromPaste(0x11);
            } else if (c >= 0x20 && c <= 0x7e) {
                keyboard.addToKeyQueueFromPaste(c);
            }
        }
    }

    private int quicksave = 0;
    int getQuickSaveState() {
        return quicksave;
    }
    void setQuickSaveState(int j) {
        quicksave = j;
    }

    public void writeBack(M35FD drive, int driveno) {
        
    }

    void showOptions() {
        options.show();
    }

    void updateFloppies() {
        boolean d0 = getDrive(0) != null;
        boolean d1 = getDrive(1) != null;
        menuFile.floppy0Insert.setDisable(!d0);
        menuFile.floppy0Eject.setDisable(!d0);
        menuFile.floppy0Wp.setDisable(!d0);
        if (!d0) {
            menuFile.floppy0Wp.setSelected(false);
        }
        menuFile.floppy1Insert.setDisable(!d1);
        menuFile.floppy1Eject.setDisable(!d1);
        menuFile.floppy1Wp.setDisable(!d1);
        if (!d1) {
            menuFile.floppy1Wp.setSelected(false);
        }
    }
    
    M35FD getDrive(int n) {
        int i = 0x4fd524c5 + n;
        for (Hardware hw: cpu.getDevices()) {
            if (hw.hardwareId() == i) {
                return (M35FD) hw;
            }
        }
        return null;
    }

    void setWriteProtected(int i, boolean wp) {
        M35FD drive = getDrive(i);
        if (drive != null) {
            drive.setWriteProtected(wp);
        }
    }
}
