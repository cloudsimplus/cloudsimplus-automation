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
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.DatacenterStorage;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.resources.SanStorage;
import org.cloudsimplus.util.Log;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

/**
 * Represents a simulation created in CloudSim Plus.
 * @author Manoel Campos da Silva Filho
 */
public class CloudSimulation implements Runnable {
    private final YamlCloudScenario scenario;
    private CloudSimPlus cloudsimplus;
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
        final var map = new HashMap<DatacenterBroker, CustomerRegistry>(totalBrokerAmount);
        for (final CustomerRegistry cr: scenario.getCustomers()) {
            for (int i = 0; i < cr.getAmount(); i++) {
                map.put(new DatacenterBrokerSimple(cloudsimplus), cr);
            }
        }

        return map;
    }

    /**
     * Creates the list of VMs in CloudSim Plus for each customer represented by a {@link DatacenterBroker}.
     *
     * @param crMap a Map between a {@link DatacenterBroker} representing a customer in CloudSim Plus
     *           and the {@link CustomerRegistry} object used to create VMs and Cloudlets for such a broker.
     * @return a map containing the list of created VMs for each customer (DatacenterBroker).
     * @see #createBrokers()
     */
    private Map<DatacenterBroker, List<Vm>> createVmListForAllBrokers(
        final Map<DatacenterBroker, CustomerRegistry> crMap)
    {
        final var vmMap = new HashMap<DatacenterBroker, List<Vm>>(crMap.size());

        int createdVms = 0;
        for (var broker : crMap.keySet()) {
            final var vmList = createVmListForOneBroker(broker, crMap.get(broker), createdVms++);
            vmMap.put(broker, vmList);
        }

        return vmMap;
    }

    /**
     * Creates the list of VMs in CloudSim Plus for a given customer represented by a {@link DatacenterBroker}.
     *
     * @param broker {@link DatacenterBroker} representing a customer in CloudSim Plus, for who VMs will be created
     * @param cr  {@link CustomerRegistry} object used to create VMs and Cloudlets for such a broker
     * @param createdVms the number of VMs already created
     * @return a map containing the list of created VMs for the given customer (DatacenterBroker)
     * @see #createBrokers()
     */
    private List<Vm> createVmListForOneBroker(
        final DatacenterBroker broker,
        final CustomerRegistry cr,
        final int createdVms) throws RuntimeException
    {
        final int totalVmsAmount = cr.getVms().stream().mapToInt(VmRegistry::getAmount).sum();
        final var vmList = new ArrayList<Vm>(totalVmsAmount);
        for (VmRegistry vmr : cr.getVms()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                vmList.add(createVm(createdVms+i, broker, vmr));
            }
        }

        return vmList;
    }

    private Vm createVm(final int id,
                        final DatacenterBroker broker,
                        final VmRegistry vmr) throws RuntimeException
    {
        final var scheduler = PolicyLoader.cloudletScheduler(vmr);
        final Vm vm = new VmSimple(id, vmr.getMips(), vmr.getPes());
        vm
            .setRam(vmr.getRam())
            .setBw(vmr.getBw())
            .setSize(vmr.getSize())
            .setCloudletScheduler(scheduler)
            .setBroker(broker);
        return vm;
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
        final var map = new HashMap<DatacenterBroker, List<Cloudlet>>(brokerRegistries.size());
        int createdCloudlets = 0;
        for (var broker : brokerRegistries.keySet()) {
            final int cloudletsNum =
                brokerRegistries.get(broker)
                    .getCloudlets()
                    .stream()
                    .mapToInt(CloudletRegistry::getAmount)
                    .sum();
            final var cloudletList = new ArrayList<Cloudlet>(cloudletsNum);
            for (CloudletRegistry up : brokerRegistries.get(broker).getCloudlets()) {
                for (int i = 0; i < up.getAmount(); i++) {
                    cloudletList.add(createCloudlet(++createdCloudlets, up, broker));
                }
            }
            map.put(broker, cloudletList);
        }

        return map;
    }

    private Cloudlet createCloudlet(
        final int id,
        final CloudletRegistry up,
        final DatacenterBroker broker) throws RuntimeException
    {
        final var cpuUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelCpu());
        final var ramUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelRam());
        final var bwUtilization  = PolicyLoader.utilizationModel(up.getUtilizationModelBw());

        final var cloudlet = new CloudletSimple(id, up.getLength(), up.getPes());
        cloudlet
            .setFileSize(up.getFileSize())
            .setOutputSize(up.getOutputSize())
            .setUtilizationModelCpu(cpuUtilization)
            .setUtilizationModelRam(ramUtilization)
            .setUtilizationModelBw(bwUtilization)
            .setBroker(broker);
        return cloudlet;
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
        final var datacenterList = new ArrayList<Datacenter>(datacenterNumber);
        for (DatacenterRegistry dcr : scenario.getDatacenters()) {
            int hostCount = 0;
            for (int i = 0; i < dcr.getAmount(); i++) {
                datacenterName = generateDataCenterName(dcr, ++datacenterCount);

                List<Host> hostList = createHosts(dcr, hostCount);
                hostCount += hostList.size();

                try {
                    final var dc = createDataCenter(datacenterName, dcr, hostList);
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
        final var hostList = new ArrayList<Host>(hostNumber);
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

        final var storageList = createSan(dcr);
        final var allocationPolicy = PolicyLoader.vmAllocationPolicy(dcr);

        final var dc = new DatacenterSimple(cloudsimplus, hostList, allocationPolicy);
        dc.setSchedulingInterval(dcr.getSchedulingInterval())
          .setDatacenterStorage(new DatacenterStorage(storageList));
        setDatacenterCharacteristics(dc, dcr);
        return dc;
    }

    private Host createHost(final int hostId, final HostRegistry hr, final List<Pe> peList) throws RuntimeException {
        final var ramProvisioner = PolicyLoader.newResourceProvisioner(hr);
        final var bwProvisioner  = PolicyLoader.newResourceProvisioner(hr);
        final var vmScheduler    = PolicyLoader.vmScheduler(hr.getVmScheduler());

        final var host = new HostSimple(hr.getRam(), hr.getBw(), hr.getStorage(), peList);
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
    private List<SanStorage> createSan(final DatacenterRegistry dcr) throws IllegalArgumentException {
        final var storageList = new ArrayList<SanStorage>(dcr.getSans().size());
        for (SanStorageRegistry sr : dcr.getSans()) {
            final var san = new SanStorage(sr.getCapacity(), sr.getBandwidth(), sr.getNetworkLatency());
            storageList.add(san);
        }

        return storageList;
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
    private void setDatacenterCharacteristics(final Datacenter dc, final DatacenterRegistry dcr)
    {
        dc.getCharacteristics()
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
        final var peList = new ArrayList<Pe>(hr.getPes());
        for (int i = 0; i < hr.getPes(); i++) {
            peList.add(new PeSimple(hr.getMips(), PolicyLoader.newPeProvisioner(hr)));
        }

        return peList;
    }

    /**
     * Builds and runs the simulation scenario in CloudSim Plus.
     */
    @Override
    public void run() {
        final double startTime = System.currentTimeMillis();
        this.cloudsimplus = new CloudSimPlus();
        if(!logEnabled){
            Log.setLevel(Level.OFF);
        }

        this.datacenters = createDatacenters();
        printScenariosConfiguration();

        this.brokers = createBrokers();
        this.vmsToBrokerMap = createVmListForAllBrokers(brokers);
        this.cloudletsToBrokerMap = createCloudlets(brokers);

        for (final var broker : brokers.keySet()) {
            broker.submitVmList(vmsToBrokerMap.get(broker));
            broker.submitCloudletList(cloudletsToBrokerMap.get(broker));
        }

        cloudsimplus.start();

        if(showResults) {
            for (final var broker : brokers.keySet()) {
                final var cloudletList = broker.getCloudletFinishedList();
                cloudletList.sort(comparingLong((Cloudlet c) -> c.getVm().getId()).thenComparingLong(Cloudlet::getId));
                new CloudletsTableBuilder(cloudletList)
                    .setTitle(broker.getName())
                    .build();
            }
        }

        final double finishTimeSecs = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println();
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
    public CloudSimPlus getCloudSimPlus() {
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

    public CloudSimulation setLogEnabled(final boolean logEnabled) {
        this.logEnabled = logEnabled;
        return this;
    }

    public boolean isPrintScenariosConfiguration() {
        return printScenariosConfiguration;
    }

    public CloudSimulation setPrintScenariosConfiguration(final boolean printScenariosConfiguration) {
        this.printScenariosConfiguration = printScenariosConfiguration;
        return this;
    }
}
