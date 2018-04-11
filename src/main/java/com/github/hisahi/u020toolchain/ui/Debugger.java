
package com.github.hisahi.u020toolchain.ui; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.logic.AssemblyListing;
import com.github.hisahi.u020toolchain.logic.Disassembler;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The class for the Debugger window. Parses commands sent to the
 * debugger and executes them.
 * 
 * @author hisahi
 */
public class Debugger {
    private EmulatorMain main;
    private UCPU16 cpu;
    private Stage debuggerStage;
    private Scene debuggerScene;
    private TextArea log;
    private TextField cmd;
    private List<String> history;
    private int memoryLast;
    private int historyIndex;
    private String backup;
    
    /**
     * Initializes a new Debugger instance.
     * 
     * @param main The main window.
     */
    public Debugger(EmulatorMain main) {
        this.main = main;
        this.cpu = main.cpu;
        this.memoryLast = 0;
        this.history = new ArrayList<>();
        this.historyIndex = this.history.size();
        this.backup = "";
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
        log.setFont(main.getMonospacedFont());
        cmd.setFont(main.getMonospacedFont());
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
                } else if (event.getCode() == KeyCode.UP) {
                    event.consume();
                    if (historyIndex > 0) {
                        if (historyIndex == history.size()) {
                            backup = cmd.getText();
                        }
                        --historyIndex;
                        cmd.setText(history.get(historyIndex));
                        cmd.end();
                    }
                } else if (event.getCode() == KeyCode.DOWN) {
                    if (historyIndex < history.size()) {
                        ++historyIndex;
                        if (historyIndex == history.size()) {
                            cmd.setText(backup);
                        } else {
                            cmd.setText(history.get(historyIndex));
                        }
                        cmd.end();
                    }
                }
            }
        });
        
        debuggerStage.setScene(debuggerScene);
        debuggerStage.setWidth(640.0);
        debuggerStage.setHeight(480.0);
    }
    void showDebugger(String reason) {
        if (debuggerStage.isShowing()) {
            logPrintln(reason);
            logPrintln(I18n.format("debugger.helpintro"));
            debuggerStage.toFront();
            cmd.requestFocus();
        } else {
            log.clear();
            logPrintln(reason);
            logPrintln(I18n.format("debugger.helpintro"));
            debuggerStage.show();
            cmd.requestFocus();
        }
    }
    
    /**
     * Logs text into the debugger console.
     * 
     * @param text The text to be logged.
     */
    public void logPrint(String text) {
        log.appendText(text);
        log.setScrollTop(Double.MAX_VALUE);
    }
    
    /**
     * Logs a newline into the debugger console.
     */
    public void logPrintln() {
        logPrint("\n");
    }
    
    /**
     * Logs a line of text into the debugger console.
     * 
     * @param text The text to be logged.
     */
    public void logPrintln(String text) {
        logPrint(text + "\n");
    }
    
    private void executeCommand(String line) {
        if (line.isEmpty()) {
            if (this.history.size() > 0) {
                String lastCommand = this.history.get(this.history.size() - 1);
                if (!lastCommand.equals("n") && !lastCommand.equals("nr") && !lastCommand.equals("cycle")) {
                    logPrintln(">" + lastCommand);   
                }
                executeCommand(lastCommand);
            }
            return;
        }
        this.history.add(line);
        while (this.history.size() > 512) {
            this.history.remove(0);
        }
        this.historyIndex = this.history.size();
        this.backup = "";
        String cmd = line.split(" ")[0];
        String[] args = line.split(" ");
        main.cpu.getClock().stop();
        if (cmd.equalsIgnoreCase("?")) {
            displayHelp(args);
        } else if (cmd.equalsIgnoreCase("bp")) {
            breakpointCommand(args);
        } else if (cmd.equalsIgnoreCase("c")) {
            disassembleCode(args);
        } else if (cmd.equalsIgnoreCase("cycle")) {
            runCycle(args);
        } else if (cmd.equalsIgnoreCase("g")) {
            resumeCPU(args);
        } else if (cmd.equalsIgnoreCase("h")) {
            listHardware(args);
        } else if (cmd.equalsIgnoreCase("m")) {
            showMemory(args);
        } else if (cmd.equalsIgnoreCase("mw")) {
            writeToMemory(args);
        } else if (cmd.equalsIgnoreCase("n")) {
            runInstruction(args);
        } else if (cmd.equalsIgnoreCase("nr")) {
            runInstructionWithRegs(args);
        } else if (cmd.equalsIgnoreCase("p")) {
            pauseCPU(args);
        } else if (cmd.equalsIgnoreCase("r")) {
            showRegs(args);
        } else if (cmd.equalsIgnoreCase("s")) {
            showStack(args);
        } else if (cmd.equalsIgnoreCase("z")) {
            resetCPU(args);
        } else {
            logPrintln(I18n.format("debugger.unknown"));
        }
        logPrintln();
        main.cpu.getClock().start();
    }

    private void displayHelp(String[] args) {
        logPrintln(I18n.format("debugger.help.repeat"));
        logPrintln(I18n.format("debugger.help.help"));
        logPrintln(I18n.format("debugger.help.breakpoints"));
        logPrintln(I18n.format("debugger.help.code"));
        logPrintln(I18n.format("debugger.help.cycle"));
        logPrintln(I18n.format("debugger.help.go"));
        logPrintln(I18n.format("debugger.help.hw"));
        logPrintln(I18n.format("debugger.help.memory"));
        logPrintln(I18n.format("debugger.help.memorywrite"));
        logPrintln(I18n.format("debugger.help.instr"));
        logPrintln(I18n.format("debugger.help.instrregs"));
        logPrintln(I18n.format("debugger.help.skipping"));
        logPrintln(I18n.format("debugger.help.pause"));
        logPrintln(I18n.format("debugger.help.reg"));
        logPrintln(I18n.format("debugger.help.stack"));
        logPrintln(I18n.format("debugger.help.reset"));
    }

    private void runCycle(String[] args) {
        if (main.cpu.isHalted()) {
            logPrintln(I18n.format("debugger.halted"));
            return;
        }
        if (!main.cpu.isPaused()) {
            pauseCPU(args);
        }
        cpu.resume();
        int oldPC = cpu.getPC();
        cpu.tick();
        if (oldPC != cpu.getPC()) {
            if (cpu.wasInterruptHandled()) {
                logPrintln(I18n.format("debugger.interrupt", String.format("%04x", cpu.readRegister(Register.A))));
            }
            disassembleInstruction();
        } else {
            logPrintln(I18n.format("debugger.cyclestonext", cpu.getCyclesLeft()));
        }
        cpu.pause();
    }

    private void resetCPU(String[] args) {
        logPrintln(I18n.format("debugger.reset"));
        main.cpu.reset(true);
        main.cpu.pause();
        main.menuRun.pause.setText(I18n.format("menu.run.resume"));
    }

    private void pauseCPU(String[] args) {
        logPrintln(I18n.format("debugger.paused"));
        main.cpu.pause();
        main.menuRun.pause.setText(I18n.format("menu.run.resume"));
    }

    private void resumeCPU(String[] args) {
        logPrintln(I18n.format("debugger.unpaused"));
        main.cpu.resume();
        main.menuRun.pause.setText(I18n.format("menu.run.pause"));
    }

    private void showRegs(String[] args) {
        logPrintln(main.cpu.dumpRegisters());
    }

    private void listHardware(String[] args) {
        logPrintln("HW_ID       VER     HW_MAKE     CLASS");
        for (Hardware h: main.cpu.getDevices()) {
            logPrintln(String.format("%08x    %04x    %08x    %s", 
                    h.hardwareId(), 
                    h.hardwareVersion(), 
                    h.hardwareManufacturer(), 
                    h.getClass().getCanonicalName().replace("com.github.hisahi.u020toolchain.", "")));
        }
        logPrintln("total " + main.cpu.getDevices().size() + " devices");
    }

    private void runInstruction(String[] args) {
        if (main.cpu.isHalted()) {
            logPrintln(I18n.format("debugger.halted"));
            return;
        }
        if (!main.cpu.isPaused()) {
            pauseCPU(args);
        }
        int oldPC = cpu.getPC();
        cpu.pause();
        while (cpu.getCyclesLeft() > 0) {
            cpu.tick();
        }
        cpu.tick();
        if (cpu.wasInterruptHandled()) {
            logPrintln(I18n.format("debugger.interrupt", String.format("%04x", cpu.readRegister(Register.A))));
        }
        disassembleInstruction();
        cpu.resume();
    }

    private void runInstructionWithRegs(String[] args) {
        if (main.cpu.isHalted()) {
            logPrintln(I18n.format("debugger.halted"));
            return;
        }
        if (!main.cpu.isPaused()) {
            pauseCPU(args);
        }
        int oldPC = cpu.getPC();
        cpu.resume();
        while (cpu.getCyclesLeft() > 0) {
            cpu.tick();
        }
        cpu.tick();
        logPrintln(cpu.dumpRegisters().replace("\n", ""));
        if (cpu.wasInterruptHandled()) {
            logPrintln(I18n.format("debugger.interrupt", String.format("%04x", cpu.readRegister(Register.A))));
        }
        disassembleInstruction();
        cpu.pause();
    }

    private void disassembleInstruction() {
        logPrintln((cpu.willSkip() ? "~" : " ") + Disassembler.listingToString(Disassembler.disassembleOneInstruction(cpu.getMemory().array(), cpu.getPC())));
    }

    private void disassembleCode(String[] args) {
        int startPos = cpu.getPC();
        int endPos = startPos + 16;
        if (args.length >= 2) {
            try {
                startPos = Integer.parseInt(args[1], 16);
            } catch (NumberFormatException nfe) {
                logPrintln(I18n.format("debugger.invalidnumber"));
                return;
            }
            endPos = startPos + 16;
            if (args.length >= 3) {
                try {
                    endPos = Integer.parseInt(args[2], 16);
                } catch (NumberFormatException nfe) {
                    logPrintln(I18n.format("debugger.invalidnumber"));
                    return;
                }
            }
        }
        startPos &= 0xFFFF;
        endPos &= 0xFFFF;
        List<AssemblyListing> l = Disassembler.disassembleInRange(cpu.getMemory().array(), startPos, endPos);
        for (AssemblyListing entry: l) {
            logPrintln(" " + Disassembler.listingToString(entry));
        }
    }

    private void showMemory(String[] args) {
        int startPos = memoryLast;
        int endPos = memoryLast + 0x70;
        if (args.length >= 2) {
            try {
                startPos = Integer.parseInt(args[1], 16);
            } catch (NumberFormatException nfe) {
                logPrintln(I18n.format("debugger.invalidnumber"));
                return;
            }
            endPos = startPos + 0x70;
            if (args.length >= 3) {
                try {
                    endPos = Integer.parseInt(args[2], 16);
                } catch (NumberFormatException nfe) {
                    logPrintln(I18n.format("debugger.invalidnumber"));
                    return;
                }
            }
        }
        startPos &= 0x1FFF0;
        endPos += 0x0f;
        endPos &= 0x1FFF0;
        showMemory(startPos, endPos);
    }
    
    private void showMemory(int startPos, int endPos) {
        logPrintln("       --0- --1- --2- --3- --4- --5- --6- --7- --8- --9- --A- --B- --C- --D- --E- --F-");
        int[] arr = cpu.getMemory().array();
        for (int i = startPos; i <= endPos; i += 16) {
            int m = i & 0xFFF0;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 16; ++j) {
                sb.append(String.format(" %04x", arr[m + j]));
            }
            logPrintln(String.format("%04x :", m) + sb.toString());
        }
        memoryLast = endPos + 0x10;
    }
    
    private void showStack(String[] args) {
        for (int i = cpu.getSP(); i < 65536; ++i) {
            logPrintln(I18n.format("%s    %s", String.format("%04x", i), String.format("%04x", cpu.getMemory().read(i))));
        }
    }

    private void breakpointCommand(String[] args) {
        if (args.length < 2) {
            for (int i = 0; i < 65536; ++i) {
                if (cpu.breakpoints.contains(i)) {
                    logPrint(String.format("%04x ", i));
                }
            }
            logPrintln();
            return;
        }
        int a;
        try {
            a = Integer.parseInt(args[1], 16);
        } catch (NumberFormatException nfe) {
            logPrintln(I18n.format("debugger.invalidnumber"));
            return;
        }
        a &= 0xFFFF;
        if (cpu.breakpoints.contains(a)) {
            cpu.breakpoints.remove(a);
            if (cpu.breakpoints.size() < 1) {
                cpu.disableBreakpoints();
            }
            logPrintln(I18n.format("debugger.breakpoint.off", String.format("%04x", a)));
        } else {
            cpu.breakpoints.add(a);
            cpu.enableBreakpoints();
            logPrintln(I18n.format("debugger.breakpoint.on", String.format("%04x", a)));
        }
    }

    private void writeToMemory(String[] args) {
        if (args.length < 3) {
            logPrintln(I18n.format("debugger.help.memorywrite"));
            return;
        }
        int startPos;
        try {
            startPos = Integer.parseInt(args[1], 16);
        } catch (NumberFormatException nfe) {
            logPrintln(I18n.format("debugger.invalidnumber"));
            return;
        }
        int[] mem = new int[args.length - 2];
        for (int i = 2; i < args.length; ++i) {
            try {
                mem[i - 2] = Integer.parseInt(args[i], 16);
            } catch (NumberFormatException nfe) {
                logPrintln(I18n.format("debugger.invalidnumber"));
                return;
            }
        }
        startPos &= 0xFFFF;
        int pos = startPos;
        for (int j = 0; j < mem.length; ++j) {
            cpu.getMemory().write(pos, mem[j]);
            pos = (pos + 1) & 0xFFFF;
        }
        showMemory(startPos, startPos + mem.length - 1);
    }
    
}
