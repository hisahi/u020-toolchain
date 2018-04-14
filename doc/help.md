
# Manual

Starting the emulator can be done by simply [loading the .jar file](https://github.com/hisahi/u020-toolchain/releases), moving it into a suitable folder and executing it.

The application will store configuration and other files into the same directory the .jar file is in, so it is recommended to move it into its own folder.

## Starting the application

The .JAR file can be opened by simply double-clicking or with a command interpreter using the command `java -jar u020-toolchain-version.jar`. Since the program uses JavaFX to implement graphical interfaces, a graphical environment is required to run the program.

## Using the emulator

The emulator starts into the main view, which contains the emulated screen and menus on the top side.

* [Elaborate description of the available settings](settings.md)

## Notes

* The virtual keyboard is designed specifically for the US keyboard layout. The exact steps to change to that layout depend on the operating system. Included below are instructions for some of them:
  * [https://support.microsoft.com/en-us/help/17424/windows-change-keyboard-layout](Windows 7, Windows 8, Windows 8.1). 
  The keyboard layout to add is "English (US)". You can use the keyboard layout menu to change to the layout after adding it.
  * [https://support.microsoft.com/en-us/help/4027670/windows-add-and-switch-input-and-display-language-preferences-in-windo](Windows 10). The keyboard layout to add is "English (US)". You can use the keyboard layout menu to change to the layout after adding it.
  * [https://support.apple.com/kb/PH25311](macOS)
  * Linux: The steps depend on your desktop environment and window manager.

## BASIC interpreter

The initial program in the emulator is a BASIC interpreter. It can be used similarly to other BASIC interpreters and used to program simple programs or as a calculator. The interpreter is Univtek BASIC, which is a still unfinished BASIC interpreter for the Univtek 020. Examples of yet unsupported features are loading programs from a floppy and saving programs to them.

Try the following program that asks for two numbers and calculates their sum:

```
10 REM SUMS TWO NUMBERS
20 PRINT "ENTER TWO NUMBERS"
30 PRINT "TO CALCULATE THEIR SUM"
40 INPUT A
50 INPUT B
60 PRINT "THE SUM IS",A+B
```

* [Complete BASIC manual](basic.md)

## Executing other programs

If you have a program binary image, you can execute it with the option File -> Load and Run. When the file has been selected, it will be loaded into memory and executed automatically.

Executing from a floppy image is done by inserting the image into the virtual floppy drive and by running the BASIC command `LOAD "*",0` (the BASIC interpreter does not support this yet!)

## For developers

* [Using the debugger](debugger.md)
* [Using the assembler and disassembler](assembler.md)
* [Univtek 020 hardware specifications](hardware.md)
* [Assembly programming for the Univtek 020](asm/main.md)
