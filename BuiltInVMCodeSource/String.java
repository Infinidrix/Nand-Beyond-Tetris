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

package builtInVMCode;

import Hack.VMEmulator.BuiltInVMClass;
import Hack.VMEmulator.TerminateVMProgramThrowable;

/**
 * A built-in implementation for the String class of the Jack OS.
 */

public class String extends JackOSClass {

    public static int NEW(int maxLength)
			throws TerminateVMProgramThrowable {
		if (maxLength < 0) {
			callFunction("Sys.error", STRING_NEW_NEGATIVE_LENGTH);
		}
		int str = callFunction("Memory.alloc", maxLength+2);
		writeMemory(str, maxLength);
		writeMemory(str+1, 0);
        return str;
    }

    public static void dispose(int str)
			throws TerminateVMProgramThrowable {
		callFunction("Memory.deAlloc", str);
    }

    public static int length(int str)
			throws TerminateVMProgramThrowable {
        return readMemory(str+1);
    }

    public static char charAt(int str, int j)
			throws TerminateVMProgramThrowable {
		int l = readMemory(str+1);
		if (j < 0 || j >= l) {
			callFunction("Sys.error", STRING_CHARAT_ILLEGAL_INDEX);
		}
		return (char)readMemory(str+2+j);
    }

    public static void setCharAt(int str, int j, int c)
			throws TerminateVMProgramThrowable {
		int l = readMemory(str+1);
		if (j < 0 || j >= l) {
			callFunction("Sys.error", STRING_SETCHARAT_ILLEGAL_INDEX);
		}
		writeMemory(str+2+j, c);
    }

    public static int appendChar(int str, int c)
			throws TerminateVMProgramThrowable {
		int capacity = readMemory(str);
		int l = readMemory(str+1);
		if (l == capacity) {
			callFunction("Sys.error", STRING_APPENDCHAR_FULL);
		}
		writeMemory(str+2+l, c);
		writeMemory(str+1, l+1);
        return str;
    }

    public static void eraseLastChar(int str)
			throws TerminateVMProgramThrowable {
		int l = readMemory(str+1);
		if (l == 0) {
			callFunction("Sys.error", STRING_ERASELASTCHAR_EMPTY);
		}
		writeMemory(str+1, l-1);
    }

    public static int intValue(int str)
			throws TerminateVMProgramThrowable {
		StringBuffer javaStr = new StringBuffer();
		int l = readMemory(str+1);
		for (int i=0; i<l; ++i) {
			javaStr.append((char)readMemory(str+2+i));
		}
		return javaStringToInt(javaStr.toString());
	}

    public static void setInt(int str, int j)
			throws TerminateVMProgramThrowable {
		java.lang.String s = ""+j;
		int l = (int)s.length();
		int capacity = readMemory(str);
		if (capacity < l) {
			callFunction("Sys.error", STRING_SETINT_INSUFFICIENT_CAPACITY);
		}
		writeMemory(str+1, l);
		for (int i=0; i<l; ++i) {
			writeMemory(str+2+i, s.charAt(i));
		}
    }

    public static char newLine() {
        return NEWLINE_KEY;
    }

    public static char backSpace() {
        return BACKSPACE_KEY;
    }

    public static char doubleQuote() {
        return '"';
    }

}
