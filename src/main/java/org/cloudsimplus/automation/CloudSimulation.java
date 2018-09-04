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

import ch.qos.logback.classic.Level;
import cloudreports.models.*;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.*;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

/**
 * Represents a simulation created in CloudSim Plus.
 * @author Manoel Campos da Silva Filho
 */
public class CloudSimulation implements Runnable {
    private final YamlCloudScenario scenario;
    private CloudSim cloudsimplus;
    private List<Datacenter> datacenters;
    private boolean showResults;
    private boolean logEnabled;
    private boolean printScenariosConfiguration;

    private Map<DatacenterBroker, CustomerRegistry> brokers;
    private Map<DatacenterBroker, List<Vm>> vmsToBrokerMap;
    private Map<DatacenterBroker, List<Cloudlet>> cloudletsToBrokerMap;

    /**
     * Instantiates a CloudSimulation object to enable building
     * and running {@link YamlCloudScenario Cloud Simulation Scenario}
     * in CloudSim Plus.
     *
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
        this.showResults = true;
        this.printScenariosConfiguration = true;
        this.datacenters = new ArrayList<>();
        this.logEnabled = false;

        this.vmsToBrokerMap = new HashMap<>();
        this.cloudletsToBrokerMap = new HashMap<>();

        this.brokers = new HashMap<>();
        this.vmsToBrokerMap = new HashMap<>();
        this.cloudletsToBrokerMap = new HashMap<>();
    }

    /**
     * Creates a map containing the abstract customer information ({@link CustomerRegistry})
     * used to create each concrete CloudSim customer ({@link DatacenterBroker}).
     *
     * @return Returns the map created.
     * @see YamlCloudScenario#getCustomers()
     */
    private Map<DatacenterBroker, CustomerRegistry> createBrokers() {
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
     * @see #createBrokers()
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
     * @see #createBrokers()
     */
    private List<Vm> createVmListForOneBroker(
        final DatacenterBroker broker,
        final CustomerRegistry cr,
        final int createdVms) throws RuntimeException
    {
        final int totalVmsAmount = cr.getVms().stream().mapToInt(VmRegistry::getAmount).sum();
        final List<Vm> list = new ArrayList<>(totalVmsAmount);
        for (VmRegistry vmr : cr.getVms()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                list.add(createVm(createdVms+i, broker, vmr));
            }
        }

        return list;
    }

    private Vm createVm(final int id,
                        final DatacenterBroker broker,
                        final VmRegistry vmr) throws RuntimeException
    {
        CloudletScheduler scheduler = PolicyLoader.cloudletScheduler(vmr);
        return new VmSimple(id, vmr.getMips(), vmr.getPes())
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
    private Map<DatacenterBroker, List<Cloudlet>> createCloudlets(
        final Map<DatacenterBroker, CustomerRegistry> brokerRegistries)
    {
        final Map<DatacenterBroker, List<Cloudlet>> map = new HashMap<>(brokerRegistries.size());
        int createdCloudlets = 0;
        for (DatacenterBroker broker : brokerRegistries.keySet()) {
            final int cloudletsNum =
                brokerRegistries.get(broker)
                    .getCloudlets()
                    .stream()
                    .mapToInt(CloudletRegistry::getAmount)
                    .sum();
            List<Cloudlet> cloudlets = new ArrayList<>(cloudletsNum);
            for (CloudletRegistry up : brokerRegistries.get(broker).getCloudlets()) {
                for (int i = 0; i < up.getAmount(); i++) {
                    cloudlets.add(createCloudlet(++createdCloudlets, up, broker));
                }
            }
            map.put(broker, cloudlets);
        }

        return map;
    }

    private Cloudlet createCloudlet(
        final int id,
        final CloudletRegistry up,
        final DatacenterBroker broker) throws RuntimeException
    {
        UtilizationModel cpuUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelCpu());
        UtilizationModel ramUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelRam());
        UtilizationModel bwUtilization  = PolicyLoader.utilizationModel(up.getUtilizationModelBw());

        return new CloudletSimple(id, up.getLength(), up.getPes())
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
     * @see YamlCloudScenario#getDatacenters()
     */
    private List<Datacenter> createDatacenters() throws IllegalArgumentException {
        String datacenterName;
        int datacenterCount = 0;
        final int datacenterNumber = scenario.getDatacenters().stream().mapToInt(DatacenterRegistry::getAmount).sum();
        final List<Datacenter> datacenterList = new ArrayList<>(datacenterNumber);
        for (DatacenterRegistry dcr : scenario.getDatacenters()) {
            int hostCount = 0;
            for (int i = 0; i < dcr.getAmount(); i++) {
                datacenterName = generateDataCenterName(dcr, ++datacenterCount);

                List<Host> hostList = createHosts(dcr, hostCount);
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
     * @see YamlCloudScenario#getDatacenters()
     */
    private List<Host> createHosts(
        final DatacenterRegistry dcr, int initialHostId) throws RuntimeException
    {
        int hostId;
        final int hostNumber = dcr.getHosts().stream().mapToInt(HostRegistry::getAmount).sum();
        final List<Host> hostList = new ArrayList<>(hostNumber);
        for (HostRegistry hr : dcr.getHosts()) {
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

        final List<FileStorage> storageList = createSan(dcr);
        final VmAllocationPolicy allocationPolicy = PolicyLoader.vmAllocationPolicy(dcr);

        Datacenter dc = new DatacenterSimple(cloudsimplus, hostList, allocationPolicy);
        dc.setSchedulingInterval(dcr.getSchedulingInterval())
          .setDatacenterStorage(new DatacenterStorage(storageList));
        setDatacenterCharacteristics(dc, dcr);
        return dc;
    }

    private Host createHost(final int hostId, final HostRegistry hr, final List<Pe> peList) throws RuntimeException {
        ResourceProvisioner ramProvisioner = PolicyLoader.newResourceProvisioner(hr);
        ResourceProvisioner bwProvisioner = PolicyLoader.newResourceProvisioner(hr);
        VmScheduler vmScheduler = PolicyLoader.vmScheduler(hr.getVmScheduler());

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
     * @see YamlCloudScenario#getDatacenters()
     */
    private List<FileStorage> createSan(final DatacenterRegistry dcr) throws IllegalArgumentException {
        final List<FileStorage> list = new ArrayList<>(dcr.getSans().size());
        for (SanStorageRegistry sr : dcr.getSans()) {
            SanStorage san = new SanStorage(sr.getCapacity(), sr.getBandwidth(), sr.getNetworkLatency());
            list.add(san);
        }

        return list;
    }

    /**
     * Sets the attributes of the DatacenterCharacteristics inside the Datacenter.
     * It represents that characteristics of one CloudSim Datacenter,
     * including the list of hosts of the Datacenter.
     *
     * @param dc the Datacenter to set its characteristics
     * @param dcr       The abstract DatacenterRegistry information, obtained from the YAML file,
     *                  to be used to define the Datacenter characteristics.
     */
    private void setDatacenterCharacteristics(
        Datacenter dc, final DatacenterRegistry dcr)
    {
        dc.getCharacteristics()
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
        final List<Pe> list = new ArrayList<>(hr.getPes());
        for (int i = 0; i < hr.getPes(); i++) {
            list.add(new PeSimple(hr.getMips(), PolicyLoader.newPeProvisioner(hr)));
        }

        return list;
    }

    /**
     * Builds and runs the simulation scenario in CloudSim Plus.
     */
    @Override
    public void run() {
        final double startTime = System.currentTimeMillis();
        this.cloudsimplus = new CloudSim();
        if(!logEnabled){
            Log.setLevel(Level.OFF);
        }

        this.datacenters = createDatacenters();
        printScenariosConfiguration();

        this.brokers = createBrokers();
        this.vmsToBrokerMap = createVmListForAllBrokers(brokers);
        this.cloudletsToBrokerMap = createCloudlets(brokers);

        for (DatacenterBroker broker : brokers.keySet()) {
            broker.submitVmList(vmsToBrokerMap.get(broker));
            broker.submitCloudletList(cloudletsToBrokerMap.get(broker));
        }

        cloudsimplus.start();

        if(showResults) {
            for (DatacenterBroker broker : brokers.keySet()) {
                List<Cloudlet> list = broker.getCloudletFinishedList();
                list.sort(comparingInt((Cloudlet c) -> c.getVm().getId()).thenComparingInt(Cloudlet::getId));
                new CloudletsTableBuilder(list)
                    .setTitle(broker.getName())
                    .build();
            }
        }

        final double finishTimeSecs = (System.currentTimeMillis() - startTime) / 1000;
        printFinalResults(finishTimeSecs);
    }

    private void printScenariosConfiguration() {
        if(!isPrintScenariosConfiguration()){
            return;
        }

        System.out.println("Hosts========================");
        for (Datacenter datacenter : datacenters) {
            System.out.println(datacenter.getName() + ": " + datacenter.getHostList().size() + " hosts");
        }
        System.out.println("=============================");
    }

    private void printFinalResults(final double finishTimeSecs) {
        LogUtils.setColSeparator(";");
        final String[] captions =
            {"Framework    ", "Simulation time (seconds)", "Simulation time (minutes)", "Simulation time (hours)",
                "Datacenters", "Hosts from all DCs", "VMs from all Customers", "Cloudlets from all Customers"};

        LogUtils.printCaptions(captions);
        LogUtils.printLine(captions,
            "CloudSim Plus",
            finishTimeSecs,
            String.format("%.4f", finishTimeSecs/60.0),
            String.format("%.6f", finishTimeSecs/3600.0),
            getNumDatacenters(),
            getNumHostsFromAllDatacenters(),
            getNumVmsFromAllCustomers(),
            getNumCloudletsFromAllCustomers());
    }

    private int getNumDatacenters() {
        return scenario.getDatacenters().stream().mapToInt(DatacenterRegistry::getAmount).sum();
    }

    private int getNumHostsFromAllDatacenters() {
        return scenario.getDatacenters().stream().mapToInt(dc -> dc.getAmount() * getNumOfHostsFromDatacenter(dc)).sum();
    }

    private int getNumOfHostsFromDatacenter(DatacenterRegistry dc) {
        return dc.getHosts().stream().mapToInt(h -> h.getAmount()).sum();
    }

    private int getNumVmsFromAllCustomers() {
        return scenario.getCustomers().stream().mapToInt(c -> c.getAmount() * getNumVmsForCustomer(c)).sum();
    }

    private int getNumVmsForCustomer(CustomerRegistry customer) {
        return customer.getVms().stream().mapToInt(vm -> vm.getAmount()).sum();
    }

    private int getNumCloudletsFromAllCustomers() {
        return scenario.getCustomers().stream().mapToInt(c -> c.getAmount() * getNumCloudletsForCustomer(c)).sum();
    }

    private int getNumCloudletsForCustomer(CustomerRegistry customer) {
        return customer.getCloudlets().stream().mapToInt(cloudlet -> cloudlet.getAmount()).sum();
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

    public boolean isShowResults() {
        return showResults;
    }

    public CloudSimulation setShowResults(boolean showResults) {
        this.showResults = showResults;
        return this;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public CloudSimulation setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
        return this;
    }

    public boolean isPrintScenariosConfiguration() {
        return printScenariosConfiguration;
    }

    public CloudSimulation setPrintScenariosConfiguration(boolean printScenariosConfiguration) {
        this.printScenariosConfiguration = printScenariosConfiguration;
        return this;
    }
}
