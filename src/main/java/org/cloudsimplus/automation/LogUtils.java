/*
 * CloudSim Plus Automation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim Plus.
 * https://github.com/manoelcampos/CloudSimAutomation
 *
 *     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Automation.
 *
 *     CloudSim Plus Automation is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus Automation is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Automation. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudsimplus.automation;

import org.cloudbus.cloudsim.util.Log;

/**
 * Useful methods to print log information to the terminal.
 *
 * @author Manoel Campos da Silva Filho
 */
public class LogUtils {
    private static String colSeparator="|\t";

    /**
     * Print a array of objects like a table.
     * @param captions The captions of the table
     * @param dataArray The data to be printed.
     * @see LogUtils#printCaptions(java.lang.String[])
     */
    public static void printLine(String[] captions, Object[] dataArray, String colSeparator) {
        String s;
        String fmt;
        String data;
        for (int i = 0; i < captions.length; i++) {
            //The data will be printed with the same size of the caption
            //of the corresponding column.
            fmt = "%-" + captions[i].length() + "s";
            data = "";
            if (i < dataArray.length) {
                data = dataArray[i].toString();
            }
            s = String.format(fmt, data) + colSeparator;
            Log.print(s);
        }
        Log.printLine();
    }

    public static void printLine(String[] captions, Object... dataArray) {
        printLine(captions, dataArray, colSeparator);
    }

    /**
     * Print the captions of a table to be presented at the terminal.
     * @param captions Captions of the table
     * @see LogUtils#printCaptions(java.lang.String[])
     */
    public static void printCaptions(String[] captions, String colSeparator) {
        for (String caption : captions) {
            Log.print(caption + colSeparator);
        }
        Log.printLine();
    }

    public static void printCaptions(String... captions) {
        printCaptions(captions, colSeparator);
    }

    /**
     * @return the colSeparator
     */
    public static String getColSeparator() {
        return colSeparator;
    }

    /**
     * @param aColSeparator the colSeparator to set
     */
    public static void setColSeparator(String aColSeparator) {
        colSeparator = aColSeparator;
    }
}
