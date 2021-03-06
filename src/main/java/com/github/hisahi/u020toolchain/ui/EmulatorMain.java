
package com.github.hisahi.u020toolchain.ui;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.hardware.Clock;
import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.hardware.IPixelWriter;
import com.github.hisahi.u020toolchain.hardware.JavaFXPixelWriter;
import com.github.hisahi.u020toolchain.hardware.Keyboard;
import com.github.hisahi.u020toolchain.hardware.M35FD;
import com.github.hisahi.u020toolchain.hardware.UNAC810;
import com.github.hisahi.u020toolchain.hardware.UNCD321;
import com.github.hisahi.u020toolchain.hardware.UNEM192;
import com.github.hisahi.u020toolchain.hardware.UNMS001;
import com.github.hisahi.u020toolchain.hardware.UNTM200;
import com.github.hisahi.u020toolchain.logic.HighResolutionTimer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * The main window for the emulator. 
 * 
 * @author hisahi
 */
public class EmulatorMain extends Application {

    public static final String VERSION = "v1.0";
    public static final int CPU_HZ = 2000000;
    
    /**
     * The main function.
     * 
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    Stage mainStage;
    private Scene mainScene;
    Debugger debugger;
    EmulatorOptions options;
    EmulatorAssembler asmwnd;
    EmulatorDisassembler disasmwnd;
    UCPU16 cpu;
    HighResolutionTimer cpuclock;
    UNCD321 uncd321;
    Keyboard keyboard;
    UNEM192 unem192;
    Clock clock;
    UNTM200 untm200;
    UNMS001 unms001;
    UNAC810 unac810;
    Canvas screen;
    GraphicsContext ctx;
    PixelWriter pw;
    IPixelWriter ipw;
    EmuMenuFile menuFile;
    EmuMenuEdit menuEdit;
    EmuMenuRun menuRun;
    EmuMenuOptions menuOptions;
    EmuMenuHelp menuHelp;
    File floppy0;
    File floppy1;
    int quickState;
    long animationTimer;
    boolean shouldHideCursor;
    boolean pauseIfInactive;
    private boolean pausedInactive;
    
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
        this.ipw = new JavaFXPixelWriter(this.screen);
        setScale(2);
        this.clearScreen();
        screenGroup.getChildren().add(screen);
        ((VBox) mainScene.getRoot()).getChildren().addAll(screenGroup);
        
        addScreenEvents();
        addFocusBlurEvents();
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                exitGracefully();
            }
        });
        
        initCPU();
        initDisplay();
        
        debugger = new Debugger(this);
        options = new EmulatorOptions(this);
        asmwnd = new EmulatorAssembler(this);
        disasmwnd = new EmulatorDisassembler(this);
        options.reloadConfig();
        
        updateFloppies();
        
        this.cpu.reset(true); 
        cpuclock.start();
        
        stage.setScene(mainScene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }
    
    /**
     * Displays the debugger window.
     * 
     * @param reason The cause to trigger the debugger (invalid instruction?)
     */
    public void showDebugger(String reason) {
        final EmulatorMain self = this;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                boolean oldPaused = cpu.isPaused();
                boolean oldClock = cpu.getClock().isRunning();
                cpu.getClock().stop();
                cpu.resume();
                uncd321.displayFrame(ipw, self);
                if (oldPaused) {
                    cpu.pause();
                }
                if (oldClock) {
                    cpu.getClock().start();
                }
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

    /**
     * Updates the state of whether the text can be copied from the screen.
     * 
     * @param b Whether text can be copied.
     */
    public void setCanCopy(boolean b) {
        menuEdit.copy.setDisable(!b);
    }

    void setScale(int i) {
        this.screen.setScaleX(i);
        this.screen.setScaleY(i);
        
        mainStage.sizeToScene();
    }

    /**
     * Clears the display.
     */
    public void clearScreen() {
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, 256, 192);
    }

    void exitGracefully() {
        Config.save();
        System.exit(0);
    }
    
    /**
     * Reloads the devices when a CPU save state is restored.
     * 
     * @param monitor  The loaded UNCD321 instance.
     * @param keyboard The loaded Keyboard instance.
     * @param memory   The loaded UNEM192 instance.
     * @param clock    The loaded Clock instance.
     * @param untm200  The loaded UNTM200 instance.
     */
    public void loadDevicesFromState(UNCD321 monitor, Keyboard keyboard, UNEM192 memory, Clock clock, UNTM200 untm200) {
        this.uncd321 = monitor;
        this.keyboard = keyboard;
        this.unem192 = memory;
        this.clock = clock;
        this.untm200 = untm200;
        this.updateFloppies();
    }

    private void addScreenEvents() {
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
        this.screen.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (unms001 != null) {
                    unms001.updateFromData((int) event.getX(), (int) event.getY(), event.isPrimaryButtonDown(), event.isSecondaryButtonDown());
                }
            }
        });
        this.screen.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (unms001 != null) {
                    unms001.updateFromData((int) event.getX(), (int) event.getY(), event.isPrimaryButtonDown(), event.isSecondaryButtonDown());
                }
            }
        });
        this.screen.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (unms001 != null) {
                    unms001.updateFromData((int) event.getX(), (int) event.getY(), event.isPrimaryButtonDown(), event.isSecondaryButtonDown());
                }
            }
        });
        this.screen.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (shouldHideCursor) {
                    mainScene.setCursor(Cursor.NONE);
                }
            }
        });
        this.screen.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (Cursor.NONE.equals(mainScene.getCursor())) {
                    mainScene.setCursor(Cursor.DEFAULT);
                }
            }
        });
    }

    private void addFocusBlurEvents() {
        mainStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue.booleanValue()) {
                    if (pausedInactive) {
                        pausedInactive = false;
                        cpu.resume();
                    }
                } else {
                    if (pauseIfInactive && cpuclock.isRunning()) {
                        pausedInactive = true;
                        cpu.pause();
                    }
                }
            }
        });
    }

    private void initCPU() {
        this.cpu = new UCPU16(new StandardMemory(), this);
        this.cpuclock = new HighResolutionTimer(CPU_HZ, cpu);
        this.cpu.pause();
        this.cpu.setClock(cpuclock);
    }

    /**
     * Plug the fundamental devices into the current UCPU16 instance.
     */
    public void initDevices() {
        cpu.addDevice(this.uncd321 = new UNCD321(cpu, this));
        cpu.addDevice(this.keyboard = new Keyboard(cpu));
        cpu.addDevice(this.unem192 = new UNEM192(cpu));
        cpu.addDevice(this.clock = new Clock(cpu));
        cpu.addDevice(this.untm200 = new UNTM200(cpu));
    }

    /**
     * Plug the fundamental devices into the given UCPU16 instance when an
     * EmulatorMain instance isn't available.
     * 
     * @param cpu The UCPU16 instance.
     */
    public static void initDevicesForTesting(UCPU16 cpu) {
        cpu.addDevice(new UNCD321(cpu, null));
        cpu.addDevice(new Keyboard(cpu));
        cpu.addDevice(new UNEM192(cpu));
        cpu.addDevice(new Clock(cpu));
        cpu.addDevice(new UNTM200(cpu));
    }

    private void initDisplay() {
        final EmulatorMain self = this;
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (animationTimer == Long.MIN_VALUE) {
                    animationTimer = now;
                } else {
                    if ((now - animationTimer) >= 16600000) { // 16.6 ms (60 fps)
                        animationTimer = now;
                    } else {
                        return;
                    }
                }
                uncd321.displayFrame(ipw, self);
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

    /**
     * Writes the contents of the floppy drive back into the file inserted into it.
     * 
     * @param drive   The drive to load the data from.
     */
    public void writeBack(M35FD drive) {
        File file = null;
        if (drive.getDriveId() == 0) {
            file = floppy0;
        } else if (drive.getDriveId() == 1) {
            file = floppy1;
        }
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                int[] binary = drive.getRawMedia();
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

    void showOptions() {
        boolean oldPaused = cpu.isPaused();
        cpu.pause();
        options.show();
        if (!oldPaused) {
            cpu.resume();
        }
    }

    void updateFloppies() {
        boolean d0 = getDrive(0) != null;
        boolean d1 = getDrive(1) != null;
        menuFile.floppy0Insert.setDisable(!d0);
        menuFile.floppy0Eject.setDisable(!(d0 && getDrive(0).hasMedia()));
        menuFile.floppy0Wp.setDisable(!d0);
        if (!d0) {
            menuFile.floppy0Wp.setSelected(false);
        }
        menuFile.floppy1Insert.setDisable(!d1);
        menuFile.floppy1Eject.setDisable(!(d1 && getDrive(1).hasMedia()));
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

    Font getMonospacedFont() {
        return Font.font("Monospaced");
    }

    /**
     * Reloads the configuration.
     */
    public void reloadConfig() {
        options.reloadConfig();
    }

    /**
     * Updates the CPU paused state.
     */
    public void updatePause() {
        if (cpu.isPaused()) {
            menuRun.pause.setText(I18n.format("menu.run.resume"));
        } else {
            menuRun.pause.setText(I18n.format("menu.run.pause"));
        }
    }
}
