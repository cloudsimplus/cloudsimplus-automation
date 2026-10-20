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
package org.cloudsimplus.automation.examples;

import com.esotericsoftware.yamlbeans.YamlException;
import org.cloudsimplus.automation.CloudSimulation;
import org.cloudsimplus.automation.YamlCloudScenario;
import org.cloudsimplus.automation.YamlCloudScenarioReader;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.util.ResourceLoader;

import java.io.FileNotFoundException;

/// Starts the example, parsing a YAML file containing
/// the simulation scenario, building and running it
/// in CloudSim Plus.
///
/// @author Manoel Campos da Silva Filho
public class Example1 {
    private Example1(){
        System.out.printf("Starting %s on %s%n", getClass().getSimpleName(), CloudSimPlus.VERSION);

        //Gets the path to the YAML file inside the resource directory.
        final String yamlFilePath = ResourceLoader.getResourcePath(getClass(), "CloudEnvironment1.yml");
        try {
            //Loads the YAML file containing 1 or more simulation scenarios.
            final var reader = new YamlCloudScenarioReader(yamlFilePath);
            //Gets the list or parsed scenarios.
            final var yamlCloudScenariosList = reader.getScenarios();
            //For each existing scenario, creates and runs it in CloudSim Plus, printing results.
            for (YamlCloudScenario scenario : yamlCloudScenariosList) {
                new CloudSimulation(scenario).run();
            }
        } catch (FileNotFoundException | YamlException e) {
            System.err.println("Error loading the simulation scenario from the YAML file: "+e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Example1();
    }
}
