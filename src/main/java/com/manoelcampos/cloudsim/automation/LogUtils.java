/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manoelcampos.cloudsim.automation;

import org.cloudbus.cloudsim.Log;

/**
 * Useful methods to print log information to the terminal
 * @author manoelcampos
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
    
    public static void printLine(String[] captions, Object[] dataArray) {
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
    
    public static void printCaptions(String[] captions) {
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
