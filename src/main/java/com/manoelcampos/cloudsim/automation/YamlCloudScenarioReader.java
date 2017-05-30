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
package com.manoelcampos.cloudsim.automation;

import cloudreports.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.cloudbus.cloudsim.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads Cloud Computing simulation scenarios
 * from an YAML file and stores them into a {@link #getScenarios() List} of
 * {@link YamlCloudScenario} objects.
 * These {@link YamlCloudScenario} are built using CloudSim.
 *
 * <p>To create simulation scenarios from an YAML file,
 * you have to use the class constructor that will try
 * to read the given file.
 * The scenarios read will be available in the {@link #getScenarios() scenarios} attribute.
 * Then, to build and run each simulation scenario in CloudSim,
 * instantiate a {@link CloudSimulation} passing a {@link YamlCloudScenario} to it.
 * </p>
 *
 * @author Manoel Campos da Silva Filho
 * @see YamlCloudScenario
 */
public class YamlCloudScenarioReader {
    private final File file;
    private final List<YamlCloudScenario> scenarios;

    /**
     * Instantiates a YamlCloudScenarioReader and
     * reads the YAML file containing the data to create Cloud Computing simulation scenarios.
     * Then, the List of simulation scenarios can be accessed using {@link #getScenarios()}.
     *
     * @param filePath the path of the YAML file to read
     * @param disableLog indicate if CloudSim log must be disabled or not
     */
    public YamlCloudScenarioReader(final String filePath, final boolean disableLog) throws IllegalArgumentException, FileNotFoundException, YamlException {
        this.file = new File(filePath);

        if (filePath == null || "".equals(filePath)) {
            throw new IllegalArgumentException("You must specify an YAML file, containing the CloudSim simulation scenario, as command line parameter.");
        }
        Log.setDisabled(disableLog);

        this.scenarios = readYamlFile();
    }

    /**
     * Reads the YAML file containing the data to creat Cloud Computing simulation scenarios.
     *
     * @return a List of simulation scenarios specified inside the YAML file.
     * @throws FileNotFoundException when the YAML file is not found.
     * @throws YamlException         when there is any error parsing the YAML file.
     */
    private List<YamlCloudScenario> readYamlFile() throws FileNotFoundException, YamlException {
        final List<YamlCloudScenario> scenarios = new ArrayList<YamlCloudScenario>();
        final YamlReader reader = createYamlReader();

        YamlCloudScenario scenario;
        while ((scenario = reader.read(YamlCloudScenario.class)) != null) {
            scenarios.add(scenario);
        }

        return scenarios;
    }

    /**
     * Creates an {@link YamlReader} to read an YAML file and instantiate
     * java objects for the entries inside the file.
     *
     * @return the {@link YamlReader} to enable reading the YAML file
     * @throws FileNotFoundException
     */
    private YamlReader createYamlReader() throws FileNotFoundException {
        final YamlReader reader = new YamlReader(new FileReader(file));
        final YamlConfig cfg = reader.getConfig();

        //Defines the aliases in the YAML file that refers to specific java Classes.
        cfg.setClassTag("datacenter", DatacenterRegistry.class);
        cfg.setClassTag("customer", CustomerRegistry.class);
        cfg.setClassTag("san", SanStorageRegistry.class);
        cfg.setClassTag("host", HostRegistry.class);
        cfg.setClassTag("cloudlet", CloudletRegistry.class);
        cfg.setClassTag("vm", VmRegistry.class);

        return reader;
    }

    /**
     * Gets the YAML {@link File} to read.
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the List of Cloud Simulation scenarios loaded from the {@link #getFile() YAML file}.
     * @return
     */
    public List<YamlCloudScenario> getScenarios() {
        return scenarios;
    }
}