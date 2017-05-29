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
import com.esotericsoftware.yamlbeans.YamlReader;
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
 * Represents a Cloud Computing simulation environment read from an YAML file.
 * Each environment inside an YAML file is represented by an object of this class.
 * It stores all data representing the simulation to be created in <a href="http://cloudsimplus.org">CloudSim Plus</a>
 * and enables one to build and execute the simulation.
 *
 * <p><b>Objects of this class are created automatically by
 * a {@link YamlCloudEnvironmentReader} class.</b>
 * After you have one of these instances, you may
 * call the {@link #build()} method
 * on it to create and run the Cloud Computing simulation using CloudSim Plus.</p>
 *
 * <p>
 * Each environment inside the YAML file can be delimited using 3 dashes:<br>
 * <center><b>---</b></center>
 * </p>
 *
 * @author Manoel Campos da Silva Filho
 * @see YamlCloudEnvironmentReader
 */
public class YamlCloudEnvironment {
    private List<DatacenterRegistry> datacenterRegistries;
    private List<CustomerRegistry> customerRegistries;

    private CloudSim cloudsimplus;
    private List<Datacenter> datacenters;
    private Map<DatacenterBroker, CustomerRegistry> brokers;
    private Map<DatacenterBroker, List<Vm>> vmsToBrokerMap;
    private Map<DatacenterBroker, List<Cloudlet>> cloudletsToBrokerMap;

    /**
     * A default constructor that is called by a {@link YamlReader} using
     * reflection. This way, usually objects of this class don't have to be created manually.
     *
     * @see YamlCloudEnvironmentReader
     */
    public YamlCloudEnvironment() {
        this.datacenters = new ArrayList<>();
        this.customerRegistries = new ArrayList<>();
        this.datacenterRegistries = new ArrayList<>();
        this.vmsToBrokerMap = new HashMap<>();
        this.cloudletsToBrokerMap = new HashMap<>();
    }

    /**
     * Builds the <a href="http://cloudsimplus.org">CloudSim Plus</a> concrete objects and runs the simulation
     * using such a framework.
     *
     * @throws IllegalArgumentException Throws when the method,
     *                                  starting from the information at YAML file,
     *                                  sets invalid parameters for CloudSim objects
     *                                  to be created.
     */
    public void build() throws IllegalArgumentException {
        build("");
    }

    /**
     * Builds the <a href="http://cloudsimplus.org">CloudSim Plus</a> concrete objects and runs the simulation
     * using such a framework.
     *
     * @param label A label to be used to identify the running
     *              simulation environment. Commonly this can be the name
     *              of the loaded YAML file.
     * @throws IllegalArgumentException Throws when the method,
     *                                  starting from the information at YAML file,
     *                                  sets invalid parameters for CloudSim objects
     *                                  to be created.
     */
    public void build(final String label) throws IllegalArgumentException {
        final double startTime = System.currentTimeMillis();
        cloudsimplus = new CloudSim();
        System.out.println("Hosts========================");
        this.datacenters = createConcreteDatacentersFromAbstractDatacenterRegistries();
        for (Datacenter datacenter : datacenters) {
            System.out.println(datacenter.getName() + ": " + datacenter.getHostList().size() + " hosts");
        }
        System.out.println("=============================");

        this.brokers = createConcreteDatacenterBrokersFromAbstractCustomerRegistries();
        this.vmsToBrokerMap = createConcreteVmListForAllBrokers(brokers);
        this.cloudletsToBrokerMap = createConcreteCloudletsFromAbstractUtilizationProfiles(brokers);

        for (DatacenterBroker broker : brokers.keySet()) {
            broker.submitVmList(vmsToBrokerMap.get(broker));
            broker.submitCloudletList(cloudletsToBrokerMap.get(broker));
        }

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
     * Creates a map containing the abstract customer information ({@link CustomerRegistry})
     * used to create each concrete CloudSim customer ({@link DatacenterBroker}).
     *
     * @return Returns the map created.
     * @see YamlCloudEnvironment#customerRegistries
     */
    private Map<DatacenterBroker, CustomerRegistry> createConcreteDatacenterBrokersFromAbstractCustomerRegistries() {
        final int totalBrokerAmount = customerRegistries.stream().mapToInt(CustomerRegistry::getAmount).sum();
        final Map<DatacenterBroker, CustomerRegistry> list = new HashMap<>(totalBrokerAmount);
        int brokerCount = 0;
        for (CustomerRegistry cr: customerRegistries) {
            for (int i = 0; i < cr.getAmount(); i++) {
                list.put(new DatacenterBrokerSimple(cloudsimplus), cr);
            }
        }

        return list;
    }

    /**
     * Creates the list of VMs in CloudSim Plus for each customer represented by a {@link DatacenterBroker}.
     *
     * @param cr the information about the customers, which are represented
     *           in CloudSim Plus by {@link DatacenterBroker} objects.
     * @return the a map containing the list of created VMs for each customer (DatacenterBroker).
     * @see YamlCloudEnvironment#createConcreteDatacenterBrokersFromAbstractCustomerRegistries()
     */
    private Map<DatacenterBroker, List<Vm>> createConcreteVmListForAllBrokers(
        final Map<DatacenterBroker, CustomerRegistry> cr)
    {
        final Map<DatacenterBroker, List<Vm>> list = new HashMap<>(cr.size());

        int createdVms = 0;
        for (DatacenterBroker broker : cr.keySet()) {
            List<Vm> vms = createConcreteVmListForOneBroker(broker, cr.get(broker), ++createdVms);
            list.put(broker, vms);
        }

        return list;
    }

    /**
     * Creates the list of VMs in CloudSim Plus for a given customer represented by a {@link DatacenterBroker}.
     *
     * @param broker the {@link DatacenterBroker} to create the VMs to
     * @param cr the information about the customers, which are represented
     *           in CloudSim Plus by {@link DatacenterBroker} objects.
     * @param createdVms the number of VMs already created.
     * @return the a map containing the list of created VMs for the given customer (DatacenterBroker).
     * @see YamlCloudEnvironment#createConcreteDatacenterBrokersFromAbstractCustomerRegistries()
     */
    private List<Vm> createConcreteVmListForOneBroker(
        final DatacenterBroker broker,
        final CustomerRegistry cr,
        final int createdVms) throws RuntimeException
    {
        final int totalVmsAmount = cr.getVmList().stream().mapToInt(VirtualMachineRegistry::getAmount).sum();
        final List<Vm> list = new ArrayList<>(totalVmsAmount);
        for (VirtualMachineRegistry vmr : cr.getVmList()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                list.add(createVm(createdVms, broker, vmr));
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
    private Map<DatacenterBroker, List<Cloudlet>> createConcreteCloudletsFromAbstractUtilizationProfiles(
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
     * @see YamlCloudEnvironment#datacenterRegistries
     */
    private List<Datacenter> createConcreteDatacentersFromAbstractDatacenterRegistries() throws IllegalArgumentException {
        String datacenterName;
        int datacenterCount = 0;
        final int datacenterNumber = datacenterRegistries.stream().mapToInt(DatacenterRegistry::getAmount).sum();
        final List<Datacenter> datacenterList = new ArrayList<>(datacenterNumber);
        for (DatacenterRegistry dcr : datacenterRegistries) {
            int hostCount = 0;
            for (int i = 0; i < dcr.getAmount(); i++) {
                datacenterName = generateDataCenterName(dcr, ++datacenterCount);

                List<Host> hostList = createConcreteHostsFromAbstractHostRegistries(dcr, hostCount);
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
     * @see YamlCloudEnvironment#datacenterRegistries
     */
    private List<Host> createConcreteHostsFromAbstractHostRegistries(
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
     * @see YamlCloudEnvironment#datacenterRegistries
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
     * @see YamlCloudEnvironment#createDatacenterCharacteristics(DatacenterRegistry, List)
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

    /**
     * Gets a List of {@link DatacenterRegistry} objects representing abstract information about data centers.
     * These objects contain, for instance, the amount of datacenters
     * to be created and the host amount and configurations.
     *
     * <p>Each YAML environment can have multiple datacenters
     * that are abstractly specified using DatacenterRegistry
     * objects. The concrete datacenters are
     * created by CloudSim Plus.</p>
     * @return
     */
    public List<DatacenterRegistry> getDatacenterRegistries() {
        return datacenterRegistries;
    }

    /**
     * Gets a List of {@link DatacenterRegistry} objects.
     *
     * @param datacenterRegistries the datacenterRegistries to set
     * @see #getDatacenterRegistries()
     */
    public void setDatacenterRegistries(final List<DatacenterRegistry> datacenterRegistries) {
        if(datacenterRegistries == null){
            return;
        }
        this.datacenterRegistries = datacenterRegistries;
    }

    /**
     * Gets a List of {@link CustomerRegistry} objects representing abstract information about customers (brokers).
     * These objects contain, for instance, the amount of customers
     * to be created and the VM amount and configurations.
     *
     * <p>Each YAML environment can have multiple customers
     * that are abstractly specified using CustomerRegistry
     * objects. The concrete customers are
     * created by CloudSim Plus as its DatacenterBroker objects.
     * </p>
     * @return
     */
    public List<CustomerRegistry> getCustomerRegistries() {
        return customerRegistries;
    }

    /**
     * Sets a List of {@link CustomerRegistry} objects representing abstract information about customers (brokers).
     * @param customerRegistries the customerRegistries to set
     * @see #getCustomerRegistries()
     */
    public void setCustomerRegistries(final List<CustomerRegistry> customerRegistries) {
        if(customerRegistries == null){
            return;
        }
        this.customerRegistries = customerRegistries;
    }

    /**
     * Gets the <a href="http://cloudsimplus.org">CloudSim Plus</a> instance used to run the simulation.
     * @return
     */
    public CloudSim getCloudSimPlus() {
        return cloudsimplus;
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
     * Gets the list of {@link Datacenter}s created in CloudSim Plus, obtained from the
     * {@link DatacenterRegistry}.
     * @return
     */
    public List<Datacenter> getDatacenters() {
        return datacenters;
    }

    /**
     * Gets the list of all {@link Host}s created in CloudSim Plus for all {@link Datacenter}s.
     * @return
     */
    public List<Host> getHosts() {
        return datacenters.stream()
                .map(Datacenter::getHostList)
                .flatMap(List::stream)
                .collect(toList());
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
