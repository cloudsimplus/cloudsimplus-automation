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
 * Reads Cloud Computing simulation environments
 * from an YAML file and stores them into a {@link #getEnvironments() List} of
 * {@link YamlCloudEnvironment} objects.
 * These {@link YamlCloudEnvironment} are built using <a href="http://cloudsimplus.org">CloudSim Plus</a>.
 *
 * <p>To create simulation environments from an YAML file,
 * you have to use the class constructor that will try
 * to read the given file.
 * The environments read will be available in the {@link #getEnvironments() environments} attribute.
 * Then, to build and run each simulation environment in CloudSim Plus,
 * just call {@link YamlCloudEnvironment#build()}.
 * </p>
 *
 * @author Manoel Campos da Silva Filho
 * @see YamlCloudEnvironment
 */
public class YamlCloudEnvironmentReader {
    private final File file;
    private final List<YamlCloudEnvironment> environments;

    /**
     * Instantiates a YamlCloudEnvironmentReader and
     * reads the YAML file containing the data to create Cloud Computing simulation environments.
     * Then, the List of simulation environments can be accessed using {@link #getEnvironments()}.
     * It doesn't create the simulation scenarios in CloudSim Plus.
     * Each one added to the List should be created calling {@link YamlCloudEnvironment#build()}.
     *
     * @param filePath the path of the YAML file to read
     * @param disableLog indicate if CloudSim Plus log must be disabled or not
     */
    public YamlCloudEnvironmentReader(final String filePath, final boolean disableLog) throws IllegalArgumentException, FileNotFoundException, YamlException {
        this.file = new File(filePath);

        if (filePath == null || "".equals(filePath)) {
            throw new IllegalArgumentException("You must specify an YAML file, containing the CloudSim simulation environment, as command line parameter.");
        }
        Log.setDisabled(disableLog);

        this.environments = readYamlFile();
    }

    /**
     * Reads the YAML file containing the data to creat Cloud Computing simulation environments.
     *
     * @return a List of simulation environments specified inside the YAML file.
     * @throws FileNotFoundException when the YAML file is not found.
     * @throws YamlException         when there is any error parsing the YAML file.
     */
    private List<YamlCloudEnvironment> readYamlFile() throws FileNotFoundException, YamlException {
        final List<YamlCloudEnvironment> envs = new ArrayList<YamlCloudEnvironment>();
        final YamlReader reader = createYamlReader();

        YamlCloudEnvironment env;
        while ((env = reader.read(YamlCloudEnvironment.class)) != null) {
            envs.add(env);
        }

        return envs;
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
     * Gets the List of Cloud Simulation environments loaded from the {@link #getFile() YAML file}.
     * @return
     */
    public List<YamlCloudEnvironment> getEnvironments() {
        return environments;
    }
}
