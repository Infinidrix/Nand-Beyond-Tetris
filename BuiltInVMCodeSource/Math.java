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
 * A built-in implementation for the Math class of the Jack OS.
 */

public class Math extends JackOSClass {

	public static void init() { }

	public static int abs(int x) {
		return (x<0)?(int)-x:x;
	}

	public static int multiply(int x, int y) {
		return (int)(x*y);
	}
	
	public static int divide(int x, int y)
			throws TerminateVMProgramThrowable 	{
		if (y == 0) {
			callFunction("Sys.error", MATH_DIVIDE_ZERO);
		}
		return (int)(x/y);
	}
	
	public static int min(int x, int y) {
		return (x>y)?y:x;
	}
	
	public static int max(int x, int y) {
		return (x>y)?x:y;
	}
	
	public static int sqrt(int x)
			throws TerminateVMProgramThrowable {
		if (x < 0) {
			callFunction("Sys.error", MATH_SQRT_NEGATIVE);
		}
		return (int)java.lang.Math.sqrt(x);
	}

}
