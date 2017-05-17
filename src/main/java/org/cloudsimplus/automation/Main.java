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

import cloudreports.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the tool by loading a simulation scenario from a YAML file
 * given by command line. This is the bootstrap class that starts the command line tool.
 *
 * @author Manoel Campos da Silva Filho
 */
public class Main {

    /**
     * Execute the command line interface of the applications.
     *
     * @param args Arg 0: The name of YAML file containing
     *             the simulation scenarios to be created.
     *             Each YAML file can contain multiples scenarios to be created together.
     *             This is made only adding a --- separator between each scenario.
     *             <br>
     *             Arg 1: false or 0 to disable the CloudSim Plus Log (optional)
     */
    public static void main(String[] args) {
        String fileName = "CloudEnvironment1.yml"; //a default file to load
        if (args.length > 0) {
            fileName = args[0];
        } else if (!new File(fileName).exists()) {
            System.err.println("\n\nYou must specify a YAML file, containing the CloudSim simulation scenario, as command line parameter.\n");
            return;
        }

        final Map<Integer, YamlScenario> envs;
        try {
            envs = loadScenarioFile(fileName);
            //envs = null;
        } catch (FileNotFoundException | YamlException ex) {
            Logger.getLogger(YamlScenario.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        if (envs == null || envs.isEmpty()) {
            System.err.println("\n\nYour YAML file is empty or could not be loaded.\n");
            return;
        }

        Log.setDisabled(isToDisableLog(args));
        System.out.printf("Starting %d Simulation Scenario(s) from file %s in CloudSim Plus %s\n",
            envs.size(), fileName, CloudSim.VERSION);
        try {
            final String scenarioName = new File(fileName).getName();
            envs.forEach((i, env) -> env.run(i + " - " + scenarioName));
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.err.println("The simulation has been terminated due to an unexpected error");
        }
    }

    /**
     * Checks if CloudSim Plus Log has to be disabled.
     * @param args command line arguments
     * @return
     */
    private static boolean isToDisableLog(String[] args) {
        return args.length == 2 && ("false".equalsIgnoreCase(args[1]) || "0".equals(args[1]));
    }

    /**
     * Loads CloudSim simulation scenarios from a YAML file.
     *
     * @param yamlFileName The YAML scenario file to be loaded.
     * @return a map of simulation scenarios specified
     * inside the YAML file, where the key is the scenario ID. Each scenario inside the file
     * can be delimited using the line below:
     * --- #Delimits each Cloud Environment
     * @throws FileNotFoundException Throws when the YAML file
     *                               is not found.
     * @throws YamlException         Throws when there is any
     *                               error parsing the YAML file.
     */
    public static Map<Integer, YamlScenario> loadScenarioFile(final String yamlFileName)
        throws FileNotFoundException, YamlException {
        final Map<Integer, YamlScenario> envs = new HashMap<>();
        final YamlReader reader = createYamlReader(yamlFileName);

        YamlScenario env;
        int i = 0;
        while ((env = reader.read(YamlScenario.class)) != null) {
            envs.put(i++, env);
        }

        return envs;
    }

    public static YamlReader createYamlReader(final String yamlFileName) throws FileNotFoundException {
        YamlReader reader = new YamlReader(new FileReader(yamlFileName));
        final YamlConfig cfg = reader.getConfig();
        //Defines the aliases in the YAML file that refers to specific java Classes.
        cfg.setClassTag("datacenter", DatacenterRegistry.class);
        cfg.setClassTag("customer", CustomerRegistry.class);
        cfg.setClassTag("san", SanStorageRegistry.class);
        cfg.setClassTag("host", HostRegistry.class);
        cfg.setClassTag("profile", UtilizationProfile.class);
        cfg.setClassTag("vm", VirtualMachineRegistry.class);
        return reader;
    }
}

