/*
 * Relaunch64 - A Java cross-development IDE for C64 machine language coding.
 * Copyright (C) 2001-2014 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://www.popelganda.de
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein 
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder 
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der 
 * GNU General Public License.
 * 
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm 
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.relaunch64.popelganda.assemblers;

import java.util.ArrayList;

/**
 *
 * @author Soci/Singular
 */
public class Assemblers {
    public static final Assembler ASM_KICKASSEMBLER = new Assembler_kick();
    public static final Assembler ASM_ACME = new Assembler_acme();
    public static final Assembler ASM_64TASS = new Assembler_64tass();
    public static final Assembler ASM_CA65 = new Assembler_ca65();
    public static final Assembler ASM_DREAMASS = new Assembler_dreamass();
    public static final Assembler ASM_DASM = new Assembler_dasm();
    public static final Assembler ASM_TMPX = new Assembler_tmpx();

    private static final Assembler assemblers[] = {
        ASM_KICKASSEMBLER,
        ASM_ACME,
        ASM_64TASS,
        ASM_CA65,
        ASM_DREAMASS,
        ASM_DASM,
        ASM_TMPX
    };

    public static Assembler byID(int id) {
        try {
            return assemblers[id];
        }
        catch (Exception ex) {
            return null;
        }
    }

    public static String[] names() {
        ArrayList<String> names = new ArrayList <>();
        for (Assembler i : assemblers) {
            names.add(i.name());
        }
        return names.toArray(new String[names.size()]);
    }
}