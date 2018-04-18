
# Using the debugger
The debugger can be invoked either manually under the File menu or by an invalid state reached by the CPU, such as an invalid instruction. If the CPU reaches such an illegal state, its execution will be halted and can only be continued by a reset.

The debugger allows typing commands into the text field at the bottom, which can then be executed by hitting Enter or Return.

## Commands

### `?` - help
When the debugger is opened, you should see `Type '?' without quotes for help`. Typing `?` will bring up the following help message:

```
Enter an empty command to repeat the last command
    ? - show help
    bp [ADDR] - list or toggle breakpoints
    c {AREA | START END} - show code (disassembled)
    cycle - run one cycle
    g - resumes the execution of the CPU
    h - show list of devices
    m {AREA | START END} - show memory
    mw AREA word word word... - write to memory
    n - run one instruction & disassemble
    nr - 'n' that also shows registers
        ~ means following instruction will be skipped by IF_
    p - pauses the execution of the CPU
    r - show registers
    s [LIMIT] - show stack
    z - reset the computer
```

### `bp` - breakpoint management
Used to toggle breakpoints or list currently active breakpoints. 

Typing `bp` alone lists currently active breakpoints. 

Following `bp` with an address, like `bp 2000` (addresses are assumed to be hexadecimal) will add a breakpoint, or if a breakpoint already exists at the given address, it is removed.

When code execution hits a breakpoint, the execution will stop and the debugger will be opened.

### `c` - disassembles code
Views and disassembles code currently in memory.

If no address is given, the current instruction (pointed at by `PC`) as well as the following 16 words are all disassembled.

If an address is given, 16 words of memory starting from that position will be disassembled.

If two addresses are given, the memory area between them is disassembled, with the start address being inclusive and the end address exclusive. For instance, `c 00a0 00c0` will disassemble the code within the range `00a0:00bf` and then write the listing into the debugger console.

```
> c 19bf 19cf
 19bf    8801                    SET     A, 1
 19c0    7a40 00a1               HWI     [0x00a1]
 19c2    8452                    IFE     C, 0
 19c3    7f81 19bd               SET     PC, 0x19bd
 19c5    c852                    IFE     C, 17
 19c6    7f81 19df               SET     PC, 0x19df
 19c8    cc52                    IFE     C, 18
 19c9    7f81 19bd               SET     PC, 0x19bd
 19cb    d052                    IFE     C, 19
 19cc    7f81 19ad               SET     PC, 0x19ad
 19ce    7c52 0190               IFE     C, 0x0190
```

### `cycle` - runs one cycle
Executes one CPU cycle. The cycle may either start a next instruction or continue the current one, if cycles are still to be executed.

### `g` - resumes execution
Resumes the execution of the CPU, if it is paused.

### `h` - list the hardware
Shows the list of hardware that is currently plugged in to the machine. The list includes the hardware ID, version, manufacturer info and the class of the implementation, without including the first part of the package.

```
> h
HW_ID       VER     HW_MAKE     CLASS
db7b373e    321a    2590a31c    hardware.UNCD321
30cf7406    0001    55aa55aa    hardware.Keyboard
ca1c4b47    01c0    2590a31c    hardware.UNEM192
12d0b402    0001    55aa55aa    hardware.Clock
8f1705a6    c801    2590a31c    hardware.UNTM200
4fd524c5    000b    1eb37e91    hardware.M35FD
4fd524c6    000b    1eb37e91    hardware.M35FD
ab212484    0001    2590a31c    hardware.UNMS001
total 8 devices
```

### `m` - dumps contents of memory
Shows the contents of the emulated memory.

If no address is given, 0x0080 (128) words are shown starting from 0x0000 or the end of the memory in the last `m` call.

If an address is given, 0x0080 (128) words are shown starting from the given position.

If two addresses are given, the memory area between them is shown, with the start address being inclusive and the end address inclusive. For instance, `m 00a0 00c0` will show the memory within the range `00a0:00bf`. A row of memory is always shown in full, even if only a part of it was requested.

Due to a bug, some extra areas of the memory (an extra row from the end) may be shown with some parameters.

```
> m 0b00 0b20
       --0- --1- --2- --3- --4- --5- --6- --7- --8- --9- --A- --B- --C- --D- --E- --F-
0b00 : b02a b02a b02a b020 b055 b04e b049 b056 b054 b045 b04b b020 b020 b030 b032 b030
0b10 : b0ff b000 b000 b000 b000 b000 b000 b000 b000 b000 b000 b000 b000 b000 b000 b000
0b20 : b055 b06e b069 b076 b074 b065 b06b b020 b042 b041 b053 b049 b043 b020 b030 b02e
```

### `mw` - writes into memory
Writes words into the memory starting from a given position. The starting position is given as the first parameter, and all subsequent parameters are considered words to be written into the memory. The words are written consecutively. 

For example, `mw 0b00 f048 f069 f021` writes three words `f048 f069 f021` into memory starting at address `0b00`.

After the write is complete, a memory dump (like that of the command `m`) of the affected memory area is shown.

### `n` - next instruction
Runs a single instruction and prints the instruction disassembled into the debugger console. 

```
> n
 19bf    8801                    SET     A, 1
```

If the instruction listing is preceded by a `~`, the instruction was skipped due to the condition of a branch being false.

``` 
> n
~1968    6381                    SET     PC, POP
```

In this case, the `SET PC, POP` instruction at `1968` wasn't actually executed, but instead skipped.

### `nr` - next instruction with regs
This command is the same as `n`, but the new state of registers after the execution of the instruction are also printed into the console. This register dump is similar as the format used by `r`.

### `p` - pauses execution
Pauses the execution of the CPU. It will stop running until resumed by `g` or manually run by `cycle`, `n` or `nr` commands.

### `r` - show registers
Shows the current state of the registers. This includes the eight general-purpose registers (`A`, `B`, `C`, `X`, `Y`, `Z`, `I`, `J`) as well as the four additional registers (`PC`, `SP`, `EX`, `IA`).

### `s` - show the stack
Shows the current stack. A limit can be provided as a parameter. For example, `s` shows the entire stack, while `s 5` will show at most only 5 words, listing either the entire stack or the top 5 words if the stack is longer than 5 words.

```
> s

fffe    19bf
ffff    18b7
```

### `z` - reset
Resets the emulated machine. It is paused automatically when reset using this command.
