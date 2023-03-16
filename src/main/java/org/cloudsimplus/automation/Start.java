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
package org.cloudsimplus.automation;

import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.*;
import org.cloudsimplus.core.CloudSimPlus;

import java.io.FileNotFoundException;
import java.util.regex.Pattern;

/**
 * Starts the tool by loading a Cloud Computing simulation scenario from an YAML file given by command line.
 * This is the bootstrap class that starts the tool and parses command line arguments.
 *
 * @author Manoel Campos da Silva Filho
 * @see YamlCloudScenarioReader
 */
public final class Start {
    private Options options;
    private YamlCloudScenarioReader reader;
    private CommandLine cmd;

    /**
     * Executes the command line interface of the applications.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        new Start(args);
    }

    /**
     * Default constructor that parses command line parameters
     * and actually execute the tool.
     * @param args command line parameters (see {@link #main(String[])})
     */
    private Start(final String[] args){
        try {
            if(!parseCommandLineOptions(args)){
                return;
            }

            this.reader = new YamlCloudScenarioReader(getFileNameFromCommandLine());
            if(reader.getScenarios().isEmpty()) {
                System.err.println("Your YAML file is empty.\n");
            }
            build();
        } catch (IllegalArgumentException|FileNotFoundException e){
            System.err.printf("%s", e.getMessage());
        } catch (YamlException e){
            System.err.printf("Error trying to parse the YAML file: %s\n", e.getMessage());
        } catch (ParseException e){
            System.err.printf("Error parsing command line arguments. %s\n", e.getMessage());
        } catch (Exception e){
            System.err.printf("An unexpected error happened: %s\n", e.getMessage());
        }
    }

    private void showUsageHelp() {
        final var formatter = new HelpFormatter();
        formatter.printHelp(getApplicationStartCmd() +" [options] YamlFilePath", options);
    }

    /**
     * Parses options given by command line.
     * @param args the command line options
     */
    private boolean parseCommandLineOptions(String[] args) throws ParseException {
        options = new Options();
        final var option = new Option("v", "Enables CloudSim Plus Log (disabled by default)");
        option.setLongOpt("verbose");
        options.addOption(option);

        options.addOption("s", "Suppress simulation results");
        options.addOption("h", "Show usage help");
        options.addOption("c", "Print scenario configuration");

        final var parser = new DefaultParser();
        this.cmd = parser.parse(options, args);

        /*
         * If after parsing the options there is an additional parameter to the YAML file name,
         * show the usage help.
         */
        if(cmd.getArgs().length == 0 || cmd.hasOption("h")){
            showUsageHelp();
            return false;
        }

        return true;
    }

    /**
     * Gets the command used to launch the application.
     * If the application was launched from the jar file, returns
     * a command like "java -jar name-of-the-jar-file".
     * If it was launched directly from the class file,
     * returns a command like "java class-file".
     * @return
     */
    private String getApplicationStartCmd() {
        final String fullClassFilePath = getFullClassFilePath();
        final String jarRegex = ".*\\/(.*\\.jar).*\\/";
        final String jarFile = regexMatch(jarRegex, fullClassFilePath);
        return (jarFile.isEmpty()) ? "java " + Start.class.getName() : "java -jar "+jarFile;
    }

    private String regexMatch(final String regex, final String text) {
        final var matcher = Pattern.compile(regex).matcher(text);
        if(matcher.find()){
            return matcher.group(1);
        }

        return "";
    }

    /**
     * Gets the full file path of this class, which may include the
     * path of a jar if the class is being run from a jar package.
     * @return
     */
    private String getFullClassFilePath() {
        final String classFileName = '/' + Start.class.getName().replace('.', '/') + ".class";
        return Start.class.getResource(classFileName).getFile();
    }

    /**
     * Builds and run Cloud Computing simulation scenarios loaded from the YAML file.
     */
    public void build() {
        System.out.printf(
            "Starting %d Simulation Scenario(s) from file %s in CloudSim Plus %s\n",
            reader.getScenarios().size(), reader.getFile(), CloudSimPlus.VERSION);

        int i = 0;
        for (YamlCloudScenario scenario : reader.getScenarios()) {
            final String scenarioName = String.format("%d - %s", i++, reader.getFile().getName());
            new CloudSimulation(scenario, scenarioName)
                .setShowResults(!cmd.hasOption("s"))
                .setLogEnabled(isToEnableLog())
                .setPrintScenariosConfiguration(isToPrintScenariosConfiguration())
                .run();
        }
    }

    /**
     * Gets the file name from the command line arguments,
     * which is the first argument remained after the parsing.
     *
     * @return
     */
    private String getFileNameFromCommandLine() {
        return cmd.getArgs().length > 0 ? cmd.getArgs()[0] : "";
    }

    /**
     * Checks if CloudSim Plus Log has to be enabled.
     *
     * @return
     */
    private boolean isToEnableLog() {
        return cmd.hasOption("v");
    }

    private boolean isToPrintScenariosConfiguration() {
        return cmd.hasOption("c");
    }
}

