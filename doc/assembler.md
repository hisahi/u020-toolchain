
# Using the assembler
The assembler takes in source code in a symbolic assembly form and generates both the resulting binary machine code as well as the symbol table.

The code is input under the first tab, titled "_In: Source code_". 

No preprocessor is currently implemented, so the code is simply assembled directly. This means that macros and such features are currently not supported.

Once the `Assemble` button is clicked, you will either get a generated symbol table under the `Out: symbol table` and a machine code listing under `Out: listing`, or an error if the code could not be successfully assembled. 

The symbol table is used to disassemble the code, as it contains the areas with `DAT` (so that data won't be disassembled as code) as well as the labels. 

The final listing tab has the options to export the listing, export the machine code as binary, export it as a hexadecimal listing or to run it directly under the emulator.

# Using the disassembler
The disassembler is similar to the assembler in many ways. The first tab takes the machine code input as a hexadecimal listing, or binary data can also be imported. The code starting address should be set to `0000` for full programs, but may be changed for code snippets. 

The disassembler will also accept a symbol table that describes the labels and data sections of the hexadecimal code.

Once `Disassemble` is clicked, a listing will be generated under the `Out: listing` tab, where it can be exported into a file.
