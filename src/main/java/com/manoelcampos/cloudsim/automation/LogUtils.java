/*
 * CloudSim Automation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim.
 * https://github.com/manoelcampos/CloudSimAutomation
 *
 *     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Automation.
 *
 *     CloudSim Automation is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Automation is distributed in the hope that it will be useful,
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
package com.manoelcampos.cloudsim.automation;

import org.apache.commons.lang3.StringUtils;
import org.cloudbus.cloudsim.Log;

import java.util.Collections;
import java.util.stream.Stream;

/**
 * Useful methods to print log information to the terminal
 * @author Manoel Campos da Silva Filho
 */
public class LogUtils {
    private static String colSeparator="|";

    /**
     * Print a array of objects like a table.
     * @param captions The captions of the table
     * @param dataArray The data to be printed.
     * @see LogUtils#printCaptions(java.lang.String[]) 
     */
    public static void printLine(String[] captions, Object[] dataArray, String colSeparator) {
        for (int i = 0; i < captions.length; i++) {
            //The data will be printed with the same size of the caption 
            //of the corresponding column.
            String data = (i < dataArray.length) ? dataArray[i].toString() : "";
            Log.print(getFormattedData(captions[i].length(), data, colSeparator));
        }
        Log.printLine();
    }

    private static String getFormattedData(int colSize, String data, String colSeparator) {
        final String fmt = "%" + colSize + "s";
        return String.format(fmt, data) + colSeparator;
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
    
    public static void printCaptions(String ...captions) {
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

    public static void printLineSeparator(String[] captions) {
        for(String caption: captions){
            Log.print(String.join("", Collections.nCopies(caption.length()+1, "-")));
        }
        Log.printLine();
    }

    private static int getLengthOfColumnHeadersRow(String[] captions){
        return Stream.of(captions).mapToInt(col -> col.length()).sum();
    }

    public static String getCentralizedString(String[] captions, final String str) {
        final int identationLength = (getLengthOfColumnHeadersRow(captions) - str.length())/2;
        return String.format("%s%s", StringUtils.repeat(" ", identationLength), str);
    }

    public static void printCentralizedString(String[] captions, String text) {
        Log.printLine(getCentralizedString(captions, text));
    }
}
