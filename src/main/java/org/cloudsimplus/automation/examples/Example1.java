package org.cloudsimplus.automation.examples;

import com.esotericsoftware.yamlbeans.YamlException;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.ResourceLoader;
import org.cloudsimplus.automation.CloudSimulation;
import org.cloudsimplus.automation.YamlCloudScenario;
import org.cloudsimplus.automation.YamlCloudScenarioReader;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Starts the example, parsing a YAML file containing
 * the simulation scenario, building and running it
 * in CloudSim Plus.
 *
 * @author Manoel Campos da Silva Filho
 */
public class Example1 {
    private Example1(){
        System.out.printf("Starting %s on %s%n", getClass().getSimpleName(), CloudSim.VERSION);

        //Gets the path to the YAML file inside the resource directory.
        final String yamlFilePath = ResourceLoader.getResourcePath(getClass(), "CloudEnvironment1.yml");
        try {
            //Loads the YAML file containing 1 or more simulation scenarios.
            final YamlCloudScenarioReader reader = new YamlCloudScenarioReader(yamlFilePath);
            //Gets the list or parsed scenarios.
            final List<YamlCloudScenario> simulationScenarios = reader.getScenarios();
            //For each existing scenario, creates and runs it in CloudSim Plus, printing results.
            for (YamlCloudScenario scenario : simulationScenarios) {
                new CloudSimulation(scenario).run();
            }
        } catch (FileNotFoundException | YamlException e) {
            System.err.println("Error when trying to load the simulation scenario from the YAML file: "+e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Example1();
    }
}
