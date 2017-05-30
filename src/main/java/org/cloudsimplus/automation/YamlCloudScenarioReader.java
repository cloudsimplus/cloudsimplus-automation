package org.cloudsimplus.automation;

import cloudreports.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.cloudbus.cloudsim.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads Cloud Computing simulation scenarios
 * from an YAML file and stores them into a {@link #getScenarios() List} of
 * {@link YamlCloudScenario} objects.
 * These {@link YamlCloudScenario} are built using <a href="http://cloudsimplus.org">CloudSim Plus</a>.
 *
 * <p>To create simulation scenarios from an YAML file,
 * you have to use the class constructor that will try
 * to read the given file.
 * The scenarios read will be available in the {@link #getScenarios() scenarios} attribute.
 * Then, to build and run each simulation scenario in CloudSim Plus,
 * just call {@link YamlCloudScenario#build()}.
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
     * It doesn't create the simulation scenarios in CloudSim Plus.
     * Each one added to the List should be created calling {@link YamlCloudScenario#build()}.
     *
     * @param filePath the path of the YAML file to read
     * @param disableLog indicate if CloudSim Plus log must be disabled or not
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
        cfg.setClassTag("profile", UtilizationProfile.class);
        cfg.setClassTag("vm", VirtualMachineRegistry.class);

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
