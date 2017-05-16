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
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilderHelper;

import java.util.*;

/**
 * Creates a cloud environment autonomously from a YAML file.
 * The application has a command line interface that accept the YAML file name as parameter.
 * Each scenario specified into a YAML file will be represented by an object of this class.
 *
 * <p>
 * Each scenario inside the file can be delimited using the line below:
 * <br>
 *    <code><i>--- #Delimits each Cloud Environment</i></code>
 * </p>
 *
 * @author Manoel Campos da Silva Filho
 */
public class YamlScenario {
    /**
     * Abstract information about data centers.
     * This object contains, for instance, the amount of datacenters
     * to be created and the host amount and configurations.
     * Each YAML scenario can have multiple datacenters
     * that are abstractly specified using DatacenterRegistry
     * objects. The concrete datacenters are
     * created by CloudSim.
     */
    private List<DatacenterRegistry> datacenterRegistries;

    /**
     * Abstract information about customers (brokers).
     * This object contains, for instance, the amount of customers
     * to be created and the VM amount and configurations.
     * Each YAML scenario can have multiple customers
     * that are abstractly specified using CustomerRegistry
     * objects. The concrete customers are
     * created by CloudSim as its DatacenterBroker objects.
     */
    private List<CustomerRegistry> customerRegistries;

    /**
     * Concrete cloudlet list.
     * The list of cloudlets (applications) for each customer (Broker)
     * to be run at CloudSim.
     */
    private Map<DatacenterBroker, List<Cloudlet>> brokerCloudlets;

    /**
     * Concrete VM list.
     * The list of Virtual Machines (VMs) for each customer (Broker)
     * to be instantiated at CloudSim.
     */
    private Map<DatacenterBroker, List<Vm>> brokerVms;

    /**
     * Concrete data center list.
     * The list of datacenters to be created at CloudSim, obtained from the
     * abstract DatacenterRegistry information from the YAML file.
     */
    private List<Datacenter> datacenters;

    private CloudSim cloudsim;

    public YamlScenario(){
        this.datacenters = new ArrayList<>();
        this.customerRegistries = new ArrayList<>();
        this.datacenterRegistries = new ArrayList<>();
        this.brokerVms = new HashMap<>();
        this.brokerCloudlets = new HashMap<>();
    }

    /**
     * @return the datacenterRegistries
     */
    public List<DatacenterRegistry> getDatacenterRegistries() {
        return datacenterRegistries;
    }

    /**
     * @param datacenterRegistries the datacenterRegistries to set
     */
    public void setDatacenterRegistries(final List<DatacenterRegistry> datacenterRegistries) {
        this.datacenterRegistries = datacenterRegistries;
    }

    /**
     * @return the customerRegistries
     */
    public List<CustomerRegistry> getCustomerRegistries() {
        return customerRegistries;
    }

    /**
     * @param customerRegistries the customerRegistries to set
     */
    public void setCustomerRegistries(final List<CustomerRegistry> customerRegistries) {
        this.customerRegistries = customerRegistries;
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
     * @see YamlScenario#datacenterRegistries
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

    public Datacenter createDataCenter(
        final String datacenterName, final DatacenterRegistry dcr,
        final List<Host> hostList)
    {
        final DatacenterCharacteristics characteristics = createDatacenterCharacteristics(dcr, hostList);
        final List<FileStorage> storageList = createSan(dcr);
        final VmAllocationPolicy allocationPolicy = PolicyLoader.vmAllocationPolicy(dcr);

        return new DatacenterSimple(cloudsim, characteristics, allocationPolicy)
            .setStorageList(storageList)
            .setSchedulingInterval(dcr.getSchedulingInterval());
    }

    public String generateDataCenterName(final DatacenterRegistry dcr, final int datacenterCount) {
        final String datacenterName = dcr.getName();
        if (dcr.getName() == null || dcr.getName().trim().equals("")) {
            return String.format("datacenter%d", datacenterCount);
        }

        return datacenterName;
    }

    /**
     * Creates a map containing the abstract customer information (CustomerRegistry)
     * used to create each concrete CloudSim customer (DatacenterBroker).
     *
     * @return Returns the map created.
     * @see YamlScenario#customerRegistries
     */
    private Map<DatacenterBroker, CustomerRegistry> createConcreteDatacenterBrokersFromAbstractCustomerRegistries() {
        final int totalBrokerAmount = customerRegistries.stream().mapToInt(CustomerRegistry::getAmount).sum();
        final Map<DatacenterBroker, CustomerRegistry> list = new HashMap<>(totalBrokerAmount);
        int brokerCount = 0;
        for (CustomerRegistry cr: customerRegistries) {
            for (int i = 0; i < cr.getAmount(); i++) {
                list.put(new DatacenterBrokerSimple(cloudsim), cr);
            }
        }

        return list;
    }

    /**
     * Create the VM list for each customer represented by the brokers list.
     *
     * @return Returns the a map containing the list of created VMs
     * for each customer (DatacenterBroker).
     * @see YamlScenario#createConcreteDatacenterBrokersFromAbstractCustomerRegistries()
     */
    private Map<DatacenterBroker, List<Vm>> createConcreteVmListForAllBrokers(
        final Map<DatacenterBroker, CustomerRegistry> customerRegistries)
    {
        final Map<DatacenterBroker, List<Vm>> list = new HashMap<>(customerRegistries.size());

        int vmCount = 0;
        for (DatacenterBroker broker : customerRegistries.keySet()) {
            List<Vm> vms = createConcreteVmListForOneBroker(broker, customerRegistries.get(broker), ++vmCount);
            list.put(broker, vms);
        }

        return list;
    }

    public List<Vm> createConcreteVmListForOneBroker(
        final DatacenterBroker broker,
        final CustomerRegistry cr,
        final int vmCount) throws RuntimeException
    {
        final int totalVmsAmount = cr.getVmList().stream().mapToInt(VirtualMachineRegistry::getAmount).sum();
        final List<Vm> list = new ArrayList<>(totalVmsAmount);
        for (VirtualMachineRegistry vmr : cr.getVmList()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                list.add(createVm(vmCount, broker, vmr));
            }
        }

        return list;
    }

    public Vm createVm(final int vmCount,
                       final DatacenterBroker broker,
                       final VirtualMachineRegistry vmr) throws RuntimeException
    {
        CloudletScheduler scheduler = PolicyLoader.cloudletScheduler(vmr);
        return new VmSimple(vmCount, vmr.getMips(), vmr.getPesNumber())
            .setBroker(broker)
            .setRam(vmr.getRam())
            .setBw(vmr.getBw())
            .setSize(vmr.getSize())
            .setCloudletScheduler(scheduler);
    }

    /**
     * Create a map that stores the cloudlet list for each customer (broker).
     *
     * @param brokerRegistries The map containing the abstract customer information
     *                         (CustomerRegistry) obtained from the YAML file, for each
     *                         concrete customer created (DatacenterBroker).
     * @return the map of Cloudlets created.
     */
    private Map<DatacenterBroker, List<Cloudlet>> createConcreteCloudletsFromAbstractUtilizationProfiles(
        final Map<DatacenterBroker, CustomerRegistry> brokerRegistries)
    {
        final Map<DatacenterBroker, List<Cloudlet>> map = new HashMap<>(brokerRegistries.size());
        int cloudletCount = 0;
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
                    cloudlets.add(createCloudlet(++cloudletCount, up, broker));
                }
            }
            map.put(broker, cloudlets);
        }

        return map;
    }

    public Cloudlet createCloudlet(
        final int cloudletCount,
        final UtilizationProfile up,
        final DatacenterBroker broker) throws RuntimeException
    {
        UtilizationModel cpuUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelCpuAlias());
        UtilizationModel ramUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelRamAlias());
        UtilizationModel bwUtilization  = PolicyLoader.utilizationModel(up.getUtilizationModelBwAlias());

        return new CloudletSimple(cloudletCount, up.getLength(), up.getCloudletsPesNumber())
            .setFileSize(up.getFileSize())
            .setOutputSize(up.getOutputSize())
            .setUtilizationModelCpu(cpuUtilization)
            .setUtilizationModelRam(ramUtilization)
            .setUtilizationModelBw(bwUtilization)
            .setBroker(broker);
    }

    public String getHostIdOfVm(final Vm vm) {
        if (vm != null && vm.getHost() != null) {
            return String.valueOf(vm.getHost().getId());
        }

        return "";
    }

    /**
     * Create the concrete host list from the abstract DatacenterRegistry
     * object.
     *
     * @param dcr       A specific abstract datacenter information
     *                  get from the datacenterRegistries list at the YAML file.
     * @param initialHostId the ID for the first Host to be created
     * @return the list of created hosts from the specified datacenterRegistry.
     * @throws RuntimeException
     * @see YamlScenario#datacenterRegistries
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


    public Host createHost(final int hostId, final HostRegistry hr, final List<Pe> peList) throws RuntimeException {
        ResourceProvisioner ramProvisioner = PolicyLoader.newRamProvisioner(hr);
        ResourceProvisioner bwProvisioner = PolicyLoader.newBwProvisioner(hr);
        VmScheduler vmScheduler = PolicyLoader.vmScheduler(hr.getSchedulingPolicyAlias());

        return new HostSimple(hostId, hr.getStorage(), peList)
            .setRamProvisioner(ramProvisioner)
            .setBwProvisioner(bwProvisioner)
            .setVmScheduler(vmScheduler);
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
     * @see YamlScenario#datacenterRegistries
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
     * @see YamlScenario#createDatacenterCharacteristics(DatacenterRegistry, List)
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
     * Searches for a VM, with the specified id, at the broker (that represents
     * the customer).
     *
     * @param broker The broker (customer) where to search for the VM
     * @param vmId   The desired VM to be located.
     * @return the VM with the given ID or {@link Vm#NULL} if not found.
     */
    private Vm findVm(final DatacenterBroker broker, final int vmId) {
        return brokerVms.get(broker).stream().filter(vm -> vm.getId() == vmId).findFirst().orElse(Vm.NULL);
    }

    /**
     * From the abstract simulation scenario, loaded from the YAML file,
     * creates the CloudSim concretes objects and runs the simulation
     * using CloudSim.
     *
     * @param simulationLabel A label to be used to identify the running
     *                        simulation scenario. Commonly this can be the name
     *                        of the loaded YAML file.
     * @throws IllegalArgumentException Throws when the method,
     *                                  starting from the information at YAML file,
     *                                  sets invalid parameters for CloudSim objects
     *                                  to be created.
     */
    public void run(final String simulationLabel) throws IllegalArgumentException {
        final double startTime = System.currentTimeMillis();
        cloudsim = new CloudSim();
        System.out.println("Hosts========================");
        this.datacenters = createConcreteDatacentersFromAbstractDatacenterRegistries();
        for (Datacenter datacenter : datacenters) {
            System.out.println(datacenter.getName() + ": " + datacenter.getHostList().size() + " hosts");
        }
        System.out.println("=============================");

        final Map<DatacenterBroker, CustomerRegistry> brokers = createConcreteDatacenterBrokersFromAbstractCustomerRegistries();
        this.brokerVms = createConcreteVmListForAllBrokers(brokers);
        this.brokerCloudlets = createConcreteCloudletsFromAbstractUtilizationProfiles(brokers);

        for (DatacenterBroker broker : brokers.keySet()) {
            broker.submitVmList(brokerVms.get(broker));
            broker.submitCloudletList(brokerCloudlets.get(broker));
        }

        cloudsim.start();

        final Map<DatacenterBroker, List<Cloudlet>> receivedCloudletList = new HashMap<>();
        for (DatacenterBroker broker : brokers.keySet()) {
            receivedCloudletList.put(broker, broker.getCloudletsFinishedList());
        }

        for (DatacenterBroker broker : brokers.keySet()) {
            new CloudletsTableBuilderHelper(broker.getCloudletsFinishedList())
                .setTitle(broker.getName())
                .build();
        }

        final double finishTimeSecs = (System.currentTimeMillis() - startTime)/1000;
        System.out.printf("\nCloud Simulation finished in %.2f seconds\n!", finishTimeSecs);
    }

}
