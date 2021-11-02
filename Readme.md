# Nand Beyond Tetris

### An ongoing attempt to modernize the Hack computer. 

Welcome to what might one day become V2.0 of the Hack computer. The Hack computer introduced as a part of the nand2tetris course
is a simple single program computer with its own instruction set, VM language and simple OOP language to instruct it. 

This project tries to advance the capabilities of the Hack computer without losing its signature simplicity. 
The main areas of improvement that I feel like would be monumental to the Hack computer are the introduction of processes 
(plural) and a way to be (or seem) concurrent.

Nand Beyond Tetris uses the Nand2Tetris codebase as the starting ground and as Nand Beyond Tetris is still in development, all the goals of the project aren't fulfilled just yet. These are the things 
I've done so far:

- Add a clock and an interrupt
- Add an interrupt handler register along with other registers 
- Add additional instructions to manipulate the new registers (backward compatible)
- Use a base and bounds method to do memory virtualization
- Increase word and bus sizes from 16 bit to 32 bit
- Add additional instructions to load large constants with a backward compatible 16 bit instruction set
- Increase RAM size from 24KB to 145KB
- Add a sample assembly program to provide a proof of concept


## Upcoming changes
- Changes to the HackOS to make use of the 'hardware' changes in the hack computer

## Building the project
The `make` command should do that (only works on a linux). Requires jdk.