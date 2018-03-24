
package com.github.hisahi.u020_toolchain.ui; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.hardware.Hardware;
import com.github.hisahi.u020_toolchain.logic.AssemblyListing;
import com.github.hisahi.u020_toolchain.logic.Disassembler;
import java.util.List;
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
    private int memoryLast;
    public Debugger(EmulatorMain main) {
        this.main = main;
        this.cpu = main.cpu;
        this.memoryLast = 0;
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
        debuggerStage.setWidth(640.0);
        debuggerStage.setHeight(480.0);
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
    public void log_println() {
        log_print("\n");
    }
    public void log_println(String text) {
        log_print(text + "\n");
    }
    private void executeCommand(String line) {
        if (line.isEmpty()) {
            if (!lastCommand.isEmpty()) {
                if (!lastCommand.equals("n") && !lastCommand.equals("nr") && !lastCommand.equals("cycle"))
                    log_println(">" + lastCommand);
                executeCommand(lastCommand);
            }
            return;
        }
        lastCommand = line;
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
            log_println(I18n.format("debugger.unknown"));
        }
        log_println();
        main.cpu.getClock().start();
    }

    private void displayHelp(String[] args) {
        log_println(I18n.format("debugger.help.repeat"));
        log_println(I18n.format("debugger.help.help"));
        log_println(I18n.format("debugger.help.breakpoints"));
        log_println(I18n.format("debugger.help.code"));
        log_println(I18n.format("debugger.help.cycle"));
        log_println(I18n.format("debugger.help.go"));
        log_println(I18n.format("debugger.help.hw"));
        log_println(I18n.format("debugger.help.memory"));
        log_println(I18n.format("debugger.help.memorywrite"));
        log_println(I18n.format("debugger.help.instr"));
        log_println(I18n.format("debugger.help.instrregs"));
        log_println(I18n.format("debugger.help.skipping"));
        log_println(I18n.format("debugger.help.pause"));
        log_println(I18n.format("debugger.help.reg"));
        log_println(I18n.format("debugger.help.stack"));
        log_println(I18n.format("debugger.help.reset"));
    }

    private void runCycle(String[] args) {
        if (main.cpu.isHalted()) {
            log_println(I18n.format("debugger.halted"));
            return;
        }
        if (!main.cpu.paused) {
            pauseCPU(args);
        }
        cpu.paused = false;
        int oldPC = cpu.getPC();
        cpu.tick();
        if (oldPC != cpu.getPC()) {
            if (cpu.wasInterruptHandled()) {
                log_println(I18n.format("debugger.interrupt", String.format("%04x", cpu.readRegister(UCPU16.REG_A))));
            }
            disassembleInstruction();
        } else {
            log_println(I18n.format("debugger.cyclestonext", cpu.getCyclesLeft()));
        }
        cpu.paused = true;
    }

    private void resetCPU(String[] args) {
        log_println(I18n.format("debugger.reset"));
        main.cpu.reset(true);
        main.cpu.paused = true;
        main.menuRun.pause.setText(I18n.format("menu.run.resume"));
    }

    private void pauseCPU(String[] args) {
        log_println(I18n.format("debugger.paused"));
        main.cpu.paused = true;
        main.menuRun.pause.setText(I18n.format("menu.run.resume"));
    }

    private void resumeCPU(String[] args) {
        log_println(I18n.format("debugger.unpaused"));
        main.cpu.paused = false;
        main.menuRun.pause.setText(I18n.format("menu.run.pause"));
    }

    private void showRegs(String[] args) {
        log_println(main.cpu.dumpRegisters());
    }

    private void listHardware(String[] args) {
        log_println("HW_ID       VER     HW_MAKE     CLASS");
        for (Hardware h: main.cpu.getDevices()) {
            log_println(String.format("%08x    %04x    %08x    %s", h.hardwareId(), h.hardwareVersion(), h.hardwareManufacturer(), h.getClass().getCanonicalName().replace("com.github.hisahi.u020_toolchain.","")));
        }
        log_println("total " + main.cpu.getDevices().size() + " devices");
    }

    private void runInstruction(String[] args) {
        if (main.cpu.isHalted()) {
            log_println(I18n.format("debugger.halted"));
            return;
        }
        if (!main.cpu.paused) {
            pauseCPU(args);
        }
        int oldPC = cpu.getPC();
        cpu.paused = false;
        while (cpu.getCyclesLeft() > 0)
            cpu.tick();
        cpu.tick();
        if (cpu.wasInterruptHandled()) {
            log_println(I18n.format("debugger.interrupt", String.format("%04x", cpu.readRegister(UCPU16.REG_A))));
        }
        disassembleInstruction();
        cpu.paused = true;
    }

    private void runInstructionWithRegs(String[] args) {
        if (main.cpu.isHalted()) {
            log_println(I18n.format("debugger.halted"));
            return;
        }
        if (!main.cpu.paused) {
            pauseCPU(args);
        }
        int oldPC = cpu.getPC();
        cpu.paused = false;
        while (cpu.getCyclesLeft() > 0)
            cpu.tick();
        cpu.tick();
        log_println(cpu.dumpRegisters().replace("\n", ""));
        if (cpu.wasInterruptHandled()) {
            log_println(I18n.format("debugger.interrupt", String.format("%04x", cpu.readRegister(UCPU16.REG_A))));
        }
        disassembleInstruction();
        cpu.paused = true;
    }

    private void disassembleInstruction() {
        log_println((cpu.willSkip() ? "~" : " ") + Disassembler.listingToString(Disassembler.disassembleOneInstruction(cpu.getMemory().array(), cpu.getPC())));
    }

    private void disassembleCode(String[] args) {
        int startPos = cpu.getPC();
        int endPos = startPos + 16;
        if (args.length >= 2) {
            try {
                startPos = Integer.parseInt(args[1], 16);
            } catch (NumberFormatException nfe) {
                log_println(I18n.format("debugger.invalidnumber"));
                return;
            }
            endPos = startPos + 16;
            if (args.length >= 3) {
                try {
                    endPos = Integer.parseInt(args[2], 16);
                } catch (NumberFormatException nfe) {
                    log_println(I18n.format("debugger.invalidnumber"));
                    return;
                }
            }
        }
        startPos &= 0xFFFF;
        endPos &= 0xFFFF;
        List<AssemblyListing> l = Disassembler.disassembleInRange(cpu.getMemory().array(), startPos, endPos);
        for (AssemblyListing entry: l) {
            log_println(" " + Disassembler.listingToString(entry));
        }
    }

    private void showMemory(String[] args) {
        int startPos = memoryLast;
        int endPos = memoryLast + 0x70;
        if (args.length >= 2) {
            try {
                startPos = Integer.parseInt(args[1], 16);
            } catch (NumberFormatException nfe) {
                log_println(I18n.format("debugger.invalidnumber"));
                return;
            }
            endPos = startPos + 0x70;
            if (args.length >= 3) {
                try {
                    endPos = Integer.parseInt(args[2], 16);
                } catch (NumberFormatException nfe) {
                    log_println(I18n.format("debugger.invalidnumber"));
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
        log_println("       --0- --1- --2- --3- --4- --5- --6- --7- --8- --9- --A- --B- --C- --D- --E- --F-");
        int[] arr = cpu.getMemory().array();
        for (int i = startPos; i <= endPos; i += 16) {
            int m = i & 0xFFF0;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 16; ++j) {
                sb.append(String.format(" %04x", arr[m+j]));
            }
            log_println(String.format("%04x :", m) + sb.toString());
        }
        memoryLast = endPos + 0x10;
    }
    
    private void showStack(String[] args) {
        for (int i = cpu.getSP(); i < 65536; ++i) {
            log_println(I18n.format("%s    %s", String.format("%04x", i), String.format("%04x", cpu.getMemory().read(i))));
        }
    }

    private void breakpointCommand(String[] args) {
        if (args.length < 2) {
            for (int i = 0; i < 65536; ++i) {
                if (cpu.breakpoints.contains(i))
                    log_print(String.format("%04x ", i));
            }
            log_println();
            return;
        }
        int a;
        try {
            a = Integer.parseInt(args[1], 16);
        } catch (NumberFormatException nfe) {
            log_println(I18n.format("debugger.invalidnumber"));
            return;
        }
        a &= 0xFFFF;
        if (cpu.breakpoints.contains(a)) {
            cpu.breakpoints.remove(a);
            if (cpu.breakpoints.size() < 1)
                cpu.disableBreakpoints();
            log_println(I18n.format("debugger.breakpoint.off", String.format("%04x", a)));
        } else {
            cpu.breakpoints.add(a);
            cpu.enableBreakpoints();
            log_println(I18n.format("debugger.breakpoint.on", String.format("%04x", a)));
        }
    }

    private void writeToMemory(String[] args) {
        if (args.length < 3) {
            log_println(I18n.format("debugger.help.memorywrite"));
            return;
        }
        int startPos;
        try {
            startPos = Integer.parseInt(args[1], 16);
        } catch (NumberFormatException nfe) {
            log_println(I18n.format("debugger.invalidnumber"));
            return;
        }
        int[] mem = new int[args.length - 2];
        for (int i = 2; i < args.length; ++i) {
            try {
                mem[i - 2] = Integer.parseInt(args[i], 16);
            } catch (NumberFormatException nfe) {
                log_println(I18n.format("debugger.invalidnumber"));
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
