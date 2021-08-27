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

package Hack.Assembler;

import Hack.Translators.HackTranslatorException;
import Hack.Utilities.Conversions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * A translation service between the Assembly text and the numeric instruction values.
 * The translation is bidirectional.
 * This is a singlton class.
 */
public class HackAssemblerTranslator {

    /**
     * Indicates an assembly line with no operation
     */
    public static final int NOP = (int)0x8000;

    // exp constants
    private static final Integer ZERO         = new Integer((int)0xea80);
    private static final Integer ONE          = new Integer((int)0xefc0);
    private static final Integer MINUS_ONE    = new Integer((int)0xee80);
    private static final Integer EXP_D        = new Integer((int)0xe300);
    private static final Integer NOT_D        = new Integer((int)0xe340);
    private static final Integer EXP_M        = new Integer((int)0xfc00);
    private static final Integer EXP_A        = new Integer((int)0xec00);
    private static final Integer NOT_M        = new Integer((int)0xfc40);
    private static final Integer NOT_A        = new Integer((int)0xec40);
    private static final Integer MINUS_D      = new Integer((int)0xe3c0);
    private static final Integer MINUS_M      = new Integer((int)0xfcc0);
    private static final Integer MINUS_A      = new Integer((int)0xecc0);
    private static final Integer D_PLUS_ONE   = new Integer((int)0xe7c0);
    private static final Integer M_PLUS_ONE   = new Integer((int)0xfdc0);
    private static final Integer A_PLUS_ONE   = new Integer((int)0xedc0);
    private static final Integer D_MINUS_ONE  = new Integer((int)0xe380);
    private static final Integer M_MINUS_ONE  = new Integer((int)0xfc80);
    private static final Integer A_MINUS_ONE  = new Integer((int)0xec80);
    private static final Integer D_PLUS_M     = new Integer((int)0xf080);
    private static final Integer D_PLUS_A     = new Integer((int)0xe080);
    private static final Integer D_MINUS_M    = new Integer((int)0xf4c0);
    private static final Integer D_MINUS_A    = new Integer((int)0xe4c0);
    private static final Integer M_MINUS_D    = new Integer((int)0xf1c0);
    private static final Integer A_MINUS_D    = new Integer((int)0xe1c0);
    private static final Integer D_AND_M      = new Integer((int)0xf000);
    private static final Integer D_AND_A      = new Integer((int)0xe000);
    private static final Integer D_OR_M       = new Integer((int)0xf540);
    private static final Integer D_OR_A       = new Integer((int)0xe540);

    // dest constants
    private static final Integer A   = new Integer((int)0x20);
    private static final Integer M   = new Integer((int)0x8);
    private static final Integer D   = new Integer((int)0x10);
    private static final Integer AM  = new Integer((int)0x28);
    private static final Integer AD  = new Integer((int)0x30);
    private static final Integer MD  = new Integer((int)0x18);
    private static final Integer AMD = new Integer((int)0x38);

    // jmp constants
    private static final Integer JMP              = new Integer((int)0x7);
    private static final Integer JMP_LESS_THEN    = new Integer((int)0x4);
    private static final Integer JMP_EQUAL        = new Integer((int)0x2);
    private static final Integer JMP_GREATER_THEN  = new Integer((int)0x1);
    private static final Integer JMP_NOT_EQUAL    = new Integer((int)0x5);
    private static final Integer JMP_LESS_EQUAL   = new Integer((int)0x6);
    private static final Integer JMP_GREATER_EQUAL = new Integer((int)0x3);

    private static final String[] symbols = new String[] {"_", "-", "|", "!", "--", "++", "", "", "+", "&"};
    private static final String[] registers = new String[] { "A", "D", "M", "P", "C", "B", "I", "T", "R" };


    // the single instance
    private static HackAssemblerTranslator instance;

    // The translation tables from text to codes
    private Hashtable expToCode, destToCode, jmpToCode;
    private Map<String, Integer> symbolLoc, registerLoc;

    // The translation table from code to text.
    private Hashtable expToText, destToText, jmpToText;


    /**
     * Creates a new translator.
     */
    private HackAssemblerTranslator() {
        instance = this;
        initExp();
        initDest();
        initJmp();
        initNewInst();
    }

    private void initNewInst() {
        symbolLoc= new HashMap<>();
        for (int loc = 0; loc < symbols.length; loc++) {
            symbolLoc.put(symbols[loc], (int) loc);
        }

        registerLoc= new HashMap<>();
        for (int loc = 0; loc < registers.length; loc++) {
            registerLoc.put(registers[loc], (int) loc);
            registerLoc.put(registers[loc].toLowerCase(Locale.ROOT), (int) loc);
        }
    }

    /**
     * Returns the single instance of the translator.
     */
    public static HackAssemblerTranslator getInstance() {
        if (instance == null)
            new HackAssemblerTranslator();
        return instance;
    }

    /**
     * Returns the code which represents the given exp text.
     * If doesn't exist, throws AssemblerException.
     */
    public int getExpByText(String text) throws AssemblerException {
        Integer code = (Integer)expToCode.get(text);
        if (code == null)
            throw new AssemblerException("Illegal exp: " + text);
        return code.shortValue();
    }

    /**
     * Returns the text which represents the given exp code.
     * If doesn't exist, throws AssemblerException.
     */
    public String getExpByCode(int code) throws AssemblerException {
        String result = (String)expToText.get(new Integer(code));
        if (result == null)
            throw new AssemblerException("Illegal exp: " + code);
        return result;
    }

    /**
     * Returns the code which represents the given dest text.
     * If doesn't exist, throws AssemblerException.
     */
    public int getDestByText(String text) throws AssemblerException {
        Integer code = (Integer)destToCode.get(text);
        if (code == null)
            throw new AssemblerException("Illegal dest: " + text);
        return code.shortValue();
    }

    /**
     * Returns the text which represents the given dest code.
     * If doesn't exist, throws AssemblerException.
     */
    public String getDestByCode(int code) throws AssemblerException {
        String result = (String)destToText.get(new Integer(code));
        if (result == null)
            throw new AssemblerException("Illegal dest: " + code);
        return result;
    }

    /**
     * Returns the code which represents the given jmp text.
     * If doesn't exist, throws AssemblerException.
     */
    public int getJmpByText(String text) throws AssemblerException {
        Integer code = (Integer)jmpToCode.get(text);
        if (code == null)
            throw new AssemblerException("Illegal jmp: " + text);
        return code.shortValue();
    }

    /**
     * Returns the text which represents the given jmp code.
     * If doesn't exist, throws AssemblerException.
     */
    public String getJmpByCode(int code) throws AssemblerException {
        String result = (String)jmpToText.get(new Integer(code));
        if (result == null)
            throw new AssemblerException("Illegal jmp: " + code);
        return result;
    }

    /**
     * Translates the given assembly language command and returns the corresponding
     * machine language code.
     * If the command is not legal, throws AssemblerException.
     */
    public int textToCode(String command) throws AssemblerException {
        int code = 0;
        int expCode = 0, jmpCode = 0, destCode = 0;

        try {
            AssemblyLineTokenizer input = new AssemblyLineTokenizer(command);

            if (input.isToken("@")) {
                input.advance(true);
                try {
                    code = Integer.parseInt(input.token());
                } catch (NumberFormatException nfe) {
                    throw new AssemblerException("A numeric value is expected");
                }
            } else if (input.isToken("jmp")){
                code = (int) 0xffc7;
            }
            else if (input.isToken("=")){
                int dest = 0, src = 0, exp = 0, type = 0xa, i = 0;
                input.advance(true);
                String secondToken = input.token();
                dest = registerLoc.get(secondToken.substring(i, i + 1));
                i++;
                if (secondToken.length() > i){
                    if (secondToken.length() > i + 1 && !Character.isAlphabetic(secondToken.charAt(i + 1))){
                        exp = symbolLoc.get(secondToken.substring(i, i + 2));
                        i += 2;
                    } else {
                        exp = symbolLoc.get(secondToken.substring(i, i + 1));
                        i++;
                    }
                }
                if (secondToken.length() > i){
                    src = registerLoc.get(secondToken.substring(i, i + 1));
                    i++;
                }
                if (secondToken.length() > i ){
                    throw new AssemblerException("Instruction longer than expected");
                }
                input.ensureEnd();
                code = (int) (type << 12 | exp << 8 | src << 4 | dest);
            }
            else { // compute-store-jump command

                String firstToken = input.token();
                input.advance(false);

                // find dest (if any)
                if (input.isToken("=")) {
                    Integer dest = (Integer)destToCode.get(firstToken);
                    if (dest == null)
                        throw new AssemblerException("Destination expected");

                    destCode = dest.shortValue();
                    input.advance(true);
                }

                // find exp
                Integer exp;
                if (!firstToken.equals("=") && destCode == 0)
                    exp = (Integer)expToCode.get(firstToken);
                else
                    exp = (Integer)expToCode.get(input.token());

                if (exp == null)
                    throw new AssemblerException("Expression expected");

                expCode = exp.shortValue();
                input.advance(false);

                if (input.isToken(";"))
                    input.advance(false);

                // find jmp (if any)
                if (!input.isEnd()) {
                    Integer jmp = (Integer)jmpToCode.get(input.token());
                    if (jmp == null)
                        throw new AssemblerException("Jump directive expected");

                    jmpCode = jmp.shortValue();
                    input.ensureEnd();
                }

                code = (int)(destCode + expCode + jmpCode);
            }

        } catch (IOException ioe) {
            throw new AssemblerException("Error while parsing assembly line");
        } catch (HackTranslatorException hte) {
            throw new AssemblerException(hte.getMessage());
        }

        return code;
    }

    /**
     * Translates the given machine language code and returns the corresponding Assembly
     * language command (String).
     * If illegal, throws AssemblerException.
     */
    public String codeToText(int code) throws AssemblerException {
        StringBuffer command = new StringBuffer();

        if (code != HackAssemblerTranslator.NOP) {
            if ((code & 0x8000) == 0) {
                command.append('@');
                command.append(code);
            } else if ((code & 0xf000) == 0xa000){
                int src = (int) ((code & 0x00f0) >> 4);
                int dest = (int) (code & 0x000f);
                int symb = (int) ((code & 0x0f00) >> 8);

                command.append("=");
                command.append(registers[dest]);
                command.append(symbols[symb]);
                command.append(registers[src]);

            }
            else {
                int exp = (int)(code & 0xffc0);
                int dest = (int)(code & 0x0038);
                int jmp = (int)(code & 0x0007);

                String expText = getExpByCode(exp);
                if (!expText.equals("")) {

                    if (dest != 0) {
                        command.append(getDestByCode(dest));
                        command.append('=');
                    }

                    command.append(expText);

                    if (jmp != 0) {
                        command.append(';');
                        command.append(getJmpByCode(jmp));
                    }
                }
            }
        }

        return command.toString();
    }

    /**
     * Loads the given program file (HACK or ASM) and returns a memory array of
     * the given size that contains the program. The given null value will be used
     * to fill the memory array initially.
     */
    public static int[] loadProgram(String fileName, int size, int nullValue)
     throws AssemblerException {
        int[] memory = null;

        File file = new File(fileName);
        if (!file.exists())
            throw new AssemblerException(fileName + " doesn't exist");

        if (fileName.endsWith(".hack")) {
            memory = new int[size];
            for (int i = 0; i < size; i++)
                memory[i] = nullValue;

            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String line;
                int pc = 0;

                while ((line = reader.readLine()) != null) {

                    int value = 0;

                    if (pc >= size)
                        throw new AssemblerException("Program too large");

                    try {
                        value = (int)Conversions.binaryToInt(line);
                    } catch (NumberFormatException nfe) {
                        throw new AssemblerException("Illegal character");
                    }

                    memory[pc++] = value;
                }

                reader.close();
            } catch (IOException ioe) {
                throw new AssemblerException("IO error while reading " + fileName);
            }
        }
        else if (fileName.endsWith(".asm")) {
            try {
                HackAssembler assembler = new HackAssembler(fileName, size, nullValue, false);
                memory = assembler.getProgram();
            } catch (HackTranslatorException ae) {
                throw new AssemblerException(ae.getMessage());
            }
        }
        else
            throw new AssemblerException(fileName + " is not a .hack or .asm file");

        return memory;
    }

    // initializes the exp table
    private void initExp() {
        expToCode = new Hashtable();
        expToText = new Hashtable();

        expToCode.put("0",ZERO);
        expToCode.put("1",ONE);
        expToCode.put("-1",MINUS_ONE);
        expToCode.put("D",EXP_D);
        expToCode.put("!D",NOT_D);
        expToCode.put("NOTD",NOT_D);
        expToCode.put("M",EXP_M);
        expToCode.put("A",EXP_A);
        expToCode.put("!M",NOT_M);
        expToCode.put("NOTM",NOT_M);
        expToCode.put("!A",NOT_A);
        expToCode.put("NOTA",NOT_A);
        expToCode.put("-D",MINUS_D);
        expToCode.put("-M",MINUS_M);
        expToCode.put("-A",MINUS_A);
        expToCode.put("D+1",D_PLUS_ONE);
        expToCode.put("M+1",M_PLUS_ONE);
        expToCode.put("A+1",A_PLUS_ONE);
        expToCode.put("D-1",D_MINUS_ONE);
        expToCode.put("M-1",M_MINUS_ONE);
        expToCode.put("A-1",A_MINUS_ONE);
        expToCode.put("D+M",D_PLUS_M);
        expToCode.put("M+D",D_PLUS_M);
        expToCode.put("D+A",D_PLUS_A);
        expToCode.put("A+D",D_PLUS_A);
        expToCode.put("D-M",D_MINUS_M);
        expToCode.put("D-A",D_MINUS_A);
        expToCode.put("M-D",M_MINUS_D);
        expToCode.put("A-D",A_MINUS_D);
        expToCode.put("D&M",D_AND_M);
        expToCode.put("M&D",D_AND_M);
        expToCode.put("D&A",D_AND_A);
        expToCode.put("A&D",D_AND_A);
        expToCode.put("D|M",D_OR_M);
        expToCode.put("M|D",D_OR_M);
        expToCode.put("D|A",D_OR_A);
        expToCode.put("A|D",D_OR_A);

        expToText.put(ZERO,"0");
        expToText.put(ONE,"1");
        expToText.put(MINUS_ONE,"-1");
        expToText.put(EXP_D,"D");
        expToText.put(NOT_D,"!D");
        expToText.put(EXP_M,"M");
        expToText.put(EXP_A,"A");
        expToText.put(NOT_M,"!M");
        expToText.put(NOT_A,"!A");
        expToText.put(MINUS_D,"-D");
        expToText.put(MINUS_M,"-M");
        expToText.put(MINUS_A,"-A");
        expToText.put(D_PLUS_ONE,"D+1");
        expToText.put(M_PLUS_ONE,"M+1");
        expToText.put(A_PLUS_ONE,"A+1");
        expToText.put(D_MINUS_ONE,"D-1");
        expToText.put(M_MINUS_ONE,"M-1");
        expToText.put(A_MINUS_ONE,"A-1");
        expToText.put(D_PLUS_M,"D+M");
        expToText.put(D_PLUS_A,"D+A");
        expToText.put(D_MINUS_M,"D-M");
        expToText.put(D_MINUS_A,"D-A");
        expToText.put(M_MINUS_D,"M-D");
        expToText.put(A_MINUS_D,"A-D");
        expToText.put(D_AND_M,"D&M");
        expToText.put(D_AND_A,"D&A");
        expToText.put(D_OR_M,"D|M");
        expToText.put(D_OR_A,"D|A");
    }

    // initializes the dest table
    private void initDest() {
        destToCode = new Hashtable();
        destToText = new Hashtable();

        destToCode.put("A",A);
        destToCode.put("M",M);
        destToCode.put("D",D);
        destToCode.put("AM",AM);
        destToCode.put("AD",AD);
        destToCode.put("MD",MD);
        destToCode.put("AMD",AMD);

        destToText.put(A,"A");
        destToText.put(M,"M");
        destToText.put(D,"D");
        destToText.put(AM,"AM");
        destToText.put(AD,"AD");
        destToText.put(MD,"MD");
        destToText.put(AMD,"AMD");
    }

    // initializes the jmp table
    private void initJmp() {
        jmpToCode = new Hashtable();
        jmpToText = new Hashtable();

        jmpToCode.put("JMP",JMP);
        jmpToCode.put("JLT",JMP_LESS_THEN);
        jmpToCode.put("JEQ",JMP_EQUAL);
        jmpToCode.put("JGT",JMP_GREATER_THEN);
        jmpToCode.put("JNE",JMP_NOT_EQUAL);
        jmpToCode.put("JLE",JMP_LESS_EQUAL);
        jmpToCode.put("JGE",JMP_GREATER_EQUAL);

        jmpToText.put(JMP,"JMP");
        jmpToText.put(JMP_LESS_THEN,"JLT");
        jmpToText.put(JMP_EQUAL,"JEQ");
        jmpToText.put(JMP_GREATER_THEN,"JGT");
        jmpToText.put(JMP_NOT_EQUAL,"JNE");
        jmpToText.put(JMP_LESS_EQUAL,"JLE");
        jmpToText.put(JMP_GREATER_EQUAL,"JGE");
    }
}
