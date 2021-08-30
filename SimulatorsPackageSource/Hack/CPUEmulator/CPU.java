/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/

package Hack.CPUEmulator;

import Hack.Controller.*;
import Hack.ComputerParts.*;
import Hack.Utilities.*;
import Hack.Assembler.*;
import builtInVMCode.Sys;

import java.util.Timer;

/**
 * A computer with memory (ROM & RAM) and two registers (A & D). Includes
 * a method for loading a program (HACK file) into the ROM and another
 * method for running the program which is currently in them ROM. Also gives
 * read access to the memory.
 */
public class CPU
{
    // The address and program counter registers.
    protected PointerAddressRegisterAdapter A, PC;

    // The data register
    protected Register D;
    protected Register PCS;
    protected Register CTR;
    protected Register BASE;
    protected Register INTH;
    protected Register TEMP;
    protected Register RESET;
    protected Register TIMER;
    protected Register ONE;

    // The RAM array.
    protected RAM M;

    // The ROM array
    protected ROM rom;

    // The ALU
    protected ALU alu;

    // The bus.
    protected Bus bus;

    // The time that passed since the program started running.
    protected long time;

    protected Register[] choice;

    // An assembler transltor
    protected HackAssemblerTranslator assemblerTranslator;

    /**
     * Constructs a new cpu with the given ROM, RAM, A, D, PC & ALU.
     */
    public CPU(RAM ram, ROM rom, PointerAddressRegisterAdapter A, Register D,
               PointerAddressRegisterAdapter PC, ALU alu, Bus bus, Register PCS,
               Register CTR, Register BASE, Register INTH, Register TEMP, Register RESET,
               Register TIMER, Register ONE) {
        this.M = ram;
        this.rom = rom;
        this.A = A;
        this.D = D;
        this.PC = PC;
        this.alu = alu;
        this.bus = bus;
        this.PCS = PCS;
        this.CTR = CTR;
        this.BASE = BASE;
        this.INTH = INTH;
        this.TEMP = TEMP;
        this.RESET = RESET;
        this.TIMER = TIMER;
        this.ONE = ONE;

        choice = new Register[]{ A, D, null, PCS, CTR, BASE, INTH, TEMP, RESET, ONE };


        A.setUpdatePointer(false);

        assemblerTranslator = HackAssemblerTranslator.getInstance();
    }

    /**
     * Returns the bus.
     */
    public Bus getBus() {
        return bus;
    }

    /**
     * Returns the A register.
     */
    public Register getA() {
        return A;
    }

    /**
     * Returns the D register.
     */
    public Register getD() {
        return D;
    }

    /**
     * Returns the PC register.
     */
    public Register getPC() {
        return PC;
    }

    /**
     * Returns the RAM (random access memory).
     */
    public RAM getRAM() {
        return M;
    }

    /**
     * Returns the ROM (read only memory).
     */
    public ROM getROM() {
        return rom;
    }

    /**
     * Returns the ALU.
     */
    public ALU getALU() {
        return alu;
    }

    public Register getPCS() {
        return PCS;
    }

    public Register getCTR() {
        return CTR;
    }

    public Register getBASE() {
        return BASE;
    }

    public Register getINTH() {
        return INTH;
    }

    public Register getTEMP() {
        return TEMP;
    }

    public Register getRESET() {
        return RESET;
    }

    public Register getTIMER() {
        return TIMER;
    }

    /**
     * Returns the time that passed since the program started running.
     */
    public long getTime() {
        return time;
    }

    /**
     * Restarts the program from the beginning.
     */
    public void initProgram() {
        A.reset();
        A.setUpdatePointer(true);
        A.setUpdatePointer(false);

        D.reset();
        PCS.reset();
        CTR.reset();
        BASE.reset();
        INTH.reset();
        TEMP.reset();
        RESET.reset();
        TIMER.reset();
        ONE.reset();
        ONE.setValueAt(0, 0x8000, true);

        PC.reset();
        alu.reset();
        M.clearScreen();
        M.hideSelect();
        M.hideHighlight();
        rom.hideSelect();
        rom.hideHighlight();
        time = 0;
    }

    /**
     * Executes the current instruction (ROM at pc).
     * Throws ProgramException if the current instruction is illegal or
	 * if it causes an illegal effect (read/write from M when A is an illegal
	 * address or jump when A is an illegal address).
     */
    public void executeInstruction() throws ProgramException {
        int instruction = rom.getValueAt(PC.get());
        boolean pcChanged = false;

        if ((instruction & 0x8000) == 0)
            bus.send(rom, PC.get(), A, 0);
        else if ((instruction & 0xe000) == 0xe000) {
            computeExp(instruction);
            setDestination(instruction);
            pcChanged = checkJump(instruction);
        } else if ((instruction & 0xa000) == 0xa000) {
            computeNewExp(instruction);
            setNewDestination(instruction);
        } else if ((instruction & 0xe000) == 0x8000 ){
            setLoadDestination(instruction);
        }
        else if (instruction != HackAssemblerTranslator.NOP)
            throw new ProgramException("At line " + PC.get() +
									   ": Illegal instruction");

        if (!pcChanged) {
            int newPC = PC.get() + 1;
            if (newPC < 0 || newPC >= Definitions.ROM_SIZE)
                throw new ProgramException("At line " + PC.get() +
										   ": Can't continue past last line");
            PC.setValueAt(0, newPC, true);
        }
        int controlValue = CTR.getValueAt(0);
        if ((controlValue & 0x1) > 0){
            int timerValue = TIMER.getValueAt(0) - 1;
            if (timerValue == 0) {
                PCS.setValueAt(0, PC.get(), true);
                TIMER.setValueAt(0, RESET.getValueAt(0), true);
                PC.setValueAt(0, INTH.getValueAt(0), true);
            } else if (timerValue < 0) {
                TIMER.setValueAt(0, RESET.getValueAt(0), true);
            } else {
                TIMER.setValueAt(0, timerValue, true);
            }
        }
        time++;
    }

    private void setLoadDestination(int instruction) throws ProgramException {
        boolean highSet = (instruction & 0x1000) > 0;
        int register = (instruction & 0x0f00);
        int number = (instruction & 0x00ff) << 16;
        if (highSet) {
            number <<= 8;
        }

        if (register >= choice.length){
            throw new ProgramException("Chosen register index: " + register + " is larger than expected.");
        }
        Register destination = choice[register];
        // very hacky solution (won't run on resets) TODO: fix this
//        rom.setValueAt(PC.get(), number, true);
        int mask = highSet ? 0x00ffffff : 0xff00ffff;
        int result = (mask & A.get()) | number;
        alu.setInput0(result);

        alu.setCommand(highSet ? "^" : "#", false, false, true, false, true, false);
        alu.compute();
        bus.send(alu, 2, destination, 0);
    }

    private void computeNewExp(int instruction) {
        int instructionSeg = (instruction & 0x0f00) >> 8;
        boolean z0 = (instructionSeg == 0 || instructionSeg == 3 || instructionSeg == 5 || instructionSeg == 4); //Destination properties
        // TODO: Doesn't work for NOR in this case: Current solution is to document it
        boolean n0 = ((instruction & 0x0800) == 0);
        boolean z1 = false; // Source properties
        boolean n1 = (instructionSeg == 2 || instructionSeg == 5);
        boolean f = (instructionSeg == 1 || instructionSeg == 4 || instructionSeg == 5 || instructionSeg == 8);
        boolean no = (instructionSeg == 1 || instructionSeg == 2 || instructionSeg == 3 || instructionSeg == 5);
        alu.setCommand("NONE", z0, n0, z1, n1, f, no);
        int sourceIndex = (instruction & 0x00f0) >> 4;
        if (sourceIndex == 2){
            // TODO: Add bounds check and refactor
            bus.send(M, A.get() + BASE.get(), alu, 1);
        } else {
            Register source = choice[sourceIndex];
            bus.send(source, 0, alu, 1);
        }

        int destIndex = (instruction & 0x000f);
        if (destIndex == 2){
            bus.send(M, A.get() + BASE.get(), alu, 0);
        } else {
            Register destination = choice[destIndex];
            bus.send(destination, 0, alu, 0);
        }

        alu.compute();
    }

    private void setNewDestination(int instruction) {
        int destIndex = (instruction & 0x000f);
        if (destIndex == 2){
            bus.send(alu, 2, M, A.get() + BASE.get());
        } else {
            Register destination = choice[destIndex];
            bus.send(alu, 2, destination, 0);
        }
    }

    // computes the exp part of the given instruction.
    // The result will be at the alu's output.
    // Throws ProgramException if the calculation involves M and A contains
	// an illegal address.
    protected void computeExp(int instruction) throws ProgramException {
        boolean indirect = (instruction & 0x1000) > 0;
        boolean zd = (instruction & 0x0800) > 0;
        boolean nd = (instruction & 0x0400) > 0;
        boolean zm = (instruction & 0x0200) > 0;
        boolean nm = (instruction & 0x0100) > 0;
        boolean f = (instruction & 0x0080) > 0;
        boolean no = (instruction & 0x0040) > 0;

        boolean isJmp = (indirect & zd & nd & zm & nm & f & no);
        if (isJmp){
            return;
        }

        try {
            alu.setCommand(assemblerTranslator.getExpByCode(instruction & 0xffc0),
                           zd, nd, zm, nm, f, no);
        } catch (AssemblerException ae) {}

        bus.send(D, 0, alu, 0); // sends D to input0 of the alu

        // sends A or M[A] to input1 of the alu
        if (indirect) {
			int address = A.get() + BASE.get();
			if (address < 0 || address >= M.getSize())
				throw new ProgramException("At line " + PC.get() +
										   ": Expression involves M but A=" +
										   address +
										   " is an illegal memory address.");
            A.setUpdatePointer(true);
            bus.send(M, address, alu, 1);
            A.setUpdatePointer(false);
        }
        else
            bus.send(A, 0, alu, 1);
        alu.compute();
    }

    // Sets the registers with the alu's output according to
	// the given instruction
    // Throws ProgramException if destination contains M and A contains
	// an illegal address.
    protected void setDestination(int instruction) throws ProgramException {
        boolean destA = (instruction & 0x0020) > 0;
        boolean destD = (instruction & 0x0010) > 0;
        boolean destM = (instruction & 0x0008) > 0;

        if (destM) {
			int address = A.get() + BASE.get();
			if (address < 0 || address >= M.getSize())
				throw new ProgramException("At line " + PC.get() +
										   ": Destination is M but A=" +
										   address +
										   " is an illegal memory address.");
            A.setUpdatePointer(true);
			bus.send(alu, 2, M, address);
            A.setUpdatePointer(false);
        }
        if (destA)
            bus.send(alu, 2, A, 0);
        if (destD)
            bus.send(alu, 2, D, 0);
    }

    // Sets the program counter (if necessary) according to
	// the given instruction and the alu's output.
	// If the program counter was changed, returns true, otherwise false.
    // Throws ProgramException if the program counter should be changed and A
	// contains an illegal address.
    protected boolean checkJump(int instruction) throws ProgramException {
        boolean jumpNegative = (instruction & 0x0004) > 0;
        boolean jumpEqual = (instruction & 0x0002) > 0;
        boolean jumpPositive = (instruction & 0x0001) > 0;
        boolean useStore = (instruction & 0x1fc0) == 0x1fc0;
        boolean changed = false;

        int exp = alu.getValueAt(2);

        if ((exp < 0 && jumpNegative) ||
            (exp == 0 && jumpEqual) ||
            (exp > 0 && jumpPositive)) {
			int newPC = A.get();
            if (newPC < 0 || newPC >= Definitions.ROM_SIZE)
				throw new ProgramException("At line " + PC.get() +
										   ": Jump requested but A=" + newPC +
										   " is an illegal program address.");
            if (useStore){
                bus.send(PCS, 0, PC, 0);
            } else {
                bus.send(A, 0, PC, 0);
            }
            changed = true;
        }

        return changed;
    }

    public Register getONE() {
        return ONE;
    }
}
