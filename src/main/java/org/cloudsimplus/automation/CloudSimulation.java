package org.cloudsimplus.automation;

import cloudreports.models.*;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristicsSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.FileStorage;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.resources.SanStorage;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Represents a simulation created in CloudSim Plus.
 * @author Manoel Campos da Silva Filho
 */
public class CloudSimulation implements Runnable {
    private final YamlCloudScenario scenario;
    private final String label;
    private CloudSim cloudsimplus;
    private List<Datacenter> datacenters;

    private Map<DatacenterBroker, CustomerRegistry> brokers;
    private Map<DatacenterBroker, List<Vm>> vmsToBrokerMap;
    private Map<DatacenterBroker, List<Cloudlet>> cloudletsToBrokerMap;

    /**
     * Builds a {@link YamlCloudScenario Cloud Simulation Scenario} read from an YAML file
     * in CloudSim Plus.
     * @param scenario the {@link YamlCloudScenario} read from an YAML file
     */
    public CloudSimulation(YamlCloudScenario scenario) {
        this(scenario, "");
    }

    /**
     * Builds a {@link YamlCloudScenario Cloud Simulation Scenario} read from an YAML file
     * in CloudSim Plus.
     * @param scenario the {@link YamlCloudScenario} read from an YAML file
     * @param label A label to be used to identify the running
     *              simulation scenario. Commonly this can be the name
     *              of the loaded YAML file.
     */
    public CloudSimulation(YamlCloudScenario scenario, final String label) {
        this.scenario = scenario;
        this.datacenters = new ArrayList<>();
        this.cloudsimplus = new CloudSim();
        this.label = label;

        this.vmsToBrokerMap = new HashMap<>();
        this.cloudletsToBrokerMap = new HashMap<>();

        System.out.println("Hosts========================");
        this.datacenters = createDatacentersFromDatacenterRegistries();
        for (Datacenter datacenter : datacenters) {
            System.out.println(datacenter.getName() + ": " + datacenter.getHostList().size() + " hosts");
        }
        System.out.println("=============================");

        this.brokers = createDatacenterBrokersFromCustomerRegistries();
        this.vmsToBrokerMap = createVmListForAllBrokers(brokers);
        this.cloudletsToBrokerMap = createCloudletsFromUtilizationProfiles(brokers);

        for (DatacenterBroker broker : brokers.keySet()) {
            broker.submitVmList(vmsToBrokerMap.get(broker));
            broker.submitCloudletList(cloudletsToBrokerMap.get(broker));
        }
    }

    /**
     * Creates a map containing the abstract customer information ({@link CustomerRegistry})
     * used to create each concrete CloudSim customer ({@link DatacenterBroker}).
     *
     * @return Returns the map created.
     * @see YamlCloudScenario#customerRegistries
     */
    private Map<DatacenterBroker, CustomerRegistry> createDatacenterBrokersFromCustomerRegistries() {
        final int totalBrokerAmount = scenario.getCustomers().stream().mapToInt(CustomerRegistry::getAmount).sum();
        final Map<DatacenterBroker, CustomerRegistry> list = new HashMap<>(totalBrokerAmount);
        int brokerCount = 0;
        for (CustomerRegistry cr: scenario.getCustomers()) {
            for (int i = 0; i < cr.getAmount(); i++) {
                list.put(new DatacenterBrokerSimple(cloudsimplus), cr);
            }
        }

        return list;
    }

    /**
     * Creates the list of VMs in CloudSim Plus for each customer represented by a {@link DatacenterBroker}.
     *
     * @param crMap a Map between a {@link DatacenterBroker} representing a customer in CloudSim Plus
     *           and the {@link CustomerRegistry} object used to create VMs and Cloudlets for such a broker.
     * @return the a map containing the list of created VMs for each customer (DatacenterBroker).
     * @see #createDatacenterBrokersFromCustomerRegistries()
     */
    private Map<DatacenterBroker, List<Vm>> createVmListForAllBrokers(
        final Map<DatacenterBroker, CustomerRegistry> crMap)
    {
        final Map<DatacenterBroker, List<Vm>> vmMap = new HashMap<>(crMap.size());

        int createdVms = 0;
        for (DatacenterBroker broker : crMap.keySet()) {
            List<Vm> vms = createVmListForOneBroker(broker, crMap.get(broker), createdVms++);
            vmMap.put(broker, vms);
        }

        return vmMap;
    }

    /**
     * Creates the list of VMs in CloudSim Plus for a given customer represented by a {@link DatacenterBroker}.
     *
     * @param broker {@link DatacenterBroker} representing a customer in CloudSim Plus, for who VMs will be created
     * @param cr  {@link CustomerRegistry} object used to create VMs and Cloudlets for such a broker
     * @param createdVms the number of VMs already created
     * @return the a map containing the list of created VMs for the given customer (DatacenterBroker)
     * @see #createDatacenterBrokersFromCustomerRegistries()
     */
    private List<Vm> createVmListForOneBroker(
        final DatacenterBroker broker,
        final CustomerRegistry cr,
        final int createdVms) throws RuntimeException
    {
        final int totalVmsAmount = cr.getVmList().stream().mapToInt(VirtualMachineRegistry::getAmount).sum();
        final List<Vm> list = new ArrayList<>(totalVmsAmount);
        for (VirtualMachineRegistry vmr : cr.getVmList()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                list.add(createVm(createdVms+i, broker, vmr));
            }
        }

        return list;
    }

    private Vm createVm(final int id,
                        final DatacenterBroker broker,
                        final VirtualMachineRegistry vmr) throws RuntimeException
    {
        CloudletScheduler scheduler = PolicyLoader.cloudletScheduler(vmr);
        return new VmSimple(id, vmr.getMips(), vmr.getPesNumber())
            .setBroker(broker)
            .setRam(vmr.getRam())
            .setBw(vmr.getBw())
            .setSize(vmr.getSize())
            .setCloudletScheduler(scheduler);
    }

    /**
     * Creates a map that stores the cloudlet list for each customer (broker).
     *
     * @param brokerRegistries the map containing the abstract customer information
     *                         ({@link CustomerRegistry}) obtained from the YAML file, for each
     *                         concrete customer created ({@link DatacenterBroker}).
     * @return the map of Cloudlets created.
     */
    private Map<DatacenterBroker, List<Cloudlet>> createCloudletsFromUtilizationProfiles(
        final Map<DatacenterBroker, CustomerRegistry> brokerRegistries)
    {
        final Map<DatacenterBroker, List<Cloudlet>> map = new HashMap<>(brokerRegistries.size());
        int createdCloudlets = 0;
        for (DatacenterBroker broker : brokerRegistries.keySet()) {
            final int cloudletsNum =
                brokerRegistries.get(broker)
                    .getUtilizationProfile()
                    .stream()
                    .mapToInt(UtilizationProfile::getNumOfCloudlets)
                    .sum();
            List<Cloudlet> cloudlets = new ArrayList<>(cloudletsNum);
            for (UtilizationProfile up : brokerRegistries.get(broker).getUtilizationProfile()) {
                for (int i = 0; i < up.getNumOfCloudlets(); i++) {
                    cloudlets.add(createCloudlet(++createdCloudlets, up, broker));
                }
            }
            map.put(broker, cloudlets);
        }

        return map;
    }

    private Cloudlet createCloudlet(
        final int id,
        final UtilizationProfile up,
        final DatacenterBroker broker) throws RuntimeException
    {
        UtilizationModel cpuUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelCpuAlias());
        UtilizationModel ramUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelRamAlias());
        UtilizationModel bwUtilization  = PolicyLoader.utilizationModel(up.getUtilizationModelBwAlias());

        return new CloudletSimple(id, up.getLength(), up.getCloudletsPesNumber())
            .setFileSize(up.getFileSize())
            .setOutputSize(up.getOutputSize())
            .setUtilizationModelCpu(cpuUtilization)
            .setUtilizationModelRam(ramUtilization)
            .setUtilizationModelBw(bwUtilization)
            .setBroker(broker);
    }

    /**
     * Searches for a VM, with the specified id, at the broker (that represents
     * the customer).
     *
     * @param broker The broker (customer) where to search for the VM
     * @param vmId   The desired VM to be located.
     * @return the VM with the given ID or {@link Vm#NULL} if not found.
     */
    private Vm findVm(final DatacenterBroker broker, final int vmId) {
        return vmsToBrokerMap.get(broker).stream().filter(vm -> vm.getId() == vmId).findFirst().orElse(Vm.NULL);
    }

    /**
     * Creates the concrete Datacenter list from the abstract
     * DatacenterRegistry list.
     *
     * @return Returns the list of created CloudSim datacenters
     * @throws IllegalArgumentException Throws when the method,
     *                                  starting from the information at YAML file,
     *                                  sets invalid parameters for CloudSim Datacenter objects
     *                                  to be created.
     * @see YamlCloudScenario#datacenterRegistries
     */
    private List<Datacenter> createDatacentersFromDatacenterRegistries() throws IllegalArgumentException {
        String datacenterName;
        int datacenterCount = 0;
        final int datacenterNumber = scenario.getDatacenters().stream().mapToInt(DatacenterRegistry::getAmount).sum();
        final List<Datacenter> datacenterList = new ArrayList<>(datacenterNumber);
        for (DatacenterRegistry dcr : scenario.getDatacenters()) {
            int hostCount = 0;
            for (int i = 0; i < dcr.getAmount(); i++) {
                datacenterName = generateDataCenterName(dcr, ++datacenterCount);

                List<Host> hostList = createHostsFromHostRegistries(dcr, hostCount);
                hostCount += hostList.size();

                try {
                    Datacenter dc = createDataCenter(datacenterName, dcr, hostList);
                    datacenterList.add(dc);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }

        return datacenterList;
    }

    /**
     * Creates the concrete host list from the abstract DatacenterRegistry
     * object.
     *
     * @param dcr       A specific abstract datacenter information
     *                  get from the datacenterRegistries list at the YAML file.
     * @param initialHostId the ID for the first Host to be created
     * @return the list of created hosts from the specified datacenterRegistry.
     * @throws RuntimeException
     * @see YamlCloudScenario#datacenterRegistries
     */
    private List<Host> createHostsFromHostRegistries(
        final DatacenterRegistry dcr, int initialHostId) throws RuntimeException
    {
        int hostId;
        final int hostNumber = dcr.getHostList().stream().mapToInt(HostRegistry::getAmount).sum();
        final List<Host> hostList = new ArrayList<>(hostNumber);
        for (HostRegistry hr : dcr.getHostList()) {
            for (int i = 0; i < hr.getAmount(); i++) {
                List<Pe> peList = createHostProcessingElements(hr);
                hostId = generateHostId(hr, ++initialHostId);
                hostList.add(createHost(hostId, hr, peList));
            }
        }

        return hostList;
    }

    private String generateDataCenterName(final DatacenterRegistry dcr, final int datacenterCount) {
        final String datacenterName = dcr.getName();
        if (dcr.getName() == null || dcr.getName().trim().equals("")) {
            return String.format("datacenter%d", datacenterCount);
        }

        return datacenterName;
    }

    private Datacenter createDataCenter(
        final String datacenterName, final DatacenterRegistry dcr,
        final List<Host> hostList)
    {
        final DatacenterCharacteristics characteristics = createDatacenterCharacteristics(dcr, hostList);
        final List<FileStorage> storageList = createSan(dcr);
        final VmAllocationPolicy allocationPolicy = PolicyLoader.vmAllocationPolicy(dcr);

        return new DatacenterSimple(cloudsimplus, characteristics, allocationPolicy)
            .setStorageList(storageList)
            .setSchedulingInterval(dcr.getSchedulingInterval());
    }

    private Host createHost(final int hostId, final HostRegistry hr, final List<Pe> peList) throws RuntimeException {
        ResourceProvisioner ramProvisioner = PolicyLoader.newResourceProvisioner(hr);
        ResourceProvisioner bwProvisioner = PolicyLoader.newResourceProvisioner(hr);
        VmScheduler vmScheduler = PolicyLoader.vmScheduler(hr.getSchedulingPolicyAlias());

        Host host = new HostSimple(hr.getRam(), hr.getBw(), hr.getStorage(), peList);
        host
            .setRamProvisioner(ramProvisioner)
            .setBwProvisioner(bwProvisioner)
            .setVmScheduler(vmScheduler)
            .setId(hostId);
        return host;
    }

    private int generateHostId(final HostRegistry hr, final int currentHostCount) {
        return hr.getId() != 0 ? hr.getId() : currentHostCount;
    }

    /**
     * Creates a CloudSim StorageSystem Area Network (SAN) from an abstract DatacenterRegistry
     * information, get from a YAML specification file.
     *
     * @param dcr A specific DatacenterRegistry information
     *            to be used to create the SANs.
     * @return Returns the SANs created list.
     * @throws IllegalArgumentException Throws when the method,
     *                                  starting from the information at YAML file,
     *                                  sets invalid parameters for CloudSim SAN objects
     *                                  to be created.
     * @see YamlCloudScenario#datacenterRegistries
     */
    private List<FileStorage> createSan(final DatacenterRegistry dcr) throws IllegalArgumentException {
        final List<FileStorage> list = new ArrayList<>(dcr.getSanList().size());
        for (SanStorageRegistry sr : dcr.getSanList()) {
            SanStorage san = new SanStorage(sr.getCapacity(), sr.getBandwidth(), sr.getNetworkLatency());
            list.add(san);
        }

        return list;
    }

    /**
     * Create a DatacenterCharacteristics object
     * that represents that characteristics of one CloudSim Datacenter,
     * including the list of hosts of the Datacenter.
     *
     * @param dcr       The abstract DatacenterRegistry information, obtained from the YAML file,
     *                  to be used to define the Datacenter characteristics.
     * @param hostList  The list of hosts of the Datacenter
     * @return Returns the DatacenterCharacteristics object created.
     * @see #createDatacenterCharacteristics(DatacenterRegistry, List)
     */
    private DatacenterCharacteristics createDatacenterCharacteristics(
        final DatacenterRegistry dcr, List<Host> hostList)
    {
        return new DatacenterCharacteristicsSimple(hostList)
            .setArchitecture(dcr.getArchitecture())
            .setOs(dcr.getOs())
            .setVmm(dcr.getVmm())
            .setCostPerSecond(dcr.getCostPerSec())
            .setCostPerMem(dcr.getCostPerMem())
            .setCostPerStorage(dcr.getCostPerStorage())
            .setCostPerBw(dcr.getCostPerBw());
    }

    /**
     * Creates a list of PEs (Processing Elements, i.e. CPUs/Cores)
     * for a host or VM.
     *
     * @param hr The abstract Host information obtained from the YAML file.
     * @return Returns the list of PEs created.
     */
    private List<Pe> createHostProcessingElements(final HostRegistry hr) {
        final List<Pe> list = new ArrayList<>(hr.getNumOfPes());
        for (int i = 0; i < hr.getNumOfPes(); i++) {
            list.add(new PeSimple(hr.getMipsPerPe(), PolicyLoader.newPeProvisioner(hr)));
        }

        return list;
    }

    @Override
    public void run() {
        final double startTime = System.currentTimeMillis();
        cloudsimplus.start();

        for (DatacenterBroker broker : brokers.keySet()) {
            new CloudletsTableBuilder(broker.getCloudletsFinishedList())
                .setTitle(broker.getName())
                .build();
        }

        final double finishTimeSecs = (System.currentTimeMillis() - startTime)/1000;
        System.out.printf("\nCloud Simulation finished in %.2f seconds!\n", finishTimeSecs);
    }

    /**
     * Gets the <a href="http://cloudsimplus.org">CloudSim Plus</a> instance used to run the simulation.
     *
     * @return
     */
    public CloudSim getCloudSimPlus() {
        return cloudsimplus;
    }

    /**
     * Gets the list of {@link Datacenter}s created in CloudSim Plus, obtained from the
     * {@link DatacenterRegistry}.
     *
     * @return
     */
    public List<Datacenter> getDatacenters() {
        return datacenters;
    }

    /**
     * Gets the list of all {@link Host}s created in CloudSim Plus for all {@link Datacenter}s.
     *
     * @return
     */
    public List<Host> getHosts() {
        return datacenters.stream()
            .map(Datacenter::getHostList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Gets the list of {@link DatacenterBroker}s created in CloudSim Plus,
     * each one representing a customer obtained from a {@link CustomerRegistry}.
     * @return
     */
    public List<DatacenterBroker> getBrokers() {
        return new ArrayList<>(brokers.keySet());
    }

    /**
     * Gets the list of Virtual Machines ({@link Vm}) for each customer ({@link DatacenterBroker})
     * created in CloudSim Plus.
     * @return
     * @see #getVms()
     */
    public List<Vm> getVms(DatacenterBroker broker){
        return vmsToBrokerMap.get(broker);
    }

    /**
     * Gets the list of all Virtual Machines ({@link Vm} created in CloudSim Plus.
     * @return
     * @see #getVms(DatacenterBroker)
     */
    public List<Vm> getVms(){
        return vmsToBrokerMap.values().stream().flatMap(List::stream).collect(toList());
    }

    /**
     * Gets the list of applications ({@link Cloudlet}s) for each customer ({@link DatacenterBroker})
     * created in CloudSim Plus.
     * @return
     * @see #getCloudlets()
     */
    public List<Cloudlet> getCloudlets(DatacenterBroker broker){
        return cloudletsToBrokerMap.get(broker);
    }

    /**
     * Gets the list of all applications ({@link Cloudlet}s) created in CloudSim Plus.
     * @return
     * @see #getCloudlets(DatacenterBroker)
     */
    public List<Cloudlet> getCloudlets(){
        return cloudletsToBrokerMap.values().stream().flatMap(List::stream).collect(toList());
    }
}
