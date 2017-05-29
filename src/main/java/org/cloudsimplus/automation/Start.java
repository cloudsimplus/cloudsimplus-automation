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
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.FileNotFoundException;

/**
 * Starts the tool by loading a Cloud Computing simulation environment from an YAML file given by command line.
 * This is the bootstrap class that starts the tool and parses command line arguments.
 *
 * @author Manoel Campos da Silva Filho
 * @see YamlCloudEnvironmentReader
 */
public final class Start {
    /**
     * Command line args (see {@link #main(String[])}).
     */
    private final String[] args;
    private YamlCloudEnvironmentReader reader;

    /**
     * Executes the command line interface of the applications.
     *
     * @param args Arg 0: The name of YAML file containing
     *             the simulation environments to be created.
     *             Each YAML file can contain multiples environments to be created together.
     *             This is made only adding a --- separator between each environment.
     *             <br>
     *             Arg 1: false or 0 to disable the CloudSim Plus Log (optional)
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
        this.args = args;
        try {
            this.reader = new YamlCloudEnvironmentReader(getFileNameFromCommandLine(), isToDisableLog());
            if(reader.getEnvironments().isEmpty()) {
                System.err.println("Your YAML file is empty.\n");
            }
            build();
        } catch (IllegalArgumentException|FileNotFoundException e){
            System.err.printf("%s", e.getMessage());
        } catch (YamlException e){
            System.err.printf("Error trying to parse the YAML file: %s\n", e.getMessage());
        } catch (Exception e){
            System.err.printf("An unexpected error happened: %s\n", e.getMessage());
        }

    }

    /**
     * Builds and run the Cloud Computing simulation environments loaded from the YAML file.
     */
    public void build() {
        System.out.printf(
            "Starting %d Simulation Environments(s) from file %s in CloudSim Plus %s\n",
            reader.getEnvironments().size(), reader.getFile(), CloudSim.VERSION);

        int i = 0;
        for (YamlCloudEnvironment env : reader.getEnvironments()) {
            final String environmentName = String.format("%d - %s", i++, reader.getFile().getName());
            env.build(environmentName);
        }
    }

    /**
     * Gets the file name from the command line arguments.
     *
     * @return
     */
    private String getFileNameFromCommandLine() {
        return args.length > 0 ? args[0] : "";
    }

    /**
     * Checks if CloudSim Plus Log has to be disabled.
     *
     * @return
     */
    private boolean isToDisableLog() {
        return args.length == 2 && ("false".equalsIgnoreCase(args[1]) || "0".equals(args[1]));
    }

}

