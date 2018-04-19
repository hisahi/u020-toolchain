
# Assembly programming for Univtek 020
The main CPU architecture, UCPU-16, is based on the DCPU-16, an architecture designed for the now cancelled game _0x10c_ by Mojang. The architecture is best described as a register plus memory CISC architecture with some RISC elements.

# Instructions
There are two types of instructions:

* _unary instructions_, instructions that only take a single parameter. These are called _special opcodes_ by the specification.
* _binary instructions_, instructions that take two parameters. These are called _basic opcodes_ by the specification.

In addition to these instructions, there is a pseudo-instruction `DAT`, which can take any number of parameters and is assembled as its parameters, allowing the entry of binary data. For example, `DAT 0x55AA, 0x1234` represents the two words `0x55AA 0x1234`. Such instructions can even be disassembled, if a proper symbol table is given.

## Unary instructions
Unary instructions only take a single parameter. Example of an unary instruction is `JSR`, which is used to jump into a subroutine: `JSR 0x4000` jumps into a subroutine located at 0x4000.

Some unary instructions, while technically take a parameter, can also be assembled without such, as the instructions do not use the parameter in any way (they do not read it or write to it). These include the instructions `NOP`, `DBG` and `RFI`, which are assembled as `NOP A`, `DBG A` and `RFI A` respectively.

## Binary instructions
Binary instructions take two parameters. The most common binary instruction is `SET`, which simply reads the second parameter and writes into the first. 

## Conditional skip instructions
Many (primarily CISC) architectures, such as x86, 6502 and 68000, use conditional jumps or branches as the only form of conditional execution. These jumps can then be used to implement both branches, such as if-else switches, as well as loops.

UCPU-16 inherits a peculiar DCPU-16 feature: IF instructions, or _conditional skip instructions_. These binary instructions will skip the following instruction if the condition they represent is false. The instructions also support chaining: if a conditional skip instruction itself is skipped this way, the instruction following it is too skipped. Every skipped instruction incurs a cost of a single CPU cycle.

To implement conditional jumps with this system, the instruction to be skipped can simply be set to be a jump instruction. If the condition is false when reaching the conditional skip instruction, the jump instruction will be skipped and execution will continue from after it. 

## Instruction set
The full instruction set is documented under the [main UCPU16 specification](../specs/UCPU16.txt).

Example of the table: 

```
--- Basic opcodes (5 bits) ----------------------------------------------------
 C | VAL  | NAME     | DESCRIPTION
---+------+----------+---------------------------------------------------------
...
 1 | 0x01 | SET b, a | sets b to a
...
```
The `SET` instruction is a _basic opcode_, or a binary instruction, that takes one clock cycle to execute and has the opcode `0x01`. It `sets b to a`.

# Addressing modes
UCPU-16 supports a variety of addressing modes all inherited from DCPU-16.

## Cycle costs
Some addressing modes may require an extra cycle to process. This cycle only counts once within an instruction, not once every time the value is accessed.

## List of addressing modes
The list can be found under the [main UCPU16 specification](../specs/UCPU16.txt).

Example of the table:

```
--- Values: (5/6 bits) ---------------------------------------------------------
 C | VALUE     | DESCRIPTION
---+-----------+----------------------------------------------------------------
 0 | 0x00-0x07 | register (A, B, C, X, Y, Z, I or J, in that order)
...
```
The first eight addressing modes `0x00-0x07` represent the general-purpose registers and take 0 extra cycles to process.

# Synthesizing instructions
At first glance it may seem that the UCPU-16 architecture lacks several important instructions. However, many of these can be synthesized from the wide variety of instructions and addressing modes.

## Load & store
Loads and stores are done like on architectures without separate instructions.
* `LDA 0x4000` (load value into `A` from `0x4000`) = `SET A, [0x4000]`
* `STA 0x4000` (store value from `A` into `0x4000`) = `SET [0x4000], A`

## Push & pop
Interacting with the stack is done with the stack push/pop addressing mode.
* `PHA` (push register `A`) = `SET PUSH, A`
* `PLA` (pop register `A`) = `SET A, POP`

## Jumps
Absolute and relative jumps can simply be done by modifying the `PC` register directly, as it is supported as an addressing mode.

* `JMP 0x4000` (absolute jump to 0x4000) = `SET PC, 0x4000`
* `JMR 0x4000` (relative jump 0x4000 words onwards) = `ADD PC, 0x4000`

## Conditional branches
See the "_Conditional skip instructions_" section.

## Return from subroutine
To return from a subroutine, simply pop the `PC` from the stack.
* `RET` = `SET PC, POP`

## Complement
* `CPL A` (complement of `A`) = `XOR A, -1`

## Negate
* `NEG A` (negate the value of `A`) = The two instructions: `XOR A, -1` and `ADD A, 1`

# Interfacing with hardware
The Univtek 020 architecture uses a hardware system inherited from DCPU-16, in which 65,536 devices can be attached into the computer to be interfaced with the `HWN`, `HWQ` and `HWI` instructions.

`HWN` writes the number of currently connected hardware devices into the given location (such as a register or memory address). This number can then be used to check through all connected hardware devices with `HWQ`, which writes information about the given hardware into a subset of CPU registers. The allowed parameters for `HWQ` are in the range 0 to the value returned by `HWN` (the upper bound is exclusive).

For example, if there are currently 5 connected hardware devices, `HWN` will return `5` and `HWQ` will accept any value in the range `[0, 4]` or `[0, 5[`. Any parameters outside this range will simply do nothing, but will still consume the CPU cycles.

As the order of hardware is not well defined, all hardware that a program is planning to use must be scanned through with `HWQ`. An example of a routine for finding hardware by ID is given below.

```
; ================================
;     HARDWARE FINDING ROUTINE
; ================================
; Inputs:
;     A - low word of hardware ID
;     B - high word of hardware ID
; Outputs:
;     Z - the index of the requested
;         hardware device for use with
;         HWQ or HWI.
;         0xFFFF if not found.
;     EX - 0x0000 if the device was found
;          0xFFFF if not
;  

:dev_id_lo DAT 0x0000
:dev_id_hi DAT 0x0000
:device_find_raw
    HWN Z
:device_find_loop
    IFE Z, 0
        SET PC, device_find_err
    SUB Z, 1
    HWQ Z
    IFN A, [dev_id_lo]
        SET PC, device_find_loop
    IFN B, [dev_id_hi]
        SET PC, device_find_loop
:device_find_found
    SET EX, 0
    SET PC, POP
:device_find_err
    SET Z, 0xFFFF
    SET EX, 0xFFFF
    SET PC, POP

:device_find
    SET PUSH, A
    SET PUSH, B
    SET PUSH, C
    SET PUSH, X
    SET PUSH, Y
    SET [dev_id_lo], A
    SET [dev_id_hi], B
    JSR device_find_raw
    SET Y, POP
    SET X, POP
    SET C, POP
    SET B, POP
    SET A, POP
    SET PC, POP

```

Example of how to use the above routine:

```
:uncd321
    DAT 0x0000

;   (...)

    SET B, 0xdb7b ; UNCD321: 
    SET A, 0x373e ; 0xdb7b373e
    JSR device_find
    IFN EX, 0
        SET PC, uncd321_not_found
    SET [uncd321], Z
    
;   (...)

    SET A, 0        ; UNCD321 MEM_MAP_SCREEN
    SET B, 0xA000   ; address 0xA000
    HWI [uncd321]   ; use index we found earlier
    DBG             ; invoke the debugger

```

Note that while the example above has a fallback in case the UNCD321 is not found, it is on the list of devices that can be considered to _always_ be present, as it is a part of the basic configuration. This also includes the UNEM192, Keyboard, Clock and UNTM200.

# Example programs
This program shows a simple loop and will invoke the debugger when done. To see the results, type `r` into the [debugger console](../debugger.md).

```
    SET PC, main

:main
    SET A, 0        ; A := 10
    SET B, 10       ; B := 10
:simple_loop
    ADD A, B        ; A := A + B
    SUB B, 1        ; B := B - 1
    IFG B, 0        ; repeat if B > 0
        SET PC, simple_loop
    DBG             ; invoke the debugger

; Final values of registers:
;  A:0037   B:0000   C:0000   X:0000
;  Y:0000   Z:0000   I:0000   J:0000
; PC:000a  SP:0000  EX:0000  IA:0000
; 
; A = 0x0037 = 55
; (1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10)

```

The second program is a more complete version of the hardware interfacing example above. It shows "Hello, World!" on the screen and then stops.

```
    SET PC, main

; ================================
;     HARDWARE FINDING ROUTINE
; ================================
; Inputs:
;     A - low word of hardware ID
;     B - high word of hardware ID
; Outputs:
;     Z - the index of the requested
;         hardware device for use with
;         HWQ or HWI.
;         0xFFFF if not found.
;     EX - 0x0000 if the device was found
;          0xFFFF if not
;  

:dev_id_lo DAT 0x0000
:dev_id_hi DAT 0x0000
:device_find_raw
    HWN Z
:device_find_loop
    IFE Z, 0
        SET PC, device_find_err
    SUB Z, 1
    HWQ Z
    IFN A, [dev_id_lo]
        SET PC, device_find_loop
    IFN B, [dev_id_hi]
        SET PC, device_find_loop
:device_find_found
    SET EX, 0
    SET PC, POP
:device_find_err
    SET Z, 0xFFFF
    SET EX, 0xFFFF
    SET PC, POP

:device_find
    SET PUSH, A
    SET PUSH, B
    SET PUSH, C
    SET PUSH, X
    SET PUSH, Y
    SET [dev_id_lo], A
    SET [dev_id_hi], B
    JSR device_find_raw
    SET Y, POP
    SET X, POP
    SET C, POP
    SET B, POP
    SET A, POP
    SET PC, POP

:uncd321
    DAT 0x0000

:main
    SET B, 0xdb7b ; UNCD321: 
    SET A, 0x373e ; 0xdb7b373e
    JSR device_find
    IFN EX, 0
        SET PC, end
    SET [uncd321], Z

    SET A, 0        ; UNCD321 MEM_MAP_SCREEN
    SET B, 0xA000   ; address 0xA000
    HWI [uncd321]   ; use index we found earlier

    ; Hello, World!
    SET I, 0
    STI [I+0xA000], 0xF048
    STI [I+0xA000], 0xF065
    STI [I+0xA000], 0xF06C
    STI [I+0xA000], 0xF06C
    STI [I+0xA000], 0xF06F
    STI [I+0xA000], 0xF02C
    STI [I+0xA000], 0xF020
    STI [I+0xA000], 0xF057
    STI [I+0xA000], 0xF06F
    STI [I+0xA000], 0xF072
    STI [I+0xA000], 0xF06C
    STI [I+0xA000], 0xF064
    STI [I+0xA000], 0xF021
:end
    SET PC, end
```
